package com.cometride.mobile;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlarmManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
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
	private int duration;
	private int frequency;
	private int distance;
	private Thread check = null;
	private Location currLocation;
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
		Toast.makeText(this, "You are now Unsubscribed"+pref.getString("SubscribedRoute", ""), Toast.LENGTH_LONG).show();
		stopFlg = true;
		super.onDestroy();
	}
	private boolean stopFlg = false;
	//###################################### INITIALIZE ################################//
	public void Initialize()
	{
		dbController= new RiderDatabaseController(getBaseContext());
		pref = getSharedPreferences("COMET", 0);
		editor = pref.edit();
		duration =pref.getInt("SubscriptionDuration", 15);
	    frequency = pref.getInt("Frequency", 5);
	    distance = pref.getInt("Distance", 200);
	    
	    Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM | Criteria.ACCURACY_MEDIUM);
        //lm = (LocationManager)getBaseContext().getSystemService(Context.LOCATION_SERVICE);
      	//String provider = lm.getBestProvider(criteria, true);
      	//lm.requestLocationUpdates(provider,100,0,this);
        //Location location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
      	
      	lm = (LocationManager)getBaseContext().getSystemService(Context.LOCATION_SERVICE);
      	//String provider = lm.getBestProvider(criteria, true);
      	lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,100,0,this);
      	Location location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
      	
      	
      	
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        
        if(location!=null)
        {
            onLocationChanged(location);
        }
        
        if(pref.getString("ServiceStatus", "NOT RUNNING").equals("NOT RUNNING"))
		{
        	Calendar calSet = Calendar.getInstance();
            calSet.add(Calendar.MINUTE,duration);
            
            intent = new Intent(getBaseContext(), Blank.class);
    		pendingIntent = PendingIntent.getActivity(getBaseContext(), 1 , intent, 0);
    	    alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
    		Toast.makeText(this, "Subscription will Expire in "+duration+" Mins !!", Toast.LENGTH_LONG).show();
			editor.putString("ServiceStatus", "RUNNING");
			editor.commit();
		}
        StartThread();
    }
	
	
	public void StartThread()
	{	
		check = (new Thread() {
            // This method is called when the thread runs
            public void run() {
            	while(true)
            	{
                	try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	if(stopFlg)
	            	{	
	            		break;
	            	}
	            	
	            	List<LatLng> CabLocationList = new ArrayList<LatLng>();
	            	CabLocationList = FetchCapacity();
	    			if(pref.getBoolean("NotifyDistance", true))
	    			{
	    				Log.i("Comet", "Before");
	    				if(pref.getString("SendDistanceNotification", "Yes").equals("Yes"))
	    					CalculateDistance(CabLocationList,currLocation);
	    				Log.i("Comet", "After");
	    			}
	               // SendNotification("Hello","Bye");
            	}
            }
        });
		check.start();
	}
	
	//##################################### LOCATION BASED EVENTS ###########################//
	@Override
	public void onLocationChanged(Location location) 
	{
		currLocation= new Location("gps");
		currLocation.setLatitude(location.getLatitude());
		currLocation.setLongitude(location.getLongitude());
		//UpdateCounter++;
		//List<LatLng> CabLocationList = new ArrayList<LatLng>();
		
		//if(UpdateCounter==1)
		//{	
			//Log.i("Comet","Location Updated from Service");
			//Toast.makeText(this,"Loc Service", Toast.LENGTH_SHORT).show();
			
			//CabLocationList = FetchCapacity();
			//if(pref.getBoolean("NotifyDistance", true))
			//{
			//	if(pref.getString("SendDistanceNotification", "Yes").equals("Yes"))
			//		CalculateDistance(CabLocationList,location);
			//}
			//UpdateCounter=0;
		//}
	}
	
	
	
	//#################################### I'M INTERESTED FEATURE FUNCTIONS ########################//
	public List<LatLng> FetchCapacity()
	{
		List<DBLiveVehicleInformationClass> dbLiveInfo = dbController.GetLiveVehicleAvailability(pref.getString("SubscribedRoute", "Route"));
		List<LatLng> cabLocationList = new ArrayList<LatLng>();
		
		int availability = 0;
		int ServiceAvailability = 0;
		
		for (DBLiveVehicleInformationClass dbLiveVehicleInfo : dbLiveInfo) 
		{
			ServiceAvailability++;
			cabLocationList.add(new LatLng(dbLiveVehicleInfo.getVehicleLat(),dbLiveVehicleInfo.getVehicleLong()));
			if((dbLiveVehicleInfo.getVehicleTotalCapacity()- dbLiveVehicleInfo.getCurrentRiders())> availability)
			{
				availability = (dbLiveVehicleInfo.getVehicleTotalCapacity()- dbLiveVehicleInfo.getCurrentRiders());
			}
		}
		
		if(pref.getBoolean("NotifyService", true))
		{
			int prevServiceAvailability= pref.getInt("SubscribedServiceAvailability", 0);
			if((prevServiceAvailability==0)&& (ServiceAvailability!=0))
			{
				SendNotification("Cab Service Available in the Subscribed Route", "");
			}
			else if((prevServiceAvailability!=0)&& (ServiceAvailability==0))
			{
				SendNotification("Cab Service Cancelled in the Subscribed Route", "");
			}
		}
		editor.putInt("SubscribedServiceAvailability", ServiceAvailability);
		
		if(pref.getBoolean("NotifySeat", true))
		{
			int prevAvailability = pref.getInt("SubscribedRouteAvailability", 0);
			if(prevAvailability==0)
			{
				if(availability!=0)
				{	
					SendNotification(availability+" Seat(s) Available","");
					//Toast.makeText(getBaseContext(), "Seat Available", Toast.LENGTH_SHORT).show();
				}
			}
			else
			{
				if(availability==0)
				{	
					if(ServiceAvailability!=0)
						SendNotification("No Seats Available","");
				}
			}
		}
		editor.putInt("SubscribedRouteAvailability", availability);
		editor.commit();
		
		return cabLocationList;
	}
	
	public void CalculateDistance(List<LatLng> CabLocationList,Location location)
	{
		float[] result = new float[2];
		float cabdistance = 1000;
		for (LatLng latLng : CabLocationList) 
		{
			Location.distanceBetween(location.getLatitude(),location.getLongitude(),latLng.latitude,latLng.longitude,result);
			if(cabdistance>result[0])
				cabdistance=result[0];
		}
		if(cabdistance < distance)
		{
			//1 Miles Per Hour = 0.33334 Per Minute.
			double ETA = (0.33334)*(cabdistance * 0.00062137)*60;
			int min = (int)ETA;
			int sec = (int) Math.ceil((ETA - min)*60);
			SendNotification("Cab Is Near You","ETA : "+min+" Mins "+sec+" Sec");
			editor.putString("SendDistanceNotification", "No");
			editor.commit();
		}	
	}

	public void SendNotification(String Message,String SecondMessage)
    {			
      Intent inOpen = new Intent(getApplicationContext(), NotificationSettings.class);
      inOpen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      PendingIntent pInOpen = PendingIntent.getActivity(getApplicationContext(),0,inOpen,0);
	  
      Intent inApp = new Intent(getApplicationContext(), MainActivity.class);			
      inApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      PendingIntent pInApp = PendingIntent.getActivity(getApplicationContext(),0 ,inApp,0);
	  
      
      Intent inDelete = new Intent(getApplicationContext(), Blank.class);			
      inDelete.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      PendingIntent pInDelete = PendingIntent.getActivity(getApplicationContext(),0 ,inDelete,0);
	  
		
	  //Intent callIntent = new Intent(Intent.ACTION_DELETE);
      //PendingIntent callpIntent = PendingIntent.getActivity(getBaseContext(), 0, callIntent, 0);
		
      NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
	  String[] events = new String[]{Message,"",SecondMessage,"","",""};
      inboxStyle.setBigContentTitle("Comet Ride");
      
		for (int i=0; i < events.length; i++) 
		{
		    inboxStyle.addLine(events[i]);
		}
	  Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

	  NotificationCompat.Builder n  = new NotificationCompat.Builder(getBaseContext());
	  n.setContentTitle("CometRide");
	  n.setSmallIcon(R.drawable.ic_launcher);
	  n.setAutoCancel(true);
	  n.setContentIntent(pInApp);
	  n.addAction(R.drawable.ic_action_action_settings,"Settings",pInOpen);
	  n.addAction(R.drawable.ic_action_action_delete,"UnSubscribe",pInDelete);
	  n.setStyle(inboxStyle);
	  n.setSound(uri);
	  n.setPriority(Notification.PRIORITY_MAX);
	  Notification not = n.build();
	  not.flags = Notification.FLAG_AUTO_CANCEL;
	  NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
	  notificationManager.notify(0, not); 
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
