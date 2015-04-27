package com.cometride.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.google.android.gms.maps.model.LatLng;


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
	
	public List<String> GetAllRoute()
	{
		List<String> allRoute = new ArrayList<String>();
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		PaginatedScanList<DBRouteInformationClass> result = mapper.scan(DBRouteInformationClass.class, scanExpression);
		for (DBRouteInformationClass dbRouteInformationClass : result) {
			if(!allRoute.contains(dbRouteInformationClass.getRouteid())){
				allRoute.add(dbRouteInformationClass.getRouteid());
			}
		}
		return allRoute;
	}
	
	//
	public HashMap<String,String> GetAllRouteLatLong()
	{
		HashMap<String,String> dbRouteResult= new HashMap<String, String>();
		
		//List<String> dbRouteResult = new ArrayList<String>();
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		PaginatedScanList<DBRouteInformationClass> result = mapper.scan(DBRouteInformationClass.class, scanExpression);
		String routeID_temp = "";
		String routeID = "";
		String LatLong="";
		
		for (DBRouteInformationClass routeInformation : result) 
		{
			routeID = routeInformation.getRouteid();
			if(!routeID.equals(routeID_temp))
			{ 
				if(!LatLong.equals(""))
				{
					LatLong=LatLong.replaceAll("^;","").replaceAll(";$","");
					dbRouteResult.put(routeID_temp, LatLong);
					routeID_temp= routeID;
					LatLong= "";
				}
				else
				{
					LatLong= routeInformation.getLatitude()+","+routeInformation.getLongitude();
					routeID_temp= routeID;
				}
			}
			else
			{
				LatLong= LatLong+";"+routeInformation.getLatitude()+","+routeInformation.getLongitude();
				routeID_temp= routeID;
			}
		}
		LatLong = LatLong.replaceAll("^;","").replaceAll(";$","");
		dbRouteResult.put(routeID_temp, LatLong);
		
		return dbRouteResult;
	}
	
	public List<String> GetRouteSpecificLatLong(String RouteID)
	{
		List<String> LatLong = new ArrayList<String>();
		DBRouteInformationClass route = new DBRouteInformationClass();
		route.setRouteid(RouteID);
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
        	.withHashKeyValues(route)
        	.withConsistentRead(false);

		PaginatedQueryList<DBRouteInformationClass> result = mapper.query(DBRouteInformationClass.class, queryExpression);
		for (DBRouteInformationClass dbRouteInformationClass : result) 
		{
			LatLong.add(dbRouteInformationClass.getLatitude()+","+dbRouteInformationClass.getLongitude());
		}
		return LatLong;
	}
	
	public List<VehicleInfo> GetLiveVehicleLocation(String routeID)
	{
		List<VehicleInfo> dbLatLongResult = new ArrayList<VehicleInfo>();
		
		//List<LatLng> dbLatLongResult = new ArrayList<LatLng>();
		DBLiveVehicleInformationClass vehicleInfo = new DBLiveVehicleInformationClass();
		vehicleInfo.setRouteID(routeID);
		
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
        	.withHashKeyValues(vehicleInfo)
        	.withConsistentRead(false);

		PaginatedQueryList<DBLiveVehicleInformationClass> result = mapper.query(DBLiveVehicleInformationClass.class, queryExpression);
		
		for (DBLiveVehicleInformationClass dbLiveVehicleInfo : result) 
		{
			VehicleInfo temp_info = new VehicleInfo();
			temp_info.setLocationInfo(new LatLng(dbLiveVehicleInfo.getVehicleLat(),dbLiveVehicleInfo.getVehicleLong()));
			temp_info.setPrevLocationInfo(new LatLng(dbLiveVehicleInfo.getPrevlat(),dbLiveVehicleInfo.getPrevlong()));
			temp_info.setSeatAvailable(dbLiveVehicleInfo.getVehicleTotalCapacity()-dbLiveVehicleInfo.getCurrentRiders());
			temp_info.setTotalCapacity(dbLiveVehicleInfo.getVehicleTotalCapacity());
			dbLatLongResult.add(temp_info);
		}
		
		return dbLatLongResult;
	}
	
	public List<DBLiveVehicleInformationClass> GetLiveVehicleAvailability(String routeID)
	{
		int Capacity = 0;
		//List<LatLng> dbLatLongResult = new ArrayList<LatLng>();
		DBLiveVehicleInformationClass vehicleInfo = new DBLiveVehicleInformationClass();
		vehicleInfo.setRouteID(routeID);
		
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
        	.withHashKeyValues(vehicleInfo)
        	.withConsistentRead(false);

		PaginatedQueryList<DBLiveVehicleInformationClass> result = mapper.query(DBLiveVehicleInformationClass.class, queryExpression);
		
		return result;
	}
	
	public List<String> GetSafePointInfo(String routeid)
	{
		List<String> latlongList = new ArrayList<String>();
		DBSafePointInformationClass safepointInfo = new DBSafePointInformationClass();
		safepointInfo.setRouteID(routeid);
		
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
        	.withHashKeyValues(safepointInfo)
        	.withConsistentRead(false);

		PaginatedQueryList<DBSafePointInformationClass> result = mapper.query(DBSafePointInformationClass.class, queryExpression);
		for (DBSafePointInformationClass dbSafePointInformationClass : result) 
		{
			latlongList.add(dbSafePointInformationClass.getLatlong());
		}

		return latlongList;
	}

}


