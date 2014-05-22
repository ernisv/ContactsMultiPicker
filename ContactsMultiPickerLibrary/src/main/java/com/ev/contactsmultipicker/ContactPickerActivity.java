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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Ernestas Vaiciukevicius (ernestas.vaiciukevicius@gmail.com)
 *
 */
public class ContactPickerActivity extends ActionBarActivity {
	
	public static final String PICKER_TYPE = "type";
	public static final String PICKER_TYPE_PHONE = "phone";
	public static final String CONTACT_PICKER_RESULT = "contacts";
	
	public static final int RESULT_ERROR = RESULT_FIRST_USER;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getIntent().getAction() != null && !Intent.ACTION_PICK.equals(getIntent().getAction())) {
			Intent ret = new Intent();
			ret.putExtra("error", "Unsupported action type");
			setResult(RESULT_ERROR, ret);
			return;
		}
		
		if (getIntent().getExtras() == null || PICKER_TYPE_PHONE.equals(getIntent().getExtras().getString(PICKER_TYPE))) {
			setContentView(R.layout.contact_list);
		} else {
			Intent ret = new Intent();
			ret.putExtra("error", "Unsupported picker type");
			setResult(RESULT_ERROR, ret);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_picker_options, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.contacts_done) {
			returnResults();
			return true;
		} else if (item.getItemId() == R.id.contacts_cancel) {
			cancel();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void returnResults() {
		ContactListFragment fragment = (ContactListFragment) getSupportFragmentManager().getFragments().get(0);
		ArrayList<ContactResult> resultList = new ArrayList<ContactResult>( fragment.getResults().values() );
		Intent retIntent = new Intent();
		retIntent.putExtra(CONTACT_PICKER_RESULT, resultList);
		
		setResult(RESULT_OK, retIntent);
		finish();
	}
	
	private void cancel() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onBackPressed() {
		cancel();
		super.onBackPressed();
	}
	
}
