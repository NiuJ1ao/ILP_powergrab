package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import com.mapbox.geojson.*;


/**
 *
 *
 */
public class App {
	
	private String mapSource = new String();
	private List<ChargingStation> stations = new ArrayList<ChargingStation>();
	
    public static void main( String[] args ) {
    	App test = new App();
    	
    	try {
			test.downloadMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	test.praseSource();
    	int i = 0;
    	for (ChargingStation s : test.stations) {
    		System.out.println(s.getId() + "---" + ++i);
    	}
    	
    }
    
    private void downloadMap() throws Exception {
    	// Get the date of today.
    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
    	LocalDateTime today = LocalDateTime.now();  
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/powergrabmap.geojson", dtf.format(today));
    	
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
    
    private void praseSource() {
	    FeatureCollection fc = FeatureCollection.fromJson(mapSource);
    	List<Feature> featuresList  = fc.features();
    	for (Feature f : featuresList) {
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
