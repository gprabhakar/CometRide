package com.cometride.mobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RouteSpecificUserInterface extends android.app.Fragment implements LocationListener,OnCheckedChangeListener
{
	private static GoogleMap map;
	private static MapView mapView;
	private String routeID;
	private ArrayList<LatLng> markerPoints;
	private View myFragmentView =null;
	private List<String> LatLongList = new ArrayList<String>() ;
	private RiderDatabaseController dbcontroller ;
	private DownloadRouteTask downloadRouteTask;
	private DownloadAllRouteTask downloadAllRouteTask;
	private ParserTask parserTask;
	private FetchCabLocationTask fetchTask ;
	private List<Marker> markerlist = new ArrayList<Marker>();
	private List<String> colorlist = new ArrayList<String>();
	private LocationManager lm;
	private int colorcounter = 0;
	private HashMap<String,LatLng> routeMarkerList = new HashMap<String, LatLng>();
	private Switch swSubscribe;
	private LinearLayout llSubscribe;
	private SharedPreferences pref;
	private Editor editor;
	private TextView tvSubscribe;
	private int updateCounter=0;
	private Intent in;
	private boolean checkFlgOverride = false;
	private boolean FirstWarningFlg = true;
	
	//######################################### CONSTRUCTORS ######################################//
	public RouteSpecificUserInterface() 
	{	}

	public RouteSpecificUserInterface(String routeID) 
	{ 
		this.routeID = routeID;  // Initializes the RouteID to fetch the appropriate map information to be fetched from the Database
		
		
	}
	
	//################################ LIFE CYCLE EVENTS ##########################################//
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		if(container==null){return null;}
		try
		{	
			myFragmentView = inflater.inflate(R.layout.fragment_route_user_interface,container, false);
			mapView = (MapView) myFragmentView.findViewById(R.id.map_route1);
			mapView.onCreate(savedInstanceState);
			int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
	    	if(status!=ConnectionResult.SUCCESS)
	    	{
	    		int requestCode = 10;
	            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), requestCode);
	            dialog.show();
	        }
	    }
		catch(Exception ex){//TO avoid Inflater Exception.
		}
		return myFragmentView;
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		if(lm!=null)
		{
			map.clear();
			lm.removeUpdates(this);
		}
		Initialize();
	}
	
	@Override
	public void onPause() 
	{
		super.onPause();
		map.clear();
		lm.removeUpdates(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onDestroyView() 
	{
		super.onDestroyView();
		
	     try {
	        trimCache(getActivity());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	//################################# INITIALIZE ##################################################//
	public void Initialize()
    {
		dbcontroller = new RiderDatabaseController(getActivity());
		
		pref = getActivity().getSharedPreferences("COMET", 0);
		editor = pref.edit();
		editor.putString("CurrentScreen", routeID);
		editor.commit();
		
		//NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		//notificationManager.cancelAll();
		colorlist.add("#0039A6");
		colorlist.add("#00CCFF");
		colorlist.add("#FA0D0D");
		colorlist.add("#460DF1");
		colorlist.add("#0DF14A");
		colorlist.add("#F68308");
		colorlist.add("#8E8B88");
		colorlist.add("#FF0040");
		colorlist.add("#A9E2F3");
		colorlist.add("#FA58D0");
		colorlist.add("#610B0B");
		colorlist.add("#0B3B0B");
		colorlist.add("#8181F7");
		colorlist.add("#F4FA58");
		colorlist.add("#FA5882");
		colorlist.add("#070B19");
		colorlist.add("#0101DF");
		
		swSubscribe= (Switch) getActivity().findViewById(R.id.swSubscribe);
		swSubscribe.setOnCheckedChangeListener(this);
		
		tvSubscribe =(TextView)getActivity().findViewById(R.id.tvSubscribe);
		llSubscribe= (LinearLayout) getActivity().findViewById(R.id.llSubscribe);
		
		mapView.onResume();
		map = mapView.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);        // Enable / Disable my location button
        map.getUiSettings().setMyLocationButtonEnabled(true);        // Enable / Disable Compass icon
        map.getUiSettings().setCompassEnabled(true);        // Enable / Disable Rotate gesture
        map.getUiSettings().setRotateGesturesEnabled(true);        // Enable / Disable zooming functionality
        map.getUiSettings().setZoomGesturesEnabled(true);
        MapsInitializer.initialize(this.getActivity());
        
        markerPoints = new ArrayList<LatLng>();
        LatLng University = new LatLng(32.9864304,-96.7548886);
        CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(University)   							 // Sets the center of the map to location user
        .zoom(15)                  						 // Sets the zoom
        //.bearing(90)              					 // Sets the orientation of the camera to east
        //.tilt(40)                   					 // Sets the tilt of the camera to 30 degrees
        .build();                   					 // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),10,null);
        
        //Criteria criteria = new Criteria();
        //criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
      	//String provider = lm.getBestProvider(criteria, true);
      	lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,100,0,this);
      	Location location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
      	if(location!=null)
        {
            onLocationChanged(location);
        }
        
      	//Toast.makeText(getActivity(),, Toast.LENGTH_SHORT).show();
        if(routeID.equals("All Routes"))
        {
        	LoadAllRouteMap();
        	swSubscribe.setVisibility(View.GONE);
        	tvSubscribe.setVisibility(View.VISIBLE);
        }
        else
        {
        	LoadRouteSpecificMap();
        	swSubscribe.setVisibility(View.VISIBLE);
        	tvSubscribe.setVisibility(View.GONE);
        }		
        
        if(!pref.getString("SubscribedRoute", "").equals(""))
		{
        	swSubscribe.setChecked(true);
			swSubscribe.setText("Subscribed to "+pref.getString("SubscribedRoute", ""));
		}
        else
        {
        	checkFlgOverride = true;
        	swSubscribe.setChecked(false);
        	swSubscribe.setText("Subscribe");
        	llSubscribe.setBackgroundColor(Color.parseColor("#E98300"));
			swSubscribe.setBackgroundColor(Color.parseColor("#E98300"));
        }
       
    }
	
	
	//########################################### DELETING TEMP FILES TO IMPROVE SPEED ######################################//
	public static void trimCache(Context context) {
	    try 
	    {
	       File dir = context.getCacheDir();
	       if (dir != null && dir.isDirectory()) {
	          deleteDir(dir);
	       }
	    } catch (Exception e) {
	    }
	 }

	public static boolean deleteDir(File dir) 
	{
	    if (dir != null && dir.isDirectory()) {
	       String[] children = dir.list();
	       for (int i = 0; i < children.length; i++) {
	          boolean success = deleteDir(new File(dir, children[i]));
	          if (!success) {
	             return false;
	          }
	       }
	    }
	    return dir.delete();
	}

	
	//################################################# GPS RELATED FUNCTION ###############################################//
	@Override
	public void onLocationChanged(Location location) 
	{
		updateCounter++;
		//Toast.makeText(getActivity(),"Loc",Toast.LENGTH_SHORT).show();
		
		if(updateCounter==5)
		{
			Toast.makeText(getActivity(),"Location Update",Toast.LENGTH_SHORT).show();
			Log.i("Comet","Location Update");
			//Toast.makeText(getActivity(), "Loc Update", Toast.LENGTH_SHORT).show();
			if(!routeID.equals("All Routes"))
	        {
	            fetchTask = new FetchCabLocationTask();
	            fetchTask.execute();
	        }
			updateCounter=0;
		}
	}
	
	//############################################ LOADING MAP ROUTES FUNCTIONS ################################################//		
	public void LoadRouteSpecificMap(){	
        downloadRouteTask = new DownloadRouteTask();
        downloadRouteTask.execute();
        //safePointTask = new DrawSafePointsTask();
        //safePointTask.execute();
	}
	
	public void LoadAllRouteMap()
	{
	    	downloadAllRouteTask = new DownloadAllRouteTask();
	    	downloadAllRouteTask.execute();
	}
	
	private String getDirectionsUrl(LatLng origin,LatLng dest)
	{
	        String str_origin = "origin="+origin.latitude+","+origin.longitude;
	        String str_dest = "destination="+dest.latitude+","+dest.longitude;
	        String sensor = "sensor=false";
	        String waypoints = "";
	        
	        for(int i=1;i<markerPoints.size()-1;i++)
	        {
	            LatLng point  =  markerPoints.get(i);
	            if(i==1)
	                waypoints = "waypoints=";
	            waypoints += point.latitude + "," + point.longitude + "|";
	        }
	        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;
	        String output = "json";
	        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&mode=walking";
	        return url;
	 }
	
	private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);            								 // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();            // Connecting to url
            urlConnection.connect();           									 // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null)
            {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }
        catch(Exception e)
        {
            Log.d("Exception while downloading url", e.toString());
        }
        finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void buildmarker(MarkerOptions marker)
	{
		Marker mark = map.addMarker(marker);
		mark.showInfoWindow();
	}
    
    //################################# Async Task#####################################//
 
    private class DownloadRouteTask extends AsyncTask<String, Void, String>
    {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) 
        {
            // For storing data from web service
            String data = "";
            try
            {
        		LatLongList = dbcontroller.GetRouteSpecificLatLong(routeID);
        		markerPoints = new ArrayList<LatLng>();
            	for (String str : LatLongList) 
            	{
            		markerPoints.add(new LatLng(Double.parseDouble(str.split(",")[0]),Double.parseDouble(str.split(",")[1])));	
        		}
                String url_map = getDirectionsUrl(markerPoints.get(0),markerPoints.get(markerPoints.size()-1));
                // Fetching the data from web service
                data = downloadUrl(url_map);
                
                  
        	}
            catch(Exception e)
            {
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) 
        {
        	 List<String> polygonlist = dbcontroller.GetSafePointInfo(routeID);
 			for (String latlonglist : polygonlist) 
 			{
 				List<LatLng> safepoint = new ArrayList<LatLng>();
 				String[] latlong = latlonglist.split(":");
 				for (String str : latlong) 
 				{
 					safepoint.add(new LatLng(Double.parseDouble(str.split(",")[0]), Double.parseDouble(str.split(",")[1])));
 				}
 				
 				//Polygon polygon = map.addPolyline();
 				PolygonOptions polygonOptions = new PolygonOptions();
 				polygonOptions.add(safepoint.get(0),safepoint.get(2),safepoint.get(1),safepoint.get(3));
 			    polygonOptions.strokeColor(Color.parseColor("#69BE28"));
 			    polygonOptions.strokeWidth(3);
 			    polygonOptions.fillColor(Color.parseColor("#69BE28"));
 			    Polygon polygon = map.addPolygon(polygonOptions);
 			}
 			 
 			
            super.onPostExecute(result);
            parserTask = new ParserTask();
            parserTask.execute(result);
            
        }
    }
 
    private class DownloadAllRouteTask extends AsyncTask<String, Void , List<String>>
    {
    	private List<String> urlList = new ArrayList<String>();
    	
        // Downloading data in non-ui thread
        @Override
        protected List<String> doInBackground(String... url) 
        {
            // For storing data from web service
            try
            {
            	HashMap<String,String> routelistMap = new HashMap<String, String>();
                routelistMap = dbcontroller.GetAllRouteLatLong();
                for (String  route : routelistMap.keySet()) 
                {
        			List<String> LatLongList = Arrays.asList(routelistMap.get(route).split(";"));
        	   		markerPoints = new ArrayList<LatLng>();
	            	for (String str : LatLongList) 
	            	{
	            		markerPoints.add(new LatLng(Double.parseDouble(str.split(",")[0]),Double.parseDouble(str.split(",")[1])));	
	        		}
	            	String url_map = getDirectionsUrl(markerPoints.get(0),markerPoints.get(markerPoints.size()-1));
	            	routeMarkerList.put(route,markerPoints.get((markerPoints.size()-1)/2));
	                urlList.add(downloadUrl(url_map));
                }
        	}
            catch(Exception e)
            {
                Log.d("Background Task",e.toString());
            }
            return urlList;
        }
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(List<String> result) 
        {
            super.onPostExecute(result);
            
            for (String url : result) 
            {
            	 parserTask = new ParserTask();
                 // Invokes the thread for parsing the JSON data
                 parserTask.execute(url);
                 
			}
        }
    }
 
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>>
    {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) 
        {
	        JSONObject jObject;
	        List<List<HashMap<String, String>>> routes = null;
            try
            {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            //new DrawSafePointsTask().execute();
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) 
        {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
         
            CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(new LatLng(32.9864304,-96.7548886))      // Sets the center of the map to location user
            .zoom(15)                   // Sets the zoom
            //.bearing(90)                // Sets the orientation of the camera to east
            //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
            .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),1000,null);
            
            // Traversing through all the routes
            for(int i=0;i<result.size();i++)
            {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.parseColor(colorlist.get(colorcounter)));
                lineOptions.geodesic(true);
                map.addPolyline(lineOptions);
                
                
            }
            
            			
			
            colorcounter++;
            if(colorcounter>15)
            	colorcounter=0;
            
            dropmarker();
            
             
         }
        public void dropmarker()
        {
        	 if(routeID.equals("All Routes"))
             {
 	           	 for (String route : routeMarkerList.keySet()) 
 	           	 {
 	           		 Marker m =map.addMarker(new MarkerOptions()
			  	     		   .title(route)
			  	     		   .position(routeMarkerList.get(route))
			  	     		   //.icon(BitmapDescriptorFactory.fromResource(R.drawable.route_marker))
			  	     		   );
 	           		 m.showInfoWindow();
 	           	}
             }
             
        }
    }

    public class DrawSafePointsTask extends AsyncTask<Void,Integer,String>
    {

		@Override
		protected String doInBackground(Void... params) 
		{
			

			return "0";
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
    	
    }
    
	private class FetchCabLocationTask extends AsyncTask<Void,List<VehicleInfo>,List<VehicleInfo>>
	{
		@Override
		protected List<VehicleInfo> doInBackground(Void... params) 
		{
			List<VehicleInfo> currentCabLocations = new ArrayList<VehicleInfo>();
			currentCabLocations = dbcontroller.GetLiveVehicleLocation(routeID);
			return currentCabLocations;
		}
		
		@Override
		protected void onPostExecute(List<VehicleInfo> result) 
		{
			super.onPostExecute(result);
			//Toast.makeText(getActivity(), "Size: "+result.size(), Toast.LENGTH_SHORT).show();
			for (Marker marker : markerlist) 
			{	
				marker.remove();
			}
			markerlist.clear();
			
			if(result.size()==0)
			{
				if(FirstWarningFlg)
				{
					Toast.makeText(getActivity(), "There are no cabs available in "+routeID, Toast.LENGTH_SHORT).show();
					FirstWarningFlg= false;
				}
			}
			for (VehicleInfo latLng : result) 
			{
				FirstWarningFlg= false;
				Location prevLoc = new Location("dummy");
				Location currLoc = new Location("dummy");
				
				prevLoc.setLatitude(latLng.getPrevLocationInfo().latitude);
				prevLoc.setLongitude(latLng.getPrevLocationInfo().longitude);
				
				currLoc.setLatitude(latLng.getLocationInfo().latitude);
				currLoc.setLongitude(latLng.getLocationInfo().longitude);
				
				float rotation = prevLoc.bearingTo(currLoc);
				//Toast.makeText(getActivity(), "Rotation : "+rotation, Toast.LENGTH_SHORT).show();
				
				//String Title = new String();
				//if(latLng.getSeatAvailable()>5)
				//	Title = "You have good chance of getting it!!";
				//else if(latLng.getSeatAvailable()>=2)
				//	Title = "You can still try!!";
				//else if(latLng.getSeatAvailable()>=0)
				//	Title = "Try your Luck someone might jump off!!";
				Log.i("FETCH", latLng.getSeatAvailable()+"");
				
				double percentage = ((double)latLng.getSeatAvailable()/(double)latLng.getTotalCapacity())*100;
				percentage=new BigDecimal(percentage).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
				Marker marker = map.addMarker(new MarkerOptions()
	     		   .title("Seats Available : "+latLng.getSeatAvailable())
	     		   .snippet(percentage + "% Free")
	     		   .position(latLng.getLocationInfo())
	     		   .rotation(rotation)
	     		   .anchor(0.5f, 0.5f)
	     		   .flat(true)
	     		   .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car1)));
	     		   //.draggable(true)
	     		   //.snippet("The most populous city in USA.")
	     		  //map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 17));
				 //marker.showInfoWindow();
				 markerlist.add(marker);
			
				 CameraPosition cameraPosition = new CameraPosition.Builder()
	             .target(latLng.getLocationInfo())      // Sets the center of the map to location user
	             .zoom(18)                   // Sets the zoom
	             //.bearing(rotation)                // Sets the orientation of the camera to east
	             //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
	             .build();  
				 map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	        }	
		}
	}
	
		
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
			if (isChecked) 
		    {
				Toast.makeText(getActivity(),"You are Subscribed", Toast.LENGTH_SHORT).show();
				llSubscribe.setBackgroundColor(Color.RED);
				swSubscribe.setBackgroundColor(Color.RED);
				checkFlgOverride=false;
				if(pref.getString("SubscribedRoute", "").equals(""))
				{	
					editor.putString("SubscribedRoute",routeID);
					editor.putString("SendDistanceNotification","Yes");
					editor.commit();
					swSubscribe.setText("Subscribed to "+pref.getString("SubscribedRoute", routeID));
					in = new Intent(getActivity(),RiderService.class);
					getActivity().startService(in);
				}
			}
			else
			{
				if(!checkFlgOverride)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Comet Ride")
					   .setMessage("You are about to UnSubscibe Route "+pref.getString("SubscribedRoute", "")+", Do you Still want to").setCancelable(true)
					   .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
					   {
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								llSubscribe.setBackgroundColor(Color.parseColor("#E98300"));
								swSubscribe.setBackgroundColor(Color.parseColor("#E98300"));
								getActivity().stopService(new Intent(getActivity(), RiderService.class));
								editor.putString("ServiceStatus", "NOT RUNNING");
								editor.commit();
								swSubscribe.setText("Subscribe");
								editor.putString("SubscribedRoute","");
								editor.commit();
							}
					})
					   .setNegativeButton("No", new DialogInterface.OnClickListener() 
					   {
						    @Override
						    public void onClick(DialogInterface dialog, int arg1) {
						    	checkFlgOverride = true;
						        swSubscribe.setChecked(true);
						    }
					});
					
					AlertDialog alert = builder.create();
					alert.show();
			}
			else
			{
				checkFlgOverride=false;
			}
		}
	}
	
	//######################################### UNUSED FUNCITONS ##############################################//
	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}


}


