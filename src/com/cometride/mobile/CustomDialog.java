package com.cometride.mobile;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.EditText;

public class CustomDialog extends ActionBarActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Custom Alert Dialog");
        final EditText editText=(EditText)dialog.findViewById(R.id.editText);
        Button save=(Button)dialog.findViewById(R.id.save);
        Button btnCancel=(Button)dialog.findViewById(R.id.cancel);
        dialog.show();
	}

}
