package uk.ac.ed.inf.powergrab;

import java.util.Calendar;
import java.net.URL;
import java.net.HttpURLConnection;
import com.mapbox.geojson.*;
import com.google.gson.JsonElement;


/**
 *
 *
 */
public class App {
	
	private String mapSource = new String();
	
    public static void main( String[] args ) {
    	App test = new App();
    	
    	try {
    		test.downloadMap();
    	} catch (Exception e) {
    		System.out.print(e);
    	}
    	
    }
    
    private void downloadMap() throws Exception {
    	// Get the date of today.
    	Calendar today = Calendar.getInstance();
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%d/0%d/0%d/powergrabmap.geojson"
    			, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
    	
    	URL mapUrl = new URL(mapString);
    	HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
    	conn.setReadTimeout(10000);
    	conn.setConnectTimeout(15000);
    	conn.setRequestMethod("GET");
    	conn.setDoInput(true);
    	conn.connect();
    	
    	mapSource = conn.getInputStream().toString();
    }
    
    private void praseSource() {
    	FeatureCollection fc = FeatureCollection.fromJson(mapSource);
    	
    }
}
