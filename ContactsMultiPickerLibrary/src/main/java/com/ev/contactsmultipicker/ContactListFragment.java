/**
 * Copyright 2013 Ernestas Vaiciukevicius (ernestas.vaiciukevicius@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. * 
 */
package com.ev.contactsmultipicker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.ev.contactsmultipicker.ContactResult.ResultItem;

/**
 * @author Ernestas Vaiciukevicius (ernestas.vaiciukevicius@gmail.com)
 *
 */
public class ContactListFragment extends Fragment implements LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
	private final static String SAVE_STATE_KEY = "mcListFrag";

	private final String[] projection = new String[] { Contacts._ID, Contacts.DISPLAY_NAME };
	private final String selection = Contacts.HAS_PHONE_NUMBER + " = 1";
	
	private ListView mContactListView;
	private CursorAdapter mCursorAdapter;
	
	private class ContactsCursorAdapter extends SimpleCursorAdapter {
		public ContactsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View ret = super.getView(position, convertView, parent);
			
			CheckBox checkbox = (CheckBox) ret.findViewById(R.id.contactCheck);
			
			getCursor().moveToPosition(position);
			String id = getCursor().getString(0);
			checkbox.setChecked(results.containsKey(id));
			
			return ret; 
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(SAVE_STATE_KEY, results);
	}

	private Hashtable<String, ContactResult> results = new Hashtable<String, ContactResult>();
	
	public Hashtable<String, ContactResult> getResults() {
		return results;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCursorAdapter = new ContactsCursorAdapter(getActivity(), R.layout.contact_list_item, null,
				new String[] { Contacts.DISPLAY_NAME }, 
				new int[] { R.id.contactLabel }, 0);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (savedInstanceState != null) {
			results = (Hashtable<String, ContactResult>) savedInstanceState.getSerializable(SAVE_STATE_KEY);
		}
		
		View rootView = inflater.inflate(R.layout.contact_list_fragment, container);
		
		mContactListView = (ListView) rootView.findViewById(R.id.contactListView);
		
		mContactListView.setAdapter(mCursorAdapter);
		mContactListView.setOnItemClickListener(this);

		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), Contacts.CONTENT_URI, projection, selection, null, Contacts.DISPLAY_NAME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mCursorAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long rowId) {
		CheckBox checkbox = (CheckBox) view.findViewById(R.id.contactCheck);
		
		Cursor cursor = mCursorAdapter.getCursor();
		cursor.moveToPosition(pos);
		String id = cursor.getString(0);
		
		if (checkbox.isChecked()) {
			checkbox.setChecked(false);
			results.remove(id);
		} else {
			checkbox.setChecked(true);
			
			Cursor itemCursor = getActivity().getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
					new String[] { id }, null);
			List<ContactResult.ResultItem> resultItems = new LinkedList<ContactResult.ResultItem>();

			itemCursorLoop:
			while (itemCursor.moveToNext()) {
				String contactNumber = itemCursor.getString(itemCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				int contactKind = itemCursor.getInt(itemCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

				for (ResultItem previousItem : resultItems) {
					if (contactNumber.equals(previousItem.getResult())) {
						continue itemCursorLoop;
					}
				}
				
				resultItems.add(new ContactResult.ResultItem(contactNumber, contactKind));
			}
			itemCursor.close();
			
			if (resultItems.size() > 1) {
				// contact has multiple items - user needs to choose from them
				chooseFromMultipleItems(resultItems, checkbox, id);
			} else {
				// only one result or all items are similar for this contact
				results.put(id, new ContactResult(id, resultItems));
			}
		}
	}
	
	protected void chooseFromMultipleItems(List<ContactResult.ResultItem> items, CheckBox checkbox, String id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		ArrayList<String> itemLabels = new ArrayList<String>(items.size());
		
		for (ResultItem resultItem : items) {
			itemLabels.add(resultItem.getResult());
		}
		
		class ClickListener implements OnCancelListener, OnClickListener, OnMultiChoiceClickListener {
			private List<ContactResult.ResultItem> items;
			private CheckBox checkbox;
			private String id;
			private boolean[] checked;
			
			public ClickListener(List<ContactResult.ResultItem> items, CheckBox checkbox, String id) {
				this.items = items;
				this.checkbox = checkbox;
				this.id = id;
				checked = new boolean[items.size()];
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}

			@Override
			public void onClick(DialogInterface arg0, int which, boolean isChecked) {
				checked[which] = isChecked;
			}

			private void finish() {
				ArrayList<ContactResult.ResultItem> result = new ArrayList<ContactResult.ResultItem>(items.size());
				for (int i = 0; i < items.size(); ++i) {
					if (checked[i]) {
						result.add(items.get(i));
					}
				}
				if (result.size() == 0) {
					checkbox.setChecked(false);
				} else {
					results.put(id, new ContactResult(id, result));
				}
			}

			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
			
		}
		
		ClickListener clickListener = new ClickListener(items, checkbox, id);
		
		builder
			.setMultiChoiceItems(itemLabels.toArray(new String[0]), null, clickListener)
			.setOnCancelListener(clickListener)
			.setPositiveButton(android.R.string.ok, clickListener)
			.show();
	}

}
