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
 *
 *
 */
public class App {
	
	private String mapSource = new String();
	protected static List<ChargingStation> stations = new ArrayList<ChargingStation>();
	protected static Drone testDrone;
	List<Feature> featuresList = null;
	
    public static void main( String[] args ) {
    	long startTime = System.currentTimeMillis();
    	
    	// Parse arguments.
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	Position initDronePos = new Position(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
    	long seed = Long.parseLong(args[5]);
    	String droneType = args[6].toLowerCase();
    	
    	// Initialize drone and APP.
    	App test = new App();
    	if (droneType.equals("stateless")) {
    		testDrone = new StatelessDrone(initDronePos, seed);
    	} else if (droneType.equals("stateful")) {
    		testDrone = new StatefulDrone(initDronePos, seed);
    	} else {
    		System.out.println("Type not found");
    		return;
    	}
    	
    	// Download Map and parse it.
    	try {
			test.downloadMap(year, month , day);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	test.parseSource();
    	
    	// Test stateless drone;
    	Feature f = null;
		try {
			f = testDrone.strategy();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	test.featuresList.add(f);
    	FeatureCollection fc = FeatureCollection.fromFeatures(test.featuresList);
    
    	String fileName = String.format("%s-%s-%s-%s.geojson", droneType, day, month, year); 	
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println(fc.toJson());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			testDrone.move(Direction.S);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(testDrone.closestStation.type + ": " + testDrone.closestStation.getId());
    	
		long endTime = System.currentTimeMillis();
		System.out.println("Elapsed time in milliseconds: " + (endTime - startTime));
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
    	mapSource = mapBuilder.toString();
    }
    
    private void parseSource() {
	    FeatureCollection fc = FeatureCollection.fromJson(mapSource);
    	featuresList  = fc.features();
    	for (final Feature f : featuresList) {
			String id = f.getProperty("id").getAsString();
    		double coins = f.getProperty("coins").getAsFloat();
    		double power = f.getProperty("power").getAsFloat();
    		String icon = f.getProperty("marker-symbol").getAsString();
    		String brightness = f.getProperty("marker-color").getAsString();
    		Point point = (Point) f.geometry();
    		Position p = new Position(point.latitude(), point.longitude());
    		ChargingStation station = new ChargingStation(id, coins, power, icon, brightness, p);
    		stations.add(station);
    	}
		
    }
    
}
