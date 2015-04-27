package com.cometride.mobile;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> 
{

	private Activity mContext;
	private List<String> RouteList = new ArrayList<String>();
	private TextView tvRouteName;
		

	public CustomList(Context context,List<String> RouteList) 
	{
		super(context, R.layout.navigation_drawer_list,RouteList);
		this.mContext=(Activity) context;
		this.RouteList=RouteList;		
	}	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{			
		LayoutInflater inflater = mContext.getLayoutInflater();
		View rel_layout= inflater.inflate(R.layout.navigation_drawer_list, null,true);
		tvRouteName = (TextView) rel_layout.findViewById(R.id.tvRouteName);
	    tvRouteName.setText(RouteList.get(position));
        return rel_layout;
	}
   
	
	
}
