package com.cometride.mobile;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.provider.Settings;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
@SuppressLint("RtlHardcoded") public class MainActivity extends ActionBarActivity implements LocationListener, OnItemClickListener
{
	private GoogleMap map;
	ViewPager mviewpager;
	ActionBar actionBar;
	private DrawerLayout drawerLayout;
	private ListView listView;
	private List<String> routeList;
	private ActionBarDrawerToggle drawerListner;
	private RiderDatabaseController dbcontroller;
	private android.app.FragmentManager fragmentManager;
	private SharedPreferences pref;
	private Editor editor;
	
	//############################################### LIFE CYCKE EVENTS ############################################//
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
    }
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		if(isMyServiceRunning(RiderService.class))
		{
			
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Initialize(); 
		drawerListner.syncState();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	//################################# INITIALIZE ##############################################//
	public void Initialize(){
		
		//AWS Thread Issue Fix
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		pref = getSharedPreferences("COMET",0);
		editor= pref.edit();
		
		dbcontroller = new RiderDatabaseController(this);
		fragmentManager = getFragmentManager();
		
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerListner = new ActionBarDrawerToggle(this, drawerLayout,R.drawable.ic_action_navigation_menu,0,0)
        {
        	public void onDrawerOpened(View drawerView) 
        	{
        		super.onDrawerOpened(drawerView);	
        	};
        	public void onDrawerClosed(View drawerView) 
        	{
        		super.onDrawerClosed(drawerView);
        	};
        };
        
        drawerLayout.setDrawerListener(drawerListner);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008542")));
        
       
       if(isNetworkAvailable())
       { 	
    	    //Loading RouteID Information from RouteInformation Table.
    	    routeList = new ArrayList<String>();
    	    routeList = dbcontroller.GetAllRoute();
    	    routeList.add("All Routes");
    	    
	        final CustomList customlistview = new CustomList(this,routeList);
			
	        //Arrays.asList(routelistMap.keySet().toArray());
	        listView = (ListView) findViewById(R.id.drawerList);
	        listView.setAdapter(customlistview);
	        listView.setOnItemClickListener(this);
        
	        //Display ALl Route Information
	        Bundle args = new Bundle();
		    android.app.Fragment fragment = null;
			
		    String Title = getSupportActionBar().getTitle().toString();
		    if(Title.equals("CometRide"))
		    {
		    	if(pref.getString("CurrentScreen","").equals(""))
		    		Title = "All Routes";
		    	else if(!Title.equals("Exit"))
		    		Title =pref.getString("CurrentScreen", "");
		    }
		    
		    fragment = new RouteSpecificUserInterface(Title);
			fragment.setArguments(args);
		    fragmentManager.beginTransaction()
		                   .replace(R.id.mainContent, fragment)
		                   .commit();
		    getSupportActionBar().setTitle(Title);
		    
		    //Toast.makeText(MainActivity.this,"Main Create",Toast.LENGTH_SHORT).show();     
       }
       else
       {
        	Toast.makeText(this, "Please Connect to the Internet.", Toast.LENGTH_SHORT).show();
        	startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
       }
       CheckGPS();
    
	
	}
	
	
	
	public void CheckGPS()
	{
		String provider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(provider.equals(""))
		{
			Toast.makeText(this, "Please Enable GPS Settings", Toast.LENGTH_SHORT).show();
			Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			//Intent in = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
			startActivity(in);
		}
	}
	
	private boolean isNetworkAvailable() 
	{
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
		drawerListner.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
	{
		//Toast.makeText(this, routeList.get(position), Toast.LENGTH_SHORT).show();
		SelectItem(position);
		Bundle args = new Bundle();
	    android.app.Fragment fragment = null;
		fragment = new RouteSpecificUserInterface(routeList.get(position));
		fragment.setArguments(args);
	    fragmentManager.beginTransaction()
	                   .replace(R.id.mainContent, fragment)
	                   .commit();
		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void SelectItem(int position)
	{
		listView.setItemChecked(position, true);
		if(routeList.get(position).equals("All Routes"))
			getSupportActionBar().setTitle("CometRide");
		else
			getSupportActionBar().setTitle(routeList.get(position));
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getMenuInflater().inflate(R.menu.main, menu);
        return true;
   }

    //boolean canAddItem = false;
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        int id = item.getItemId();
        if(id==R.id.action_settings)
        {
        	Intent in = new Intent(this,NotificationSettings.class);
        	startActivity(in);
        }
       
        if(drawerListner.onOptionsItemSelected(item))
        {
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
	@Override
	public void onLocationChanged(Location location) 
	{
		//double latitude = location.getLatitude();
        //double longitude = location.getLongitude();
        //LatLng latLng = new LatLng(latitude, longitude);
 
        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(
          //      new LatLng(location.getLatitude(), location.getLongitude()), 13));

        //CameraPosition cameraPosition = new CameraPosition.Builder()
        //.target(latLng)      // Sets the center of the map to location user
        //.zoom(18)                   // Sets the zoom
        //.bearing(90)                // Sets the orientation of the camera to east
        //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
        //.build();                   // Creates a CameraPosition from the builder
        //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //map.animateCamera(CameraUpdateFactory.zoomTo(15));
        //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	
	
	
	
}
