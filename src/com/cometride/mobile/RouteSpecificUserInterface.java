package com.cometride.mobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class RouteSpecificUserInterface extends android.app.Fragment implements LocationListener,OnClickListener
{
	private static GoogleMap map;
	private static MapView mapView;
	private String routeID;
	private ArrayList<LatLng> markerPoints;
	private View myFragmentView =null;
	private List<String> LatLongList = new ArrayList<String>() ;
	private RiderDatabaseController dbcontroller ;
	private DownloadTask downloadTask;
	private ParserTask parserTask;
	private FetchCabLocationTask fetchTask ;
	private List<Marker> markerlist = new ArrayList<Marker>();
	
	public RouteSpecificUserInterface() 
	{	}

	public RouteSpecificUserInterface(String routeID) 
	{ 
		this.routeID = routeID;  // Initializes the RouteID to fetch the appropriate map information to be fetched from the Database  	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		if(container==null)
		{
			return null; 
		}
		try
		{	
			myFragmentView = inflater.inflate(R.layout.fragment_route1_user_interface,container, false);
			mapView = (MapView) myFragmentView.findViewById(R.id.map_route1);
			mapView.onCreate(savedInstanceState);
			Initialize();
		}
		catch(Exception ex)
		{		
			//TO avoid Inflater Exception.
		}
		return myFragmentView;
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		//Initialize();
	}
	
	@Override
	public void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		if(fetchTask!=null)
		{
			fetchTask.cancel(true);
		}
	}
	@Override
	public void onClick(View v) 
	{
		
	}
	
	public void Initialize()
    {
		dbcontroller = new RiderDatabaseController(getActivity());
		mapView.onResume();
        map = mapView.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);        // Enable / Disable my location button
        map.getUiSettings().setMyLocationButtonEnabled(true);        // Enable / Disable Compass icon
        map.getUiSettings().setCompassEnabled(false);        // Enable / Disable Rotate gesture
        map.getUiSettings().setRotateGesturesEnabled(true);        // Enable / Disable zooming functionality
        map.getUiSettings().setZoomGesturesEnabled(true);
        MapsInitializer.initialize(this.getActivity());
        markerPoints = new ArrayList<LatLng>();
        
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);
        if(location!=null)
        {
            onLocationChanged(location);
        }
		LoadRouteSpecificMap();
		
		//Load_Location_Zoom();
    }
	
	@Override
	public void onLocationChanged(Location location) 
	{
	   // double latitude = location.getLatitude();
       // double longitude = location.getLongitude();
       // LatLng latLng = new LatLng(latitude, longitude);
       // map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
       // CameraPosition cameraPosition = new CameraPosition.Builder()
       //.target(latLng)      // Sets the center of the map to location user
       //.zoom(18)                   // Sets the zoom
       //.bearing(90)                // Sets the orientation of the camera to east
       //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
       //.build();                   // Creates a CameraPosition from the builder
       // map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
       // map.animateCamera(CameraUpdateFactory.zoomTo(15));
       // map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));	
	}
	
	public void Load_Location_Zoom()
	{
    	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
    	if(status!=ConnectionResult.SUCCESS)
    	{
    		int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), requestCode);
            dialog.show();
 
        }
    	else 
    	{
    		map.setMyLocationEnabled(true);
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);
            if(location!=null)
            {
                onLocationChanged(location);
            }
            //locationManager.requestLocationUpdates(provider, 20000, 0, (android.location.LocationListener)this);
        }
	}
	
	
	public void LoadRouteSpecificMap()
	{	
        downloadTask = new DownloadTask();
        downloadTask.execute();		    
	}
	
	private String getDirectionsUrl(LatLng origin,LatLng dest)
	{
	        // Origin of route
	        String str_origin = "origin="+origin.latitude+","+origin.longitude;
	        // Destination of route
	        String str_dest = "destination="+dest.latitude+","+dest.longitude;
	        // Sensor enabled
	        String sensor = "sensor=false";
	        // Waypoints
	        String waypoints = "";
	        
	        for(int i=1;i<markerPoints.size()-1;i++)
	        {
	            LatLng point  =  markerPoints.get(i);
	            if(i==1)
	                waypoints = "waypoints=";
	            waypoints += point.latitude + "," + point.longitude + "|";
	        }
	        // Building the parameters to the web service
	        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;
	        // Output format
	        String output = "json";
	        // Building the url to the web service
	        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
	        return url;
	 }
	
	 /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();            // Connecting to url
            urlConnection.connect();            // Reading data from url
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
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>
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
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) 
        {
            super.onPostExecute(result);
            
            if(downloadTask.getStatus() != AsyncTask.Status.RUNNING)
            {
            	downloadTask.cancel(true);
            }
            parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
 
    /** A class to parse the Google Places in JSON format */
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
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) 
        {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
 
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
                lineOptions.width(15);
                lineOptions.color(Color.parseColor("#00CCFF"));
                lineOptions.geodesic(true);
            }
 
             // Drawing polyline in the Google Map for the i-th route
             map.addPolyline(lineOptions);
             LatLng marker = markerPoints.get(0);
             
             //CameraPosition cameraPosition = new CameraPosition.Builder()
             //.target(marker)      // Sets the center of the map to location user
             //.zoom(15)                   // Sets the zoom
             //.bearing(90)                // Sets the orientation of the camera to east
             //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
             //.build();                   // Creates a CameraPosition from the builder
             //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000,null);
             //map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
             //map.moveCamera(CameraUpdateFactory.newLatLngBounds(AUSTRALIA,10));
             //parserTask.cancel(true);
             //downloadTask.cancel(true);
             if(parserTask.getStatus() != AsyncTask.Status.RUNNING)
             {
            	 parserTask.cancel(true);
             }
             fetchTask = new FetchCabLocationTask();
             fetchTask.execute();
         }
    }

	private class FetchCabLocationTask extends AsyncTask<Void,List<VehicleInfo>,Void>
	{
		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Void... params) 
		{
			List<VehicleInfo> currentCabLocations = new ArrayList<VehicleInfo>();
			while(true)
			{	
				if (isCancelled())  
			         break;
				
				currentCabLocations = dbcontroller.GetLiveVehicleLocation(routeID);
				publishProgress(currentCabLocations);
				try
				{
					Thread.sleep(5000);
				}
				catch(Exception e){}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(List<VehicleInfo>... values) 
		{
			for (Marker marker : markerlist) 
			{
				marker.remove();
			}
			//super.onProgressUpdate(values);
			for (VehicleInfo latLng : values[0]) 
			{
				String Title = new String();
				if(latLng.getSeatAvailable()>5)
					Title = "You have good chance of getting it!!";
				else if(latLng.getSeatAvailable()>=2)
					Title = "You can still try!!";
				else if(latLng.getSeatAvailable()>=0)
					Title = "Try your Luck someone might jump off!!";
				Log.i("FETCH", latLng.getSeatAvailable()+"");
				Marker marker = map.addMarker(new MarkerOptions()
	     		   .title("Seats Available : "+latLng.getSeatAvailable())
	     		   .snippet(Title)
	     		   .position(latLng.getLocationInfo())
	     		   .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));
	     		   //.draggable(true)
	     		   //.snippet("The most populous city in USA.")
	     		  //map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 17));
				 marker.showInfoWindow();
				 markerlist.add(marker);
				 
				 CameraPosition cameraPosition = new CameraPosition.Builder()
	             .target(latLng.getLocationInfo())      // Sets the center of the map to location user
	             .zoom(16)                   // Sets the zoom
	             //.bearing(90)                // Sets the orientation of the camera to east
	             //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
	             .build();  
				 map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	        }	
		}
	}
	
	@Override
	public void onDestroyView() 
	{
		// TODO Auto-generated method stub
		super.onDestroyView();
		 if(fetchTask.getStatus() != AsyncTask.Status.RUNNING)
	     {
			 fetchTask.cancel(true);
	     }
		if(!fetchTask.isCancelled())
			fetchTask.cancel(true);
		
	    try {
	        trimCache(getActivity());
	       // Toast.makeText(this,"onDestroy " ,Toast.LENGTH_LONG).show();
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}

	public static void trimCache(Context context) {
	    try 
	    {
	       File dir = context.getCacheDir();
	       if (dir != null && dir.isDirectory()) {
	          deleteDir(dir);
	       }
	    } catch (Exception e) {
	       // TODO: handle exception
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
	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
	
	
	
}
