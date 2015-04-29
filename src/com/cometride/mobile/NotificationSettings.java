package com.cometride.mobile;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class NotificationSettings extends ActionBarActivity implements OnClickListener
{

	private CheckBox chkSeat;
	private CheckBox chkDistance;
	private CheckBox chkService;
	private EditText etDistance;
	private EditText etFrequency;
	private EditText etDuration;
	private Button btnSave;
	private Button btnCancel;
	private SharedPreferences pref;
	private Editor editor;
	
	
	//########################################## LIFE CYCLE EVENT #################################//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
		Initialize();
	}
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void Initialize()
	{
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		
		chkSeat =(CheckBox)findViewById(R.id.chkSeat);
		chkDistance =(CheckBox)findViewById(R.id.chkDistance);
		chkService =(CheckBox)findViewById(R.id.chkService);
		etDistance = (EditText)findViewById(R.id.etDistance);
		etFrequency = (EditText)findViewById(R.id.etFrequency);
		etDuration = (EditText)findViewById(R.id.etDuration);
		btnSave = (Button)findViewById(R.id.btnSave);
		btnCancel = (Button)findViewById(R.id.btnCancel);
		pref = getSharedPreferences("COMET", 0);
		editor= pref.edit();
		
		chkSeat.setChecked(pref.getBoolean("NotifySeat", true));
		chkDistance.setChecked(pref.getBoolean("NotifyDistance", true));
		chkService.setChecked(pref.getBoolean("NotifyService", true));
		etDistance.setText(String.valueOf(pref.getInt("Distance", 200)));
		etFrequency.setText(String.valueOf(pref.getInt("Frequency", 5)));
		etDuration.setText(String.valueOf(pref.getInt("SubscriptionDuration", 15)));
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
	}
	//########################################## CLICK EVENT #################################//
	@Override
	public void onClick(View v) 
	{
		Intent in;
		
		switch (v.getId()) {
		case R.id.btnSave:
			int distance = 200;
			int frequency =5;
			int duration =15;
			String toastString="";
			
			if(chkSeat.isChecked())
				editor.putBoolean("NotifySeat", true);
			else
				editor.putBoolean("NotifySeat", false);
			
			if(chkDistance.isChecked())
				editor.putBoolean("NotifyDistance", true);
			else
				editor.putBoolean("NotifyDistance", false);
			
			if(chkService.isChecked())
				editor.putBoolean("NotifyService", true);
			else
				editor.putBoolean("NotifyService", false);
			
			
			if(etDistance.getText().toString().equals(""))
			{
				toastString = toastString+"Default Distance 200 M";
				distance=200;
			}
			else
				distance =Integer.parseInt(etDistance.getText().toString());
			
			if(etFrequency.getText().toString().equals(""))
			{
				toastString = toastString+" Default Frequency 5 Sec";
				frequency=5;
			}
			else
			{
				frequency =Integer.parseInt(etFrequency.getText().toString());
				if(frequency<5)
					frequency=5;
			}
			
			if(etDuration.getText().toString().equals(""))
			{
				toastString = toastString+" Default Duration 15 Mins";
				duration =15;
			}
			else
				duration =Integer.parseInt(etDuration.getText().toString());
			
			if(toastString.equals(""))
				toastString ="Changes will take effect in the next Subscription.";
			else
				toastString=toastString+" is set";
			
			Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
			
			editor.putInt("SubscriptionDuration", duration);
			editor.putInt("Frequency",frequency);
			editor.putInt("Distance", distance);
			editor.commit();
			
			in = new Intent(this,MainActivity.class);
			startActivity(in);
			
			break;
		case R.id.btnCancel:
			//this.finish();
			in = new Intent(this,MainActivity.class);
			startActivity(in);
			break;
		default:
			break;
		}
		
	}
	@Override
	public void onBackPressed() {
	}

}
