package com.cometride.mobile;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

public class Blank extends ActionBarActivity
{
	private SharedPreferences pref;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blank);
		Initialize();
	}
	
	public void Initialize()
	{
		//Toast.makeText(this,"Toast",Toast.LENGTH_SHORT).show();
		pref= getSharedPreferences("COMET", 0);
		editor = pref.edit();
		editor.putString("SubscribedRoute", "");
		editor.putString("ServiceStatus", "NOT RUNNING");
		editor.commit();
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		stopService(new Intent(this,RiderService.class));
		startActivity(new Intent(this,MainActivity.class));
	}

}
