package uk.ac.ed.inf.powergrab;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatefulDrone extends Drone{

	private Map<Integer, Direction> radianToDirection = new HashMap<Integer, Direction>();
	
	public StatefulDrone(Position p, long seed, PrintWriter writer) {
		super(p, seed, writer);
		
		// Create a HashMap for A* to retrieve the direction between two points.
		Direction[] directions = Direction.values();
		for (int i=0; i<=directions.length/2; i++) {
			radianToDirection.put(i, directions[i]);
		}
		for (int i=directions.length/2; i<directions.length; i++) {
			radianToDirection.put(i-16, directions[i]);
		}
	}
	
	@Override
	public Feature strategy() {
		List<Point> points = new ArrayList<Point>();
		points.add(positionToPoint(position));
		Direction[] directions = Direction.values();
		
//		System.out.println("Stateful strategy is called");
		
		List<ChargingStation> route = greedyAlgorithm(position); // Get the access order of light houses.
		points.addAll(followRoute(route)); // Follow the route by using A*
		
		// check any station left because A* could not calculate the path in time or just missed it.
		List<ChargingStation> lefts = new ArrayList<ChargingStation>();
		for (ChargingStation station : App.stations) {
			if (station.coins > 0) {
				lefts.add(station);
//				System.out.println("Station Left: " + station.getId());
			}
		}
		points.addAll(followRoute(lefts)); // Second attempt to arrive missed stations.
		
		// Start random move until game over.
		while (!isGameOver()) {
			int idx = rnd.nextInt(directions.length);
			Direction direction = directions[idx];
			Position nextP = position.nextPosition(direction);
//			if (nextP.inPlayArea() && findNearestStation(nextP).type == ChargingStation.LIGHTHOUSE) {					
//				move(direction);
//				points.add(positionToPoint(position));
//			}
			if (nextP.inPlayArea()) {	
				ChargingStation s = findNearestStationInRange(nextP);
				if (s == null || s.type == ChargingStation.LIGHTHOUSE)
				move(direction);
				points.add(positionToPoint(position));
			}
			
		}
		
		// Create LineString feature to return.
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
	
	private List<Point> followRoute(List<ChargingStation> route) {
		List<Point> points = new ArrayList<Point>();
		for (ChargingStation station : route) {
			if (isGameOver()) { // Game over, exit
				break;
			}
			
			// Use a star to find the path to station
			Stack<Direction> path = aStar(position, station);
//			System.out.println(path);
			// Follow the path
			while(!isGameOver() && !path.isEmpty()) {
				Direction d = path.pop();
				move(d);
				points.add(positionToPoint(position));
				System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
			}		
		}
		return points;
	}
	
	
	/*******************************
	 **  A Star Search Algorithm  **
	 *******************************/
	private Stack<Direction> aStar(Position drone, ChargingStation station) {
		Map<Position, Double> gScores = new HashMap<Position, Double>();
		Map<Position, Double> fScores = new HashMap<Position, Double>();
		Map<Position, Position> cameFrom = new HashMap<Position, Position>();
		int step = 0;
		final double stepCost = 0.0003;
		
//		System.out.println("A* search algorithm is called");
		
		gScores.put(drone, 0.);
		fScores.put(drone, station.distanceTo(drone));
		Position current = drone;
		
		double minFScore = Double.MAX_VALUE;
		while (true) {
			// Find the minimum f score and set current to relevant position;
			minFScore = Double.MAX_VALUE;
			for (Entry<Position, Double> pair : fScores.entrySet()) {
				if (pair.getValue() < minFScore) {
					minFScore = pair.getValue();
					current = pair.getKey();
				}
			}

			// Goal test: the target station is the closest one and in access range.
//			ChargingStation closestStation = findNearestStation(current);
//			if (closestStation.equals(station) && station.distanceToDrone <= ACCESS_RANGE) {
//				return reconstruct_path(cameFrom, current);
//			}
			ChargingStation closestStation = findNearestStationInRange(current);
			if (closestStation != null && closestStation.equals(station)) {
				return reconstruct_path(cameFrom, current);
			}
			
			// A* time out, return current calculated path.
			if (step>2000) {
//				System.out.println("GIVE UP!!!!");
				return reconstruct_path(cameFrom, current);
			}
			
			// Add neighbours
			fScores.remove(current);
			List<Position> neighbors = nextPositions(current);
			for (Position neighbor : neighbors) {
				gScores.put(neighbor, gScores.get(current) + stepCost);
				fScores.put(neighbor, gScores.get(neighbor) + station.distanceTo(neighbor));
				cameFrom.put(neighbor, current);
			}
			
			step++;
		}
		
	}
	
	private List<Position> nextPositions(Position p) {
		Direction [] directions = Direction.values();
		List<Position> result = new ArrayList<Position>();
		Position next;
		ChargingStation nearestS;
		
		for (Direction d : directions) {
			next = p.nextPosition(d);
			if (next.inPlayArea()) {
//				nearestS = findNearestStation(next);
//				if (nearestS.distanceToDrone > ACCESS_RANGE || nearestS.type == ChargingStation.LIGHTHOUSE) {
//					result.add(next);
//				}
				nearestS = findNearestStationInRange(next);
				if (nearestS == null || nearestS.type == ChargingStation.LIGHTHOUSE) {
					result.add(next);
				}
			}
		}	
		
		return result;
	}
	
	private Stack<Direction> reconstruct_path(Map<Position, Position> cameFrom, Position current) {
		Stack<Direction> path = new Stack<Direction>();
		Position prev = null;
		Direction d;
		double radian;
		double dividor = Math.PI/8;
		
//		System.out.println("reconstruct_path is called");
		while (cameFrom.containsKey(current)) {
			prev = cameFrom.get(current);
			radian = Math.atan2(current.latitude-prev.latitude, current.longitude-prev.longitude);
			d = radianToDirection.get((int) Math.round(radian/dividor));
			path.add(d);
			current = prev;
		}
		return path;
	}
	
	
	/*************************
	 **   Greedy + 2-opt    **
	 *************************/
	private List<ChargingStation> greedyAlgorithm(Position start) {
		final List<ChargingStation> lightHouses = new ArrayList<ChargingStation>();
		App.stations.forEach(s -> {if (s.type == ChargingStation.LIGHTHOUSE) lightHouses.add(s);});
		List<ChargingStation> route = new ArrayList<ChargingStation>();
		double routeLength = 0;
//		System.out.println("Greedy is called");
		
		Position current = start;
		while (!lightHouses.isEmpty()) {
			// find the nearest lighthouse;
			ChargingStation closestStation = lightHouses.get(0);
			double minDist = closestStation.distanceTo(current);
			int length = lightHouses.size();
			for (int i=1; i<length; i++) {
				ChargingStation station = lightHouses.get(i);
				double distance = station.distanceTo(current);
				if (distance < minDist) {
					minDist = distance;
					closestStation = station;
				}
			}
			current = closestStation.position;
			route.add(closestStation);
			routeLength += minDist;
			lightHouses.remove(closestStation);
		}
		
		return twoOptOptimization(route, routeLength);
	}
	
	private List<ChargingStation> twoOptOptimization(List<ChargingStation> route, double routeLength) {
//		System.out.println("2-opt is running");
		double currentPerformance = 0;
		List<ChargingStation> newRoute = new ArrayList<ChargingStation>();
		int numOfStations = route.size();
		double newLength = 0;
		boolean isImproved;
		while (currentPerformance != routeLength) {
			currentPerformance = routeLength;
			isImproved = false;
			for (int i=0; i<numOfStations-1; i++) {
				for (int j=i+1; j<numOfStations; j++) {
					newRoute = twoOptSwap(route, i, j);
					newLength = evaluateRoute(newRoute);
					if (newLength < routeLength) {
						route = newRoute;
						routeLength = newLength;
						isImproved = true;
						break;
					}
				}
				
				if (isImproved) {
					break;
				}
			}
		}
		
		return route;
	}
	
	private List<ChargingStation> twoOptSwap(List<ChargingStation> current, int a, int b) {
		List<ChargingStation> newRoute = new ArrayList<ChargingStation>();
		
		for (int i=0; i<a; i++) {
			newRoute.add(current.get(i));
		}
		for (int i=b; i>=a; i--) {
			newRoute.add(current.get(i));
		}
		for (int i=b+1; i<current.size(); i++) {
			newRoute.add(current.get(i));
		}
		
		return newRoute;
	}
	
	private double evaluateRoute(List<ChargingStation> route) {
		double distance = 0;
		int length = route.size();
		for (int i=0; i<length-1; i++) {
			distance += route.get(i).distanceTo(route.get(i+1).position);
		}
		return distance;
	}
}