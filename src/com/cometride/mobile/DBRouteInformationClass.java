package com.cometride.mobile;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "RouteInformation")
public class DBRouteInformationClass 
{
	private String routeid;
	private double latitude;
	private double longitude;
	
	@DynamoDBHashKey (attributeName = "RouteID")
	public String getRouteid() { return routeid;	}
	public void setRouteid(String routeid) {this.routeid = routeid; }
	
	@DynamoDBAttribute (attributeName = "Latitude" )
	public double getLatitude() {	return latitude;	}
	public void setLatitude(double latitude) { this.latitude = latitude; }
	
	@DynamoDBAttribute (attributeName = "Longitude" )
	public double getLongitude() {	return longitude;	}
	public void setLongitude(double longitude) { this.longitude = longitude; }
	
}
