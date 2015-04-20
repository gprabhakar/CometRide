package com.cometride.mobile;

import com.google.android.gms.maps.model.LatLng;

public class VehicleInfo 
{
	private LatLng LocationInfo;
	public LatLng getLocationInfo() {
		return LocationInfo;
	}
	public void setLocationInfo(LatLng locationInfo) {
		LocationInfo = locationInfo;
	}
	public int getSeatAvailable() {
		return seatAvailable;
	}
	public void setSeatAvailable(int seatAvailable) {
		this.seatAvailable = seatAvailable;
	}
	private int seatAvailable;
}
