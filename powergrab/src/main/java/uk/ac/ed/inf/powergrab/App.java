package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import com.mapbox.geojson.*;


/**
 * The App class is the main part of the application. The App has the only public method, 
 * the constructor of App. All processes of this application are handled in the constructor. 
 * Charging stations and a drone are encapsulated in a App instance. The App initiates charging
 * stations by downloading GeoJSON file online according to the first three arguments 
 * which specify which date the map is. Then it initiates a drone by last three arguments,
 * which specify the initial position where the drone starts, 
 * a random seed for generating random movement of the drone and the type of this drone, 
 * and App executes the drone. After the execution, a text file and aGeoJSON file are created and 
 * appended with status changes of the drone and a map information for visualising the path of the drone respectively.
 * 
 * @author s1740055
 */
class App {
	private final List<ChargingStation> stations = new ArrayList<ChargingStation>();
	private Drone drone;
	private List<Feature> featuresList;
	private PrintWriter txtWriter;
	private PrintWriter jsonWriter;
	
    public static void main(String[] args) throws Exception {  	
    	// Parse arguments.
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	Position initDronePos = new Position(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
    	long seed = Long.parseLong(args[5]);
    	String droneType = args[6].toLowerCase();
    	
    	// Run APP
    	App app = new App(day, month, year, initDronePos, seed, droneType);
    	app.run();
    }
    
    /**
     * Constructs the App with the specific map and a drone with the specific state information. Because the application
     * does not have any APIs, all processes are handled in this constructor and the constructor returns a App instance 
     * after the drone finishes. The processes includes downloading the specific map, initiating and executing the specific
     * type of drone.
     * 
     * @param day
     * @param month			The date of map.
     * @param year
     * @param initDronePos	The position where the drone starts.
     * @param seed			The random seed.
     * @param droneType		The type of drone, either stateless or stateful.
     */
    App(String day, String month, String year, Position initDronePos, long seed, String droneType) {
    	// Initiate stations
		downloadMap(year, month, day);
    	
    	// Initiate drone
    	String txtFileName = String.format("%s-%s-%s-%s.txt", droneType, day, month, year); 
    	String geojsonFileName = String.format("%s-%s-%s-%s.geojson", droneType, day, month, year);
		try {
			// Initialise PrintWriters for outputs.
			txtWriter = new PrintWriter(txtFileName, "UTF-8");
	    	jsonWriter = new PrintWriter(geojsonFileName, "UTF-8");
			
			// Initialise a drone instance.
			if (droneType.equals("stateless")) {
	    		drone = new StatelessDrone(initDronePos, seed, this);
	    	} else if (droneType.equals("stateful")) {
	    		drone = new StatefulDrone(initDronePos, seed, this);
	    	} else {
	    		txtWriter.close();
	    		throw new ClassNotFoundException();
	    	}
	    	
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Drone Type Error: "+ droneType + " drone is not valid. The type of a drone should be either <stateful> or <stateless>.");
			System.exit(1);
		}
    	
    }
    
    /**
     * Executes the drone and appends the LineString returned by drone to the feature collection of the map. 
     * Then writes the feature collection to a GeoJSON file
     * 
     * @param txtWriter   The PrintWriter for the drone to store its state changes
     * @param jsonWriter  The PrintWriter for the app to generate a GeoJSON file
     */
    void run() {
    	// Run the drone.
    	Feature f = drone.strategy();
		txtWriter.close();
		
		// Add feature to exist features list and create a feature collection.
    	featuresList.add(f);
    	FeatureCollection fc = FeatureCollection.fromFeatures(featuresList);
    	
    	// Write feature collection to GeoJson file.
		jsonWriter.println(fc.toJson());
		jsonWriter.close();
    }
    
    /**
     * Requests a map from the specific date online and converts the InputStream from the website into a JSON String.
     * 
     * @param year
     * @param month		   Those three are the date of map which is trying to get.
     * @param day
     */
    private void downloadMap(String year, String month, String day) {
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson", 
    										year, month, day);
    	
		try {
			// Try to connect to the URL.
			URL mapUrl = new URL(mapString);
			HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
	    	conn.setReadTimeout(10000);
	    	conn.setConnectTimeout(15000);
	    	conn.setRequestMethod("GET");
	    	conn.setDoInput(true);
	    	conn.connect();
	    	
	    	// Get the data from website.
	    	InputStream in = conn.getInputStream();
	    	    	
	    	// Parse InputStream to String
	    	int c = 0;
	    	StringBuilder mapBuilder = new StringBuilder();
	    	Reader reader = new BufferedReader(new InputStreamReader(in));
    		while ((c = reader.read()) != -1) {
    			mapBuilder.append((char) c);
    		}
	    	String mapSource = mapBuilder.toString();
	    	
	    	parseSource(mapSource);
	    	
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + mapString + " is not found. Please try another date.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Connection Failure: Please check network connection.");
			System.exit(1);
		}
    	
    }
    
    /**
     * Retrieves information about charging stations from specific JSON string and stores each charging station into a ArrayList.
     * 
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
    
    List<ChargingStation> getStations() {
    	return stations;
    }
    
    PrintWriter getWriter() {
    	return txtWriter;
    }
}
