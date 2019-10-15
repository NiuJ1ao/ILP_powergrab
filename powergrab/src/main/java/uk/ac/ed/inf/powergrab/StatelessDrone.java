package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

public class StatelessDrone extends Drone{

	public StatelessDrone(Position p, int seed) {
		super(p, seed);
	}
	
	@Override
	public Feature strategy() {
		
	}

}
