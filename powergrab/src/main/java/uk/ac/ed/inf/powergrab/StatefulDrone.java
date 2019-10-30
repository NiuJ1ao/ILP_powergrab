package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatefulDrone extends Drone{

	public StatefulDrone(Position p, long seed) {
		super(p, seed);
	}

	@Override
	public Feature strategy() {
		List<Point> points = new ArrayList<Point>();
		points.add(positionToPoint(position));
		Direction[] directions = Direction.values();
		
		
		
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
	
	// Route planner: DFS / MCTS
	// distance to skull = +inf
	// Driver

}
