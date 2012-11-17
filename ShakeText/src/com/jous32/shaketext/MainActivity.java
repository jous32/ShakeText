package com.jous32.shaketext;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements TextWatcher {

	private SensorManager mSensorManager;

	private ShakeEventListener mSensorListener;

	List<String> listContacts = new ArrayList<String>();

	Spinner spinnerListContacts;
	EditText editTextMessage;

	String message;
	String contactNumber;

	TextView selection;
	AutoCompleteTextView autoCompleteTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		readContactList();

		selection = (TextView) findViewById(R.id.textView2);// hello world
		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);

		autoCompleteTextView.addTextChangedListener(this);

		autoCompleteTextView.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_expandable_list_item_1, listContacts));
		initShakeSensor();

	}

	public void initShakeSensor() {
	    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mSensorListener = new ShakeEventListener();   
 
	    mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

	      public void onShake() {
	    	  if(contactNumber != null && message !=null ){
	    		  sendSMS(contactNumber, message);
	    		  Toast.makeText(MainActivity.this, "Mensaje enviado!", Toast.LENGTH_SHORT).show();
	    	  }
	       
	      }
	    });
	}


	@Override
	  protected void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(mSensorListener,
	        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	        SensorManager.SENSOR_DELAY_UI);
	  }

	  @Override
	  protected void onPause() {
	    mSensorManager.unregisterListener(mSensorListener);
	    super.onStop();
	  }


	@Override
	protected void onStop() {
		// is called when an activity is no longer visible to, or interacting
		// with, the user
		mSensorManager.unregisterListener(mSensorListener);
		super.onStop();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	// ---sends an SMS message to another device---
	private void sendSMS(String phoneNumber, String message) {
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
				MainActivity.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}

	public void readContactList() {
		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		while (phones.moveToNext()) {
			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			listContacts.add(name + "<" + phoneNumber + ">");
		}
		phones.close();

	}

	public void saveData(View view) {
		if (contactNumber != null) {
			editTextMessage = (EditText) findViewById(R.id.editTextMessage);
			message = editTextMessage.getText().toString();
			Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT)
					.show();
		} else {
			message = null;
			Toast.makeText(getApplicationContext(), "Select a valid contact",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void readContacts() {

		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String phone = "";
				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						phone = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						listContacts.add(name + "<" + phone + ">");

					}
					pCur.close();
				}

			}
		}
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// Nothing

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// Nothing

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		if (autoCompleteTextView.getText().toString().trim().contains("<")
				&& autoCompleteTextView.getText().toString().trim()
						.contains(">")
				&& autoCompleteTextView.getText().toString().trim().length() > 2) {
			String[] words = autoCompleteTextView.getText().toString()
					.split("<");
			contactNumber = words[1].replace(">", "");
		} else {
			contactNumber = null;
		}

	}

}
