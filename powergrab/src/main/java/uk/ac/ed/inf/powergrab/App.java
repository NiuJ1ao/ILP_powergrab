package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;

import com.mapbox.geojson.*;


/**
 * App is the main part of this application, like a interface. It contains the informations about stations and the drone.
 * I used this function directly instead of creating a new class called map. It is because I think App class can represent the map. 
 * @author s1740055
 */
public class App {
	protected static List<ChargingStation> stations = new ArrayList<ChargingStation>();
	private static Drone drone;
	List<Feature> featuresList = null;
	
    public static void main(String[] args) throws Exception {
    	// Parse arguments.
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	Position initDronePos = new Position(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
    	long seed = Long.parseLong(args[5]);
    	String droneType = args[6].toLowerCase();
    	
    	// Run APP
    	new App(day, month, year, initDronePos, seed, droneType);
    }
    
    /**
     * The constructor of App class. It initiates the game and also runs the drone.
     * @param day
     * @param month						The date of map.
     * @param year
     * @param initDronePos				The position where the drone starts.
     * @param seed						The random seed.
     * @param droneType					The type of drone, either stateless or stateful.
     * @throws IOException 				Throws exception if the file does not exist or network connection fails.
     * @throws ClassNotFoundException 	If the drone type typed in is neither stateful nor stateless.
     */
    public App(String day, String month, String year, Position initDronePos, long seed, String droneType) throws IOException, ClassNotFoundException {
    	long startTime = System.currentTimeMillis();
    	
    	// Initiate stations
		downloadMap(year, month, day);
		
		// Total coins
		double totalCoins = getTotalCoins();
    	
    	// Initiate drone
    	String txtFileName = String.format("%s-%s-%s-%s.txt", droneType, day, month, year); 
    	PrintWriter txtWriter = new PrintWriter(txtFileName, "UTF-8");
    	if (droneType.equals("stateless")) {
    		drone = new StatelessDrone(initDronePos, seed, txtWriter);
    	} else if (droneType.equals("stateful")) {
    		drone = new StatefulDrone(initDronePos, seed, txtWriter);
    	} else {
    		txtWriter.close();
    		throw new ClassNotFoundException("Drone type is not found.");
    	}
    	
    	// Create and write to geojson file.
    	String geojsonFileName = String.format("%s-%s-%s-%s.geojson", droneType, day, month, year);
    	PrintWriter jsonWriter = new PrintWriter(geojsonFileName, "UTF-8");
    	
    	// Run the drone.
    	run(txtWriter, jsonWriter);
    	
    	// Evaluate the performance.
    	long endTime = System.currentTimeMillis();
    	if (drone.coins/totalCoins < 1 || endTime - startTime>1000) {
    		System.out.println("==== "+droneType+" drone is running in "+day+" "+month+" "+year+" "+"====");
    		System.out.println("Coins ratio: " + drone.coins/totalCoins);
    		System.out.println("Elapsed time in milliseconds: " + (endTime - startTime));
    	}
    }
    
    /**
     * For evaluation performance, check how many coins the drone got.
     * @return The total number of coins in the map.
     */
    private double getTotalCoins() {
    	double result = 0;
    	for (ChargingStation s : stations) {
    		if (s.type == ChargingStation.LIGHTHOUSE) {
    			result += s.coins;
    		}
    	}
    	return result;
    }
    
    /**
     * This function runs the drone and adds the feature(movements) returned by the drone to GeoJSON file.
     * @param txtWriter   It is for recording the status changes of the drone.
     * @param jsonWriter  It is for creating GeoJSON file after execution.
     */
    private void run(PrintWriter txtWriter, PrintWriter jsonWriter) {
    	// Run the drone.
    	Feature f = drone.strategy();
		txtWriter.close();
		
		// Add feature to exist features list and create a feature collection.
    	featuresList.add(f);
    	FeatureCollection fc = FeatureCollection.fromFeatures(featuresList);
    	
    	// Write feature collection to geojson file.
		jsonWriter.println(fc.toJson());
		jsonWriter.close();
    }
    
    /**
     * This function download the map according to the date.
     * @param year
     * @param month		   Those three are the date of map which is trying to get.
     * @param day
     * @throws IOException Throws exception if the file does not exist or network connection fails.
     */
    private void downloadMap(String year, String month, String day) throws IOException  {
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson", 
    										year, month, day);
    	
    	URL mapUrl = new URL(mapString);
    	HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
    	conn.setReadTimeout(10000);
    	conn.setConnectTimeout(15000);
    	conn.setRequestMethod("GET");
    	conn.setDoInput(true);
    	conn.connect();
    	
    	InputStream in = conn.getInputStream();
    	StringBuilder mapBuilder = new StringBuilder();
    	
    	// Parse InputStream to String
    	int c = 0;
    	try (Reader reader = new BufferedReader(new InputStreamReader(in))) {
    		while ((c = reader.read()) != -1) {
    			mapBuilder.append((char) c);
    		}
    	}
    	String mapSource = mapBuilder.toString();
    	
    	parseSource(mapSource);
    }
    
    /**
     * This parses the string and gets informations about stations.
     * @param mapSource The JSON data about the map
     */
    private void parseSource(String mapSource) {
	    FeatureCollection fc = FeatureCollection.fromJson(mapSource);
    	featuresList  = fc.features();
		double coins;
		double power;
		Point point;
		
		// Create stations and add them to a list.
    	for (final Feature f : featuresList) {
    		coins = f.getProperty("coins").getAsFloat();
    		power = f.getProperty("power").getAsFloat();
    		point = (Point) f.geometry();
    		Position p = new Position(point.latitude(), point.longitude());
    		ChargingStation station = new ChargingStation(coins, power, p);
    		stations.add(station);
    	}	
    }
}
