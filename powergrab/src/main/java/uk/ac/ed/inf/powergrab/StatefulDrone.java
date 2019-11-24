package uk.ac.ed.inf.powergrab;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

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
//		List<ChargingStation> lefts; 
//		do {
//			lefts = new ArrayList<ChargingStation>();
//			for (ChargingStation station : App.stations) {
//				if (station.coins > 0) {
//					lefts.add(station);
////					System.out.println("Station Left: " + station.getId());
//				}
//			}
//			points.addAll(followRoute(lefts)); // Second attempt to arrive missed stations.
//		} while (!lefts.isEmpty() && !isGameOver());

		
		// Start random move until game over.
		while (!isGameOver()) {
			int idx = rnd.nextInt(directions.length);
			Direction direction = directions[idx];
			Position nextP = position.nextPosition(direction);
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
		Stack<Direction> path;
		for (ChargingStation station : route) {
			if (isGameOver()) { // Game over, exit
				break;
			}
			
			// Use a star to find the path to station
			path = aStar(position, station, 0);
//			System.out.println(path);
			// Follow the path
			while(!isGameOver() && !path.isEmpty()) {
				Direction d = path.pop();
				move(d);
				points.add(positionToPoint(position));
//				System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
			}		
		}
		return points;
	}
	
	
	/*******************************
	 **  A Star Search Algorithm  **
	 *******************************/
	private Stack<Direction> aStar(Position drone, ChargingStation station, int attempt) {
		Map<Position, Double> gScores = new HashMap<Position, Double>();
//		Map<Position, Double> fScores = new HashMap<Position, Double>();
		TreeMap<Double, Position> fScores = new TreeMap<Double, Position>();
		Map<Position, Position> cameFrom = new HashMap<Position, Position>();
//		Set<Position> open = new HashSet<Position>();
		Set<Position> close = new HashSet<Position>();
		int step = 0;
		int threshold = 1250; // Experimental result
		final double stepCost = 0.0003;
		
//		System.out.println("A* search algorithm is running");
//		open.add(drone);
		gScores.put(drone, 0.);
		fScores.put(station.distanceTo(drone), drone);
		Position current = drone;
		
//		double minFScore;
		while (!fScores.isEmpty()) {
			// Find the minimum f score and set current to relevant position;
//			minFScore = Double.MAX_VALUE;
//			for (Entry<Position, Double> pair : fScores.entrySet()) {
//				if (pair.getValue() < minFScore) {
//					minFScore = pair.getValue();
//					current = pair.getKey();
//				}
//			}
			Entry<Double, Position> minPair = fScores.pollFirstEntry();
//			minFScore = minPair.getKey();
			current = minPair.getValue();
			
//			minFScore = Double.MAX_VALUE;
//			for (Position position : open) {
//				if (fScores.get(position) < minFScore) {
//					minFScore = fScores.get(position);
//					current = position;
//				}
//			}
			
//			System.out.println("F-score: " + minFScore);
			
			// Goal test: the target station is the closest one and in access range.

			if (step > threshold && attempt < 3) { // A* time out, try again from current point recursively.
				System.out.println("Time out, carry on");
				Stack<Direction> after = aStar(current, station, attempt+1);
				Stack<Direction> before = reconstruct_path(cameFrom, current);
				after.addAll(before);
				return after;
			} else if (step > threshold) { // Give up
				System.out.println("Give up");
				return reconstruct_path(cameFrom, current);
			}
			
			// Add neighbours
//			open.remove(current);
			List<Position> neighbors = nextPositions(current);
			for (Position neighbor : neighbors) {
				ChargingStation closestStation = findNearestStationInRange(neighbor);
				if (closestStation != null && closestStation.equals(station)) {
					cameFrom.put(neighbor, current);
					return reconstruct_path(cameFrom, neighbor);
				}
				double gScore = gScores.get(current) + stepCost;
				double fScore = gScore + station.distanceTo(neighbor);
//					Position oldInOpen = checkList(neighbor, open);
//					Position oldInClose = checkList(neighbor, close);
//					if (oldInOpen == null && oldInClose == null) {
//				if (!checkList(neighbor, open) && !checkList(neighbor, close)) {
//				if (!fScores.containsKey(fScore) && !checkList(neighbor, close)) {
				if (!checkList(neighbor, close)) {
//					if (oldInOpen != null) {
//						if (gScore < gScores.get(oldInOpen)) {
//							System.out.println("Open improved");
//							open.remove(oldInOpen);
//							gScores.replace(oldInOpen, gScore);
//							fScores.replace(oldInOpen, fScore);
//							cameFrom.replace(oldInOpen, current);
//						}
//					} else if (oldInClose != null) {	
//						if (gScore < gScores.get(oldInClose)) {
//							System.out.println("close found");
//							close.remove(oldInClose);
//							gScores.replace(oldInClose, gScore);
//							fScores.replace(oldInClose, fScore);
//							cameFrom.replace(oldInClose, current);
//						}
//					} else {
//						System.out.println("old not found");
//					open.add(neighbor);
					gScores.put(neighbor, gScore);
					fScores.put(fScore, neighbor);
					cameFrom.put(neighbor, current);
				}
				
			}
			close.add(current);
			step++;
		}
		return null;
	}

	private boolean checkList(Position position, Set<Position> list) {
		for (Position p : list) {
			if (p.latitude == position.latitude && p.longitude == position.longitude) {
				return true;
			}
		}
		return false;
	}
	
	private List<Position> nextPositions(Position p) {
		Direction [] directions = Direction.values();
		List<Position> result = new ArrayList<Position>();
		Position next;
		ChargingStation nearestS;
		
		for (Direction d : directions) {
			next = p.nextPosition(d);
			if (next.inPlayArea()) {
				nearestS = findNearestStationInRange(next);
				if (nearestS == null) {
					result.add(next);
				} else if (nearestS.type == ChargingStation.LIGHTHOUSE) {
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
		
		return twoOptOptimization(start, route, routeLength);
	}
	
	private List<ChargingStation> twoOptOptimization(Position start, List<ChargingStation> route, double routeLength) {
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
					newLength = evaluateRoute(start, route, i, j, routeLength);
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
	
	private double evaluateRoute(Position start, List<ChargingStation> route, int a, int b, double routeLength) {
		int max = route.size()-1;
		
		if (a==0 && b==max) {
			return routeLength - route.get(a).distanceTo(start) + route.get(b).distanceTo(start);
		}
		else if (a == 0) {
			return routeLength - route.get(b).distanceTo(route.get(b+1).position) - route.get(a).distanceTo(start) 
					+ route.get(b).distanceTo(start) + route.get(a).distanceTo(route.get(b+1).position);
		} 
		else if (b==max) {
			return routeLength - route.get(a-1).distanceTo(route.get(a).position) + route.get(a-1).distanceTo(route.get(b).position);
		} else {
			return routeLength - route.get(a-1).distanceTo(route.get(a).position) - route.get(b).distanceTo(route.get(b+1).position) 
					+ route.get(a).distanceTo(route.get(b+1).position) + route.get(a-1).distanceTo(route.get(b).position);
		}
	}
}