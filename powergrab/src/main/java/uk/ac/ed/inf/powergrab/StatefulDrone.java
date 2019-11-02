package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private Position[] astar(Position drone, ChargingStation station) {
		Map<Position, Double> g_scores = new HashMap<Position, Double>();
		List<Position> openSet = new ArrayList<Position>();
		Map<Position, Double> f_scores = new HashMap<Position, Double>();
		Map<Position, Position> cameFrom = new HashMap<Position, Position>();
		
		Direction[] directions = Direction.values();
		
		f_scores.put(drone, station.distanceToDrone(drone));
		openSet.add(drone);
		g_scores.put(drone, 0.);
		while (!openSet.isEmpty()) {
			Position cur = openSet.get(0);
			for (Position p : openSet) {
				if (f_scores.containsKey(p) && f_scores.get(p) < f_scores.get(cur)) {
					cur = p;
				};
			}
			
			if (station.distanceToDrone(cur) <= Constants.ACCESS_RANGE) {
				return reconstruct_path(cameFrom, cur);
			}
			
			List<Position> neighbors = findNeighbors(cur);
			for (Position neighbor : neighbors) {
				double tentative_g = g_scores.get(cur) + Position.r;
				if (!openSet.contains(neighbor)) {
					cameFrom.put(neighbor, cur);
					f_scores.put(neighbor, tentative_g + station.distanceToDrone(cur));
					g_scores.put(neighbor, tentative_g);
					openSet.add(neighbor);
				} else {
					if (g_scores.containsKey(neighbor) && tentative_g < g_scores.get(neighbor)) {
						cameFrom.replace(neighbor, cur);
						g_scores.replace(neighbor, tentative_g);
						f_scores.replace(neighbor, tentative_g + station.distanceToDrone(cur));
					}
				}
			}
		}
		
		return null;
	}
	
	private Position[] reconstruct_path(Map<Position, Position> cameFrom, Position current) {
		return null;
	}
	
}
