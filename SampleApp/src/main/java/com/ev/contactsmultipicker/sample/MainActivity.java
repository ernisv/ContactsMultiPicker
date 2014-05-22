package com.ev.contactsmultipicker.sample;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ev.contactsmultipicker.ContactPickerActivity;
import com.ev.contactsmultipicker.ContactResult;

import java.io.Serializable;
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
        startActivityForResult(
                new Intent(getApplicationContext(), ContactPickerActivity.class), RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(RESULT_OK == resultCode && data.hasExtra(
                ContactPickerActivity.CONTACT_PICKER_RESULT)) {
            processContacts((ArrayList<ContactResult>)
                    data.getSerializableExtra(ContactPickerActivity.CONTACT_PICKER_RESULT));
        } else if(data.hasExtra("error")) {
            mTvContacts.setText(data.getStringExtra("error"));
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
