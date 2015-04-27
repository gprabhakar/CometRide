package com.cometride.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class Blank extends ActionBarActivity
{
	private SharedPreferences pref;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Initialize();
	}
	
	public void Initialize()
	{
		pref= getSharedPreferences("COMET", 0);
		editor = pref.edit();
		editor.putString("SubscribedRoute", "");
		editor.putString("ServiceStatus", "NOT RUNNING");
		editor.commit();
		stopService(new Intent(this,RiderService.class));
		this.finish();
	}

}