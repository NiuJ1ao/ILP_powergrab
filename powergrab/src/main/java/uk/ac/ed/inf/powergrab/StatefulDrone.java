package uk.ac.ed.inf.powergrab;

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

	private Map<Double, Direction> radianToDirection = new HashMap<Double, Direction>();
	
	public StatefulDrone(Position p, long seed) {
		super(p, seed);
		
		double radian = 0;
		double increment = Math.PI/8;
		Direction[] directions = Direction.values();
		radianToDirection.put(0., Direction.E);
		radianToDirection.put(Math.PI/8, Direction.ENE);
		radianToDirection.put(Math.PI/4, Direction.NE);
		radianToDirection.put(3*Math.PI/8, Direction.NNE);
		radianToDirection.put(Math.PI/2, Direction.N);
		radianToDirection.put(5*Math.PI/8, Direction.NNW);
		radianToDirection.put(3*Math.PI/4, Direction.NW);
		radianToDirection.put(7*Math.PI/8, Direction.WNW);
		radianToDirection.put(Math.PI, Direction.W);
		radianToDirection.put(-7*Math.PI/8, Direction.WSW);
		radianToDirection.put(-3*Math.PI/4, Direction.SW);
		radianToDirection.put(-5*Math.PI/8, Direction.SSW);
		radianToDirection.put(-Math.PI/2, Direction.S);
		radianToDirection.put(-3*Math.PI/8, Direction.SSE);
		radianToDirection.put(-Math.PI/4, Direction.SE);
		radianToDirection.put(-Math.PI/8, Direction.ESE);
//		for (int i=0; i<=directions.length/2; i++) {
//			radianToDirection.put(radian, directions[i]);
//			radian+=increment;
//		}
//		for (int i=directions.length/2; i<directions.length; i++) {
//			radianToDirection.put(radian-2*Math.PI, directions[i]);
//			radian+=increment;
//		}
	}
	
	@Override
	public Feature strategy() {
		List<Point> points = new ArrayList<Point>();
		points.add(positionToPoint(position));
		
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
				super.move(d);
				points.add(positionToPoint(position));
				System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
			}		
		}
		
		while (!isGameOver()) {
			System.out.println("Random move start.");
			Direction[] directions = Direction.values();
			for (Direction d : directions) {
				Position nextP = position.nextPosition(d);
				if (nextP.inPlayArea()) {		
					ChargingStation nearestStation = findNearestStation(nextP);					
					if (nearestStation.type == ChargingStation.LIGHTHOUSE) {
						super.move(d);
						points.add(positionToPoint(position));
						System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
						break;
					}
				}
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
		System.out.println("reconstruct_path is called");
		while (cameFrom.containsKey(current)) {
			prev = cameFrom.get(current);
//			for (Direction d : Direction.values()) {
//				Position est = prev.nextPosition(d);
//				if (est.latitude == current.latitude && est.longitude == current.longitude) {
//					path.add(d);
//					break;
//				}
//			}
			double delta = Math.atan2(current.latitude-prev.latitude, current.longitude-prev.longitude);
			Direction d = radianToDirection.get(delta);
			path.add(d);
			System.out.println("Path: " + path);
			current = prev;
		}
		return path;
	}
	
//	public static void main(String[] args) {
//		System.out.println("atan2 pi = " + Math.atan2(-1, -1));
//	}
	
	
	/*************************
	 **   Greedy + 2-opt    **
	 *************************/
	private ArrayList<ChargingStation> greedyAlgorithm(Position start) {
		final ArrayList<ChargingStation> lightHouses = new ArrayList<ChargingStation>();
		App.stations.forEach(s -> {if (s.type == ChargingStation.LIGHTHOUSE) lightHouses.add(s);});
		ArrayList<ChargingStation> path = new ArrayList<ChargingStation>();
		
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
			lightHouses.remove(closestStation);
		}
		
		return path;
	}
	
	
	
	
}
