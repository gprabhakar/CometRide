package com.cometride.mobile;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class CustomDialog extends ActionBarActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Custom Alert Dialog");
        dialog.show();
	}

}
