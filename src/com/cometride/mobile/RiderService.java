package com.cometride.mobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.app.AlarmManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class RiderService extends Service implements LocationListener{

	private LocationManager lm;
	private SharedPreferences pref;
	private Editor editor;
	private AlarmManager alarmManager;
	private Intent intent;
	private PendingIntent pendingIntent;
	private int UpdateCounter=0;
	private RiderDatabaseController dbController;
	
	//############################ LIFE CYCLE EVENTS ########################################//
	@Override
	public void onCreate() 
	{
		super.onCreate();
		//AWS Thread Issue Fix
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		Log.i("Comet","Service Started");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Comet","Service OnStart Command");
		Initialize();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.i("Comet","Service Destroy");
		lm.removeUpdates(this);
		alarmManager.cancel(pendingIntent);
		Toast.makeText(this, "You are Unsubscribed from "+pref.getString("SubscribedRoute", ""), Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	
	//###################################### INITIALIZE ################################//
	public void Initialize()
	{
		dbController= new RiderDatabaseController(getBaseContext());
		pref = getSharedPreferences("COMET", 0);
		editor = pref.edit();
		
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        
        if(location!=null)
        {
            onLocationChanged(location);
        }
        
        if(pref.getString("ServiceStatus", "NOT RUNNING").equals("NOT RUNNING"))
		{
        	Calendar calSet = Calendar.getInstance();
            calSet.add(Calendar.MINUTE, 10);
    		
            intent = new Intent(getBaseContext(), Blank.class);
    		pendingIntent = PendingIntent.getActivity(getBaseContext(), 1 , intent, 0);
    	    alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
    		Toast.makeText(this, "Subscription will Expire in 10 Mins !!", Toast.LENGTH_LONG).show();
			editor.putString("ServiceStatus", "RUNNING");
			editor.commit();
		}
	}
	
	
	//##################################### LOCATION BASED EVENTS ###########################//
	@Override
	public void onLocationChanged(Location location) {
		UpdateCounter++;
		List<LatLng> CabLocationList = new ArrayList<LatLng>();
		
		if(UpdateCounter==4)
		{
			Log.i("Comet","Location Updated from Service");
			CabLocationList = FetchCapacity();
			if(pref.getString("SendDistanceNotification", "Yes").equals("Yes"))
				CalculateDistance(CabLocationList,location);
			UpdateCounter=0;
		}
	}
	
	//#################################### I'M INTERESTED FEATURE FUNCTIONS ########################//
	public List<LatLng> FetchCapacity()
	{
		List<DBLiveVehicleInformationClass> dbLiveInfo = dbController.GetLiveVehicleAvailability(pref.getString("SubscribedRoute", "Route"));
		List<LatLng> cabLocationList = new ArrayList<LatLng>();
		
		int availability = 0;
		for (DBLiveVehicleInformationClass dbLiveVehicleInfo : dbLiveInfo) 
		{
			cabLocationList.add(new LatLng(dbLiveVehicleInfo.getVehicleLat(),dbLiveVehicleInfo.getVehicleLong()));
			if((dbLiveVehicleInfo.getVehicleTotalCapacity()- dbLiveVehicleInfo.getCurrentRiders())> availability)
			{
				availability = (dbLiveVehicleInfo.getVehicleTotalCapacity()- dbLiveVehicleInfo.getCurrentRiders());
			}
		}
		
		int prevAvailability = pref.getInt("SubscribedRouteAvailability", 0);
		if(prevAvailability==0)
		{
			if(availability!=0)
			{	
				SendNotification(availability+" Seat Available","");
				Toast.makeText(getBaseContext(), "Seat Available", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			if(availability==0)
			{	
				SendNotification("Seat Not Available","");
				Toast.makeText(getBaseContext(), "Seat Not Available", Toast.LENGTH_SHORT).show();
			}
		}
		editor.putInt("SubscribedRouteAvailability", availability);
		editor.commit();
		
		return cabLocationList;
	}
	
	public void CalculateDistance(List<LatLng> CabLocationList,Location location)
	{
		float[] result = new float[2];
		float distance = 1000;
		for (LatLng latLng : CabLocationList) 
		{
			Location.distanceBetween(location.getLatitude(),location.getLongitude(),latLng.latitude,latLng.longitude,result);
			if(distance>result[0])
				distance=result[0];
		}
		if(distance<200)
		{
			double ETA = (0.33334)*(result[0] * 0.00062137)*60;
			int min = (int)ETA;
			int sec =  (int)(ETA - min)*60;
			SendNotification("Cab Is Near You","ETA :~ "+min+"Mins "+sec+"Sec");
			editor.putString("SendDistanceNotification", "No");
			editor.commit();
			//CalculateDistance(CabLocationList,location);
		}
	
		
	}

	public void SendNotification(String Message,String SecondMessage)
    {			
      Intent intent = new Intent(getBaseContext(), MainActivity.class);			
	  PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(),0 ,intent,0);
		
	  Intent callIntent = new Intent(Intent.ACTION_DELETE);
      PendingIntent callpIntent = PendingIntent.getActivity(getBaseContext(), 0, callIntent, 0);
		
      NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
	  String[] events = new String[]{Message,"",SecondMessage,"","",""};
      inboxStyle.setBigContentTitle("Comet Ride");
      
		for (int i=0; i < events.length; i++) 
		{
		    inboxStyle.addLine(events[i]);
		}
		
	  NotificationCompat.Builder n  = new NotificationCompat.Builder(getBaseContext());
	  n.setContentTitle("CometRide");
	  //n.setContentText(SecondMessage);
	  n.setSmallIcon(R.drawable.ic_launcher);
	  n.setAutoCancel(true);
	  n.addAction(R.drawable.ic_action_action_thumb_up,"Open CometRide",callpIntent);
	  n.addAction(R.drawable.ic_action_action_delete,"UnSubscribe",callpIntent);
	  n.setStyle(inboxStyle);
	  n.setPriority(Notification.PRIORITY_MAX);
	  n.build(); 
	            
	  NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
	  notificationManager.notify(0, n.build()); 
	}	    	
	
	
	//######################################## UNUSED FUNCTIONS ####################################//
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
