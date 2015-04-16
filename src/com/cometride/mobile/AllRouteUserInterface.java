package com.cometride.mobile;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class AllRouteUserInterface extends android.app.Fragment implements LocationListener
{
	public static GoogleMap map;
	public static MapView mapView;
	RiderDatabaseController dbcontroller;
	
	public AllRouteUserInterface() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		//super.onCreateView(inflater, container, savedInstanceState);
		if(container==null){return null;}
		View myFragmentView =null;
		try
		{	
			myFragmentView = inflater.inflate(R.layout.fragment_route2_user_interface,container, false);
			//myFragmentView = inflater.inflate(R.layout.fragment_route1_user_interface, container, false);
			//mLinearLayout = (FrameLayout) inflater.inflate(R.layout.fragment_route1_user_interface,container, false);
		
		mapView = (MapView) myFragmentView.findViewById(R.id.map_route2);
		mapView.onCreate(savedInstanceState);
        mapView.onResume();
        map = mapView.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        // Enable / Disable my location button
        map.getUiSettings().setMyLocationButtonEnabled(true);
        // Enable / Disable Compass icon
        map.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        map.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        map.getUiSettings().setZoomGesturesEnabled(true);
        MapsInitializer.initialize(this.getActivity());
		}
		catch(Exception ex)
		{
			
		}
		Load_Location_Zoom();
		return myFragmentView;
	}
	
	@Override
	public void onLocationChanged(Location location) 
	{
		LatLng Chennai = new LatLng(13.0475604,80.2089535);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(Chennai, 5));
		map.addMarker(new MarkerOptions()
		    .title("sydney")
		    .snippet("The most populous city in Australia.")
		    .position(Chennai));

		
		//double latitude = location.getLatitude();
       // double longitude = location.getLongitude();
       // LatLng latLng = new LatLng(latitude, longitude);
 
       // map.animateCamera(CameraUpdateFactory.newLatLngZoom(
         //       new LatLng(location.getLatitude(), location.getLongitude()), 13));

        //CameraPosition cameraPosition = new CameraPosition.Builder()
        //.target(latLng)      // Sets the center of the map to location user
        //.zoom(18)                   // Sets the zoom
        //.bearing(90)                // Sets the orientation of the camera to east
        //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
        //.build();                   // Creates a CameraPosition from the builder
       // map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
       //map.animateCamera(CameraUpdateFactory.zoomTo(15));
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
	
	public void Initialize()
    {
		dbcontroller = new RiderDatabaseController(getActivity());
    	
    }
	
    
}
