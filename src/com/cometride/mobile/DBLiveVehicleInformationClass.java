package com.cometride.mobile;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "LiveVehicleInformation")
public class DBLiveVehicleInformationClass 
{
    private String routeid;
    private int vehicleid;
    private double vehiclelat;
    private double vehiclelong;
    private int currentriders;
    private int vehicletotalcapacity;
    private int totalriders;
    
    @DynamoDBHashKey (attributeName = "RouteID")
    public String getRouteID() {return routeid;}
    public void setRouteID(String routeid) {this.routeid = routeid;}
    
    @DynamoDBRangeKey (attributeName = "VehicleID")
    public int getVehicleID() {return vehicleid; }
	public void setVehicleID(int vehicleid) {this.vehicleid = vehicleid;}
	
	@DynamoDBAttribute (attributeName = "VehicleLat")
	public double getVehicleLat() {return vehiclelat;	}
	public void setVehicleLat(double vehiclelat) {this.vehiclelat = vehiclelat;}
	
	@DynamoDBAttribute (attributeName = "VehicleLong")
	public double getVehicleLong() {return vehiclelong;}
	public void setVehicleLong(double vehiclelong) {this.vehiclelong = vehiclelong;}
	
	@DynamoDBAttribute (attributeName = "VehicleTotalCapacity")
	public int getVehicleTotalCapacity() {return vehicletotalcapacity;	}
	public void setVehicleTotalCapacity(int vehicletotalcapacity) {this.vehicletotalcapacity = vehicletotalcapacity;}
	
	@DynamoDBAttribute (attributeName = "CurrentRiders")
	public int getCurrentRiders() {	return currentriders;	}
	public void setCurrentRiders(int currentriders) {this.currentriders = currentriders;	}
	
	@DynamoDBAttribute (attributeName = "TotalRiders")
	public int getTotalRiders() {return totalriders;}
	public void setTotalRiders(int totalriders) {this.totalriders = totalriders;}
	
 }