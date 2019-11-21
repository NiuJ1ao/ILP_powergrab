package uk.ac.ed.inf.powergrab;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatefulDrone extends Drone{

	private Map<Integer, Direction> radianToDirection = new HashMap<Integer, Direction>();
	
	public StatefulDrone(Position p, long seed, PrintWriter writer) {
		super(p, seed, writer);
		
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
		
		//System.out.println("Stateful strategy is called");
		
		ArrayList<ChargingStation> route = greedyAlgorithm(position);
		
		for (ChargingStation station : route) {
			if (isGameOver()) {
				break;
			}
			
			Stack<Direction> path = aStar(position, station);
//			System.out.println("Path: " + path);
			while(!isGameOver() && !path.isEmpty()) {
				Direction d = path.pop();
				move(d);
				points.add(positionToPoint(position));
				System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
			}		
		}
		
		while (!isGameOver()) {
//			System.out.println("Random move start.");
			int idx = rnd.nextInt(directions.length);
			Direction direction = directions[idx];
			Position nextP = position.nextPosition(direction);
			if (nextP.inPlayArea() && findNearestStation(nextP).type == ChargingStation.LIGHTHOUSE) {					
				move(direction);
				points.add(positionToPoint(position));
				System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
			}
			
		}
		
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
	
	
	/*******************************
	 **  A Star Search Algorithm  **
	 *******************************/
	private Stack<Direction> aStar(Position drone, ChargingStation station) {
		Map<Position, Double> gScores = new HashMap<Position, Double>();
		Map<Position, Double> fScores = new HashMap<Position, Double>();
		Set<Position> visited = new HashSet<Position>();
		Set<Position> open = new HashSet<Position>();
		Map<Position, Position> cameFrom = new HashMap<Position, Position>();
		int step = 0;
		
		//System.out.println("A* search algorithm is called");
		
		open.add(drone);
		gScores.put(drone, 0.);
		fScores.put(drone, station.distanceTo(drone));
		Position current = drone;

		while (!open.isEmpty()) {
			// find minimum f
			double minFScore = Double.MAX_VALUE;
			for (Position p : open) {
				if (fScores.get(p) < minFScore) {
					current = p;
					minFScore = fScores.get(p);
				}
			}
			
			// System.out.println("Distance to station: " + station.distanceTo(current));
			if (station.distanceTo(current) <= Constants.ACCESS_RANGE && step >= 1) {
				return reconstruct_path(cameFrom, current);
			}
			
			open.remove(current);
			List<Position> neighbors = nextPositions(current);
			for (Position neighbor : neighbors) {
				double gScore = gScores.get(current) + Position.r;
				double fScore = gScore + station.distanceTo(neighbor);
				if (visited.contains(neighbor)) {
					// update value if g is smaller;
					if (gScore < gScores.get(neighbor)) {
						gScores.replace(neighbor, gScore);
						fScores.replace(neighbor, fScore);
						cameFrom.replace(neighbor, current);
						open.add(neighbor);
					}
				} else {
					// add new point to list
					gScores.put(neighbor, gScore);
					fScores.put(neighbor, fScore);
					cameFrom.put(neighbor, current);
					open.add(neighbor);
				}
			}
			
			visited.add(current);
			step++;
		}
		
		return null;
	}
	
	private List<Position> nextPositions(Position p) {
		Direction [] directions = Direction.values();
		List<Position> result = new ArrayList<Position>();
		Position next;
		ChargingStation nearestS;
		
		for (Direction d : directions) {
			next = p.nextPosition(d);
			if (next.inPlayArea()) {
				nearestS = findNearestStation(next);
				if (nearestS.distanceToDrone > Constants.ACCESS_RANGE || nearestS.type == ChargingStation.LIGHTHOUSE) {
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
//			for (Direction d : Direction.values()) {
//				Position est = prev.nextPosition(d);
//				if (est.latitude == current.latitude && est.longitude == current.longitude) {
//					path.add(d);
//					break;
//				}
//			}
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
	private ArrayList<ChargingStation> greedyAlgorithm(Position start) {
		final ArrayList<ChargingStation> lightHouses = new ArrayList<ChargingStation>();
		App.stations.forEach(s -> {if (s.type == ChargingStation.LIGHTHOUSE) lightHouses.add(s);});
		ArrayList<ChargingStation> path = new ArrayList<ChargingStation>();
		double routeLength = 0;
		// System.out.println("Greedy is called");
		
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
			path.add(closestStation);
			routeLength += minDist;
			lightHouses.remove(closestStation);
		}
		
		// run 2-opt algorithm;
		double currentPerformance = routeLength;
		int numOfLightHouses = lightHouses.size();
		do {
			for (int i=0; i<numOfLightHouses-1; i++) {
				for (int j=i+1; j<numOfLightHouses; j++) {
					
				}
			}
		} while (currentPerformance == routeLength);
		
		return path;
	}
	
	private ArrayList<ChargingStation> twoOptSwap(ArrayList<ChargingStation> current, int a, int b) {
		List<ChargingStation> newRoute = new ArrayList<ChargingStation>();
		
		for (int i=0; i<a; i++) {
			newRoute
		}
		
		return null;
	}

}
