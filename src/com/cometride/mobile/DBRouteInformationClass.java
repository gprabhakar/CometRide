package com.cometride.mobile;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "RouteInformation")
public class DBRouteInformationClass 
{
	private String routeid;
	private String routeName;
	
	@DynamoDBHashKey (attributeName = "RouteID")
	public String getRouteid() { return routeid;	}
	public void setRouteid(String routeid) {this.routeid = routeid; }
	
	@DynamoDBAttribute (attributeName = "RouteName" )
	public String getRouteName() {	return routeName;	}
	public void setRouteName(String routeName) { this.routeName = routeName; }
}
