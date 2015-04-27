package com.cometride.mobile;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "SafePointInformation")
public class DBSafePointInformationClass 
{
	private String routeID;
	private String safepointid;
	private String latlong;
	
	@DynamoDBHashKey (attributeName = "RouteID")
	public String getRouteID() {return routeID;	}
	public void setRouteID(String routeID) {this.routeID = routeID;}
	
	@DynamoDBRangeKey (attributeName = "SafePointID")
	public String getSafepointid() {return safepointid;}
	public void setSafepointid(String safepointid) {this.safepointid = safepointid;}
	
	@DynamoDBAttribute (attributeName = "LatLong" )
	public String getLatlong() {return latlong;}
	public void setLatlong(String latlong) {this.latlong = latlong;}
	
}
