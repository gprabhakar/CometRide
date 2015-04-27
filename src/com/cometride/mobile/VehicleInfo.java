package com.cometride.mobile;

import com.google.android.gms.maps.model.LatLng;

public class VehicleInfo 
{
	private LatLng LocationInfo;
	private LatLng PrevLocationInfo;
	
	private int seatAvailable;
	private int totalCapacity;
	
	public int getTotalCapacity() {return totalCapacity;}
	public void setTotalCapacity(int totalCapacity) {this.totalCapacity = totalCapacity;}
	
	public LatLng getLocationInfo() {return LocationInfo;}
	public void setLocationInfo(LatLng locationInfo) {LocationInfo = locationInfo;}
	
	public LatLng getPrevLocationInfo() {return PrevLocationInfo;}
	public void setPrevLocationInfo(LatLng prevLocationInfo) {PrevLocationInfo = prevLocationInfo;}
	
	public int getSeatAvailable() {return seatAvailable;}
	public void setSeatAvailable(int seatAvailable) {this.seatAvailable = seatAvailable;}
	
}
