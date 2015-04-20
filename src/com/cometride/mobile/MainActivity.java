package com.cometride.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
@SuppressLint("RtlHardcoded") public class MainActivity extends ActionBarActivity implements LocationListener, OnItemClickListener
{
	GoogleMap map;
	ViewPager mviewpager;
	ActionBar actionBar;
	private DrawerLayout drawerLayout;
	private ListView listView;
	private List<String> routelist;
	private ActionBarDrawerToggle drawerListner;
	
	RiderDatabaseController dbcontroller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{	
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
        
        //Initializing Drawer Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerListner = new ActionBarDrawerToggle(this, drawerLayout,R.drawable.ic_drawer,0,0)
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
        
        
        //Loading RouteID Information from RouteInformation Table.
        HashMap<String,String> routelistMap = new HashMap<String, String>();
        routelistMap = dbcontroller.GetAllRouteLatLong();
        routelist = new ArrayList<String>(routelistMap.keySet());
        //Arrays.asList(routelistMap.keySet().toArray());
        listView = (ListView) findViewById(R.id.drawerList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1,routelist));
        listView.setOnItemClickListener(this);
        
        //Display ALl Route Information
        android.app.FragmentManager fragmentManager = getFragmentManager();
	    Bundle args = new Bundle();
	    android.app.Fragment fragment = null;
		fragment = new AllRouteUserInterface();
		fragment.setArguments(args);
	    fragmentManager.beginTransaction()
	                   .replace(R.id.mainContent, fragment)
	                   .commit();	    
        Toast.makeText(MainActivity.this,"Main Create",Toast.LENGTH_SHORT).show();     
       
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		drawerListner.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		drawerListner.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
	{
		Toast.makeText(this, routelist.get(position), Toast.LENGTH_SHORT).show();
		SelectItem(position);
		android.app.FragmentManager fragmentManager = getFragmentManager();
	    Bundle args = new Bundle();
	    android.app.Fragment fragment = null;
		fragment = new RouteSpecificUserInterface(routelist.get(position));
		fragment.setArguments(args);
	    fragmentManager.beginTransaction()
	                   .replace(R.id.mainContent, fragment)
	                   .commit();

		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void SelectItem(int position)
	{
		listView.setItemChecked(position, true);
		SetTitle(routelist.get(position));
			    // Highlight the selected item, update the title, and close the drawer
	}
	public void SetTitle(String title)
	{
		getSupportActionBar().setTitle(title);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        int id = item.getItemId();
        if (id == R.id.action_settings) 
        {
            return true;
        }
        else if(drawerListner.onOptionsItemSelected(item))
        {
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onLocationChanged(Location location) 
	{
		double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
 
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(latLng)      // Sets the center of the map to location user
        .zoom(18)                   // Sets the zoom
        //.bearing(90)                // Sets the orientation of the camera to east
        //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
        .build();                   // Creates a CameraPosition from the builder
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	public void Initialize()
	{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		dbcontroller = new RiderDatabaseController(this);
	}

	
	
}
