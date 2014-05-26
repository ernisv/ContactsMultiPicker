package com.ev.contactsmultipicker.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.ev.contactsmultipicker.ContactPickerActivity;
import com.ev.contactsmultipicker.ContactResult;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private TextView mTvContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mTvContacts = (TextView) findViewById(R.id.tvContacts);
    }

    public void showContacts(View view) {
        // Take care of using a random request code.
        startActivityForResult(new Intent(this, ContactPickerActivity.class), 1302);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1302 && RESULT_OK == resultCode) {
            processContacts((ArrayList<ContactResult>)
                    data.getSerializableExtra(ContactPickerActivity.CONTACT_PICKER_RESULT));
        } else if(RESULT_CANCELED == resultCode) {
        	if (data != null && data.hasExtra("error")) {
        		mTvContacts.setText(data.getStringExtra("error"));
        	} else {
        		mTvContacts.setText("Contact selection cancelled");
        	}
        }
    }

    private void processContacts(ArrayList<ContactResult> contacts) {
        StringBuilder sb = new StringBuilder();
        for(ContactResult contactResult : contacts) {
            sb.append(contactResult.getContactId());
            sb.append(" <");
            for(ContactResult.ResultItem item : contactResult.getResults()) {
                sb.append(item.getResult());
                if(contactResult.getResults().size() > 1) {
                    sb.append(", ");
                }
            }
            sb.append(">, ");
        }
        mTvContacts.setText(sb.toString());
    }
}
