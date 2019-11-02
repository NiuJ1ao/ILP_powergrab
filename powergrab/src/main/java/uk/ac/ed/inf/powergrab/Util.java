package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

	static double pythagoreanDistance(Position drone, Position station) {
		double distance = Math.sqrt(Math.pow(station.latitude - drone.latitude, 2) 
				+ Math.pow(station.longitude - drone.longitude, 2));
		return distance;
	}
	
	static void astar(Position start, Position end) {
		Map<Position, Double> g_scores = new HashMap<Position, Double>();
		List<Position> visited = new ArrayList<Position>();
		Map<Position, Double> f_scores = new HashMap<Position, Double>();
		Map<Position, Position> cameFrom = new HashMap<Position, Position>();
		
		f_scores.put(start, pythagoreanDistance(start, end));
		visited.add(start);
		g_scores.put(start, 0.);
		while (!visited.isEmpty()) {
			Position cur = visited.get(0);
			for (Position p : visited) {
				if (f_scores.containsKey(p) && f_scores.get(p) < f_scores.get(cur)) {
					cur = p;
				};
			}
			
			if (approxEq(cur, end)) {
				//path.add(cur);
				return;
			}
			
			List<Position> neighbors = findNeighbors(cur);
			for (Position neighbor : neighbors) {
				double tentative_g = g_scores.get(cur) + Position.r;
				if (!visited.contains(neighbor)) {
					cameFrom.put(neighbor, cur);
					f_scores.put(neighbor, tentative_g + pythagoreanDistance(cur, end));
					g_scores.put(neighbor, tentative_g);
					visited.add(neighbor);
				} else if (g_scores.containsKey(neighbor) && tentative_g < g_scores.get(neighbor)) {
					cameFrom.replace(neighbor, cur);
					g_scores.replace(neighbor, tentative_g);
					f_scores.replace(neighbor, tentative_g + pythagoreanDistance(cur, end));
				}
			}
		}
		
		return;
	}
	
	private static List<Position> findNeighbors(Position p) {
		List<Position> neighbors = new ArrayList<Position>();
		Direction[] directions = Direction.values();
		boolean danger;
		
		for (Direction d : directions) {
			Position next = p.nextPosition(d);
			
			danger = false;
			for (ChargingStation s : App.stations) {
				if (s.type == ChargingStation.SKULL && s.distanceToDrone(next) <= Constants.ACCESS_RANGE) {
					danger = true;
					break;
				}
			}
			
			if (!danger && next.inPlayArea()) {
				neighbors.add(next);
			}
		}
		
		return neighbors;
	}
	
	private static boolean approxEq(double d0, double d1) {
		final double epsilon = 1.0E-12d;
		return Math.abs(d0 - d1) < epsilon;
	}
	
	private static boolean approxEq(Position p0, Position p1) {
		return approxEq(p0.latitude, p1.latitude) && approxEq(p0.longitude, p1.longitude); 
	}
}
