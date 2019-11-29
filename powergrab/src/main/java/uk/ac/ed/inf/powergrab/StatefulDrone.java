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
import com.mapbox.geojson.Feature;

/**
 * 
 * 
 * @author s1740055
 */
public class StatefulDrone extends Drone{

	private Map<Integer, Direction> radianToDirection = new HashMap<Integer, Direction>();
	
	/**
	 * Constructor of stateful drone. It initialises stateful drone as normal drone.
	 * But this creates a HashMap which contains angles and related direction and will be used later.
	 * @param p
	 * @param seed
	 * @param writer
	 */
	StatefulDrone(Position p, long seed, PrintWriter writer) {
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
	
	/**
	 * For stateful drone, greedy and 2-opt algorithm is used for finding the access order of light houses.
	 * And A* is used for looking for the path from current position of the drone to a specific station.
	 */
	@Override
	Feature strategy() {
		Direction[] directions = Direction.values();
		
		List<ChargingStation> route = greedyAlgorithm(); // Get the access order of light houses.
		if (route != null) {
			followRoute(route); 						 // Follow the route by using A*
		}
		
		// Start random move until game over.
		while (!isGameOver()) {
			int idx = rnd.nextInt(directions.length);
			Direction direction = directions[idx];
			Position nextP = position.nextPosition(direction);
			if (nextP.inPlayArea()) {	
				ChargingStation station = findNearestStationInRange(nextP);			 // Avoid skulls.
				if (station == null || station.type == ChargingStation.LIGHTHOUSE) { 
					move(direction, station);
				}
			}
		}
		
		return getLineString();
	}
	
	/**
	 * It follows the route returned by greedy and 2-opt algorithm.
	 * The drone follows the path calculated by A* from current position to the station iteratively
	 * @param route The route to follow.
	 */
	private void followRoute(List<ChargingStation> route) {
		Stack<Direction> path;
		
		for (ChargingStation station : route) {
			if (isGameOver()) { // Game over, exit
				break;
			}		
			// Use A* to find a path to station.
			path = aStar(position, station, 0);
			// Follow the path
			while(!isGameOver() && !path.isEmpty()) {
				Direction d = path.pop();
				move(d, null);
			}		
		}
	}
	
	/*******************************
	 **  A Star Search Algorithm  **
	 *******************************/
	/**
	 * This function performs A* algorithm from current location to the target station.
	 * @param drone   The position of the drone.
	 * @param station The target station
	 * @param attempt A count about how many times the A* is performed for this station.
	 * @return		  It returns a Stack of directions for the drone to follow.
	 * 
	 * @see 		  <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* algorithm</a>
	 */
	private Stack<Direction> aStar(Position drone, ChargingStation station, int attempt) {
		Map<Position, Double> gScores = new HashMap<Position, Double>();
		TreeMap<Double, Position> fScores = new TreeMap<Double, Position>();
		Map<Position, Position> cameFrom = new HashMap<Position, Position>();
		Set<Position> visited = new HashSet<Position>();
		int iterations = 0;
		int threshold = 1250; // Experimental result
		final double stepCost = 0.0003;
		
		// Initialise start position and add to A*.
		gScores.put(drone, 0.);
		fScores.put(station.distanceTo(drone), drone);
		Position current = drone;
		
		while (!fScores.isEmpty()) {			
			// Find the minimum f score and set current to relevant position;
			Entry<Double, Position> minPair = fScores.pollFirstEntry();
			current = minPair.getValue();
			
			if (iterations > threshold && attempt < 3) { 	// A* time out
				Stack<Direction> after = aStar(current, station, attempt+1);		// try again from current position recursively
				Stack<Direction> before = reconstructPath(cameFrom, current);   	// construct a path from start position to current position
				after.addAll(before);												// add them together and return.
				return after;
			} else if (iterations > threshold) { 									// Try too many times, GIVE UP.
				return reconstructPath(cameFrom, current);
			}
			
			// Check neighbours
			Position neighbour;
			ChargingStation nearestS;
			Direction [] directions = Direction.values();
			for (Direction d : directions) {
				neighbour = current.nextPosition(d);
				if (neighbour.inPlayArea()) {					
					// Goal test: the target station is the closest one and in access range.
					nearestS = findNearestStationInRange(neighbour);
					if (nearestS != null && nearestS.equals(station)) {
						cameFrom.put(neighbour, current);
						return reconstructPath(cameFrom, neighbour);
					}
					
					if (nearestS == null || (nearestS.coins >= 0 && nearestS.power >=0)) { // Check if that direction is dangerous.
						double gScore = gScores.get(current) + stepCost;
						double fScore = gScore + station.distanceTo(neighbour);
						if (!checkVisited(neighbour, visited)) { 						   // Check if the neighbour is visited.
							gScores.put(neighbour, gScore);
							fScores.put(fScore, neighbour);
							cameFrom.put(neighbour, current);
						}
					} 
					// The following code takes bad position into account when A* tries more than one time.
					else if (nearestS.type == ChargingStation.SKULL && attempt > 0 && (nearestS.coins + station.coins > 0 || coins == 0)) {
						double gScore = gScores.get(current) + stepCost - stepCost*(nearestS.coins/station.coins-3);
						double fScore = gScore + station.distanceTo(neighbour);
						if (!checkVisited(neighbour, visited)) {
							gScores.put(neighbour, gScore);
							fScores.put(fScore, neighbour);
							cameFrom.put(neighbour, current);
						}
					}
				}
			}	
			visited.add(current);
			iterations++;
		}
		return aStar(current, station, attempt+1);
	}
	
	/**
	 * A helper function of A*, it checks whether the position is visited or not.
	 * @param current  The position for check.
	 * @param visited  The set of Positions are visited before.
	 * @return		   A boolean value determines whether the position is in visited or not.
	 */
	private boolean checkVisited(Position current, Set<Position> visited) {
		for (Position p : visited) {
			if (p.latitude == current.latitude && p.longitude == current.longitude) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * A helper function of A*, it backtracks the cameFrom map from current position to the start position, 
	 * and transfers the angle between positions to direction for the drone to follow.
	 * @param cameFrom A Map<Postion, Position> data, each pair is the position and its parent.
	 * @param current  The position where A* finished.
	 * @return		   A stack of directions for the drone to follow.
	 */
	private Stack<Direction> reconstructPath(Map<Position, Position> cameFrom, Position current) {
		Stack<Direction> path = new Stack<Direction>();
		Position prev = null;
		Direction d;
		double radian;
		double dividor = Math.PI/8;
		
		while (cameFrom.containsKey(current)) {
			prev = cameFrom.get(current);														   // backtrack the cameFrom map.
			radian = Math.atan2(current.latitude-prev.latitude, current.longitude-prev.longitude); // Calculate the radian between two positions.
			d = radianToDirection.get((int) Math.round(radian/dividor));						   // Get related direction of the radian.
			path.add(d);
			current = prev;
		}
		return path;
	}
	
	
	/*************************
	 **   Greedy + 2-opt    **
	 *************************/
	/**
	 * This function performs greedy algorithm for all light houses initially 
	 * to get a greedy route which determines the order of stations the drone flies to.
	 * Then the route is optimised by 2-opt algorithm.
	 * @return The locally optimised route tells the access order of stations.
	 */
	private List<ChargingStation> greedyAlgorithm() {
		final List<ChargingStation> lightHouses = new ArrayList<ChargingStation>();
		App.getStations().forEach(s -> {if (s.type == ChargingStation.LIGHTHOUSE) lightHouses.add(s);});
		List<ChargingStation> route = new ArrayList<ChargingStation>();
		double routeLength = 0;
		
		Position current = position;
		while (!lightHouses.isEmpty()) {
			// find the nearest lighthouse from current;
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
		
		return twoOptOptimisation(route, routeLength);
	}
	
	/**
	 * This function calculates more optimal route from the greedy one 
	 * by comparing every possible valid combination of the swapping mechanism.
	 * @param route       The greedy route
	 * @param routeLength The length of greedy route.
	 * @return			  The optimised route.
	 * 
	 * @see 			  <a href="https://en.wikipedia.org/wiki/2-opt">2-opt</a>
	 */
	private List<ChargingStation> twoOptOptimisation(List<ChargingStation> route, double routeLength) {
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
					newLength = evaluateRoute(route, i, j, routeLength);
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
	
	/**
	 * A helper function for 2-opt. This uses a swap mechanism to get a new route.
	 * The pseudocode is shown below,
	 * 2optSwap(route, a, b) {
     *   1. take route[0] to route[a-1] and add them in order to new_route
     *   2. take route[a] to route[b] and add them in reverse order to new_route
     *   3. take route[b+1] to end and add them in order to new_route
     *   return new_route;
   	 * }
	 * @param current The current route
	 * @param a		  The start index to swap
	 * @param b		  The end index to swap
	 * @return		  The route after swapping
	 */
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
	
	/**
	 * A helper function for 2-opt. This calculates the length of the new route from the old one. 
	 * @param route       The old route
	 * @param a           The start index of swapping
	 * @param b  		  The end index of swapping
	 * @param routeLength The length of the old route
	 * @return			  The length of the new route.
	 */
	private double evaluateRoute(List<ChargingStation> route, int a, int b, double routeLength) {
		int max = route.size()-1;
		
		if (a==0 && b==max) {
			return routeLength - route.get(a).distanceTo(position) + route.get(b).distanceTo(position);
		} else if (a == 0) {
			return routeLength - route.get(b).distanceTo(route.get(b+1).position) - route.get(a).distanceTo(position) 
					+ route.get(b).distanceTo(position) + route.get(a).distanceTo(route.get(b+1).position);
		} else if (b==max) {
			return routeLength - route.get(a-1).distanceTo(route.get(a).position) + route.get(a-1).distanceTo(route.get(b).position);
		} else {
			return routeLength - route.get(a-1).distanceTo(route.get(a).position) - route.get(b).distanceTo(route.get(b+1).position) 
					+ route.get(a).distanceTo(route.get(b+1).position) + route.get(a-1).distanceTo(route.get(b).position);
		}
	}
}