package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import com.mapbox.geojson.*;


/**
 *
 *
 */
public class App {
	//private String mapSource;
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
    	
    	/**
    	 * This is for evaluating performance
    	 */
//    	String year;
//    	String month;
//    	Position init = new Position(55.944425, -3.188396);
//    	int maxDay = 31;
//    	int[] m30 = {4,6,9,11};
//    	for (int y=2019; y<=2020; y++) {
//    		year = "" + y;
//    		for (int m=1; m<=12; m++) {
//    			if (m < 10) {
//    				month = "0" + m;
//    			} else {
//    				month = "" + m;
//    			}
//    			if (y == 2019 && m == 2) {
//    				maxDay = 28;
//    			} else if (y == 2020 && m == 2) {
//    				maxDay = 29;
//    			} else {
//    				for (int i=0; i<4; i++) {
//    					if (m == m30[i]) {
//    						maxDay = 30;
//    						break;
//    					}
//    				}
//    			}
//    			
//    			for (int d=1; d<=maxDay; d++) {
//			    	String day;
//					if (d < 10) {
//        				day = "0" + d;
//        			} else {
//        				day = "" + d;
//        			}
//					new App(day, month, year, init, 5678, "stateful");
//    			}
//    		}
//    	}
    }
    
    public App(String day, String month, String year, Position initDronePos, long seed, String droneType) throws Exception {
    	System.out.println("==== "+droneType+" drone is running in "+day+" "+month+" "+year+" "+"====");
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
    		System.out.println("Type not found");
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
    		System.out.println("OOPS");
    		System.out.println("Coins ratio: " + drone.coins/totalCoins);
    		System.out.println("Elapsed time in milliseconds: " + (endTime - startTime));
    	}
    }
    
    private double getTotalCoins() {
    	double result = 0;
    	for (ChargingStation s : stations) {
    		if (s.type == ChargingStation.LIGHTHOUSE) {
    			result += s.coins;
    		}
    	}
    	return result;
    }
    
    private void run(PrintWriter txtWriter, PrintWriter jsonWriter) {
    	Feature f = null;
    	
    	// Run the drone.
		f = drone.strategy();
		txtWriter.close();
		
		// Add feature to features list and create a feature collection.
    	featuresList.add(f);
    	FeatureCollection fc = FeatureCollection.fromFeatures(featuresList);
    	
    	// Append feature collection to geojson file.
		jsonWriter.println(fc.toJson());
		jsonWriter.close();
    }
    
    private void downloadMap(String year, String month, String day) throws Exception {
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
    	int c = 0;
    	try (Reader reader = new BufferedReader(new InputStreamReader(in))) {
    		while ((c = reader.read()) != -1) {
    			mapBuilder.append((char) c);
    		}
    	}
    	
    	String mapSource = mapBuilder.toString();
    	parseSource(mapSource);
    }
    
    private void parseSource(String mapSource) {
	    FeatureCollection fc = FeatureCollection.fromJson(mapSource);
    	featuresList  = fc.features();
    	String id = null;
		double coins;
		double power;
		String icon = null;
		String brightness = null;
		Point point;
		
		// Create stations and add them to a list.
    	for (final Feature f : featuresList) {
			id = f.getProperty("id").getAsString();
    		coins = f.getProperty("coins").getAsFloat();
    		power = f.getProperty("power").getAsFloat();
    		icon = f.getProperty("marker-symbol").getAsString();
    		brightness = f.getProperty("marker-color").getAsString();
    		point = (Point) f.geometry();
    		Position p = new Position(point.latitude(), point.longitude());
    		ChargingStation station = new ChargingStation(id, coins, power, icon, brightness, p);
    		stations.add(station);
    	}	
    }
}
