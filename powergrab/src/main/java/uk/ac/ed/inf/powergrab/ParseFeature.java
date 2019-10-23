package uk.ac.ed.inf.powergrab;

import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

public class ParseFeature implements Runnable{
	
	List<Feature> fl;
	
	public ParseFeature(List<Feature> featuresList) {
		fl = featuresList;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (final Feature f : fl) {
			String id = f.getProperty("id").getAsString();
    		double coins = f.getProperty("coins").getAsFloat();
    		double power = f.getProperty("power").getAsFloat();
    		String icon = f.getProperty("marker-symbol").getAsString();
    		String brightness = f.getProperty("marker-color").getAsString();
    		Point point = (Point) f.geometry();
    		Position p = new Position(point.latitude(), point.longitude());
    		ChargingStation station = new ChargingStation(id, coins, power, icon, brightness, p);
    		App.stations.add(station);
    	}
	}
	
}
