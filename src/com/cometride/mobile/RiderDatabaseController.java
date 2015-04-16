package com.cometride.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


public class RiderDatabaseController 
{
	CognitoCachingCredentialsProvider credentialsProvider;
	AmazonDynamoDBClient ddbClient;
	DynamoDBMapper mapper;
		
	public RiderDatabaseController(Context mcontext)
	{
		credentialsProvider = new CognitoCachingCredentialsProvider(
		    mcontext, // Context
		    "us-east-1:12837181-a44f-4651-a188-0204c5a59553", // Identity Pool ID
		    Regions.US_EAST_1 // Region
		);		
		
		ddbClient = new AmazonDynamoDBClient(credentialsProvider);
		mapper = new DynamoDBMapper(ddbClient);
	}
	
	public void UpdateLiveVehicleInformation(String RouteID,int vehicleID,double Latitude, double Longitude,int TotalCapacity,int CurrentRiders,int TotalRiders)
	{
		DBRouteInformationClass Route = new DBRouteInformationClass();
        mapper.save(Route);
    }
	
	public List<String> GetRouteInfo()
	{
		List<String> dbRouteResult = new ArrayList<String>();
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		PaginatedScanList<DBRouteInformationClass> result = mapper.scan(DBRouteInformationClass.class, scanExpression);
		
		for (DBRouteInformationClass routeInformation : result) 
		{
			String routeID_temp = routeInformation.getRouteid();
			if(!dbRouteResult.contains(routeID_temp))
			{
				dbRouteResult.add(routeID_temp);
			}
		}
		
		Collections.sort(dbRouteResult);
		return dbRouteResult;
	}
	
	public List<String> GetVehicleInformation(String RouteID)
	{
		List<String> dbLatLongResult = new ArrayList<String>();
		
		return dbLatLongResult;
	}

}
