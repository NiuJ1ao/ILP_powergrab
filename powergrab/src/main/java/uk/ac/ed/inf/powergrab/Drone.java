package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

/**
 * The Drone class is a abstract base class. A Drone instance represents a drone with its status. 
 * The state information for a drone includes:  
 * <ul>
 * <li>the position of the drone 
 * <li>the number of coins in the drone 
 * <li>the power stored in the drone 
 * <li>the initial seed for random move 
 * <li>the step counter counts how many steps the drone has made
 * <li>a list of Points tracks the path of the drone
 * <li>the instance of App that the drone is in 
 * </ul>
 * Also, some constants in the Drone class represents some limitations to the drone:
 * <ul>
 * <li>the maximum steps for a drone is 250 
 * <li>the power consumption of each step is 1.25 
 * <li>the access range of charging stations is 0.00025
 * <li>the minimum payload of the drone is 0
 * </ul>

 * @author s1740055
 */
abstract class Drone {
	
	Position position;
	double coins;
	double power;
	final Random rnd;
	App app;
	private int steps;
	private List<Point> points = new ArrayList<Point>();
	
	private static final double ACCESS_RANGE = 0.00025;
	private static final double POWER_CONSUMPTION = 1.25;
	private static final double MIN_PAYLOAD = 0.0;
	private static final int MAX_STEPS = 250;
	
	/**
	 * Constructs a drone at specific position. The power of the drone is initialised to 250. 
	 * The number of coins in the drone and the step are 0 initially. The initial position 
	 * is also added in the list of Points
	 * 
	 * @param position the position where the drone starts
	 * @param seed	   the initial seed
	 * @param writer   the instance of App that the drone is in
	 */
	Drone(Position position, long seed, App app) {
		this.position = position;
		this.coins = MIN_PAYLOAD;
		this.power = 250;
		this.rnd = new Random(seed);
		this.steps = 0;
		this.app = app;
		points.add(positionToPoint(position));
	}
	
	/**
	 * Moves the drone to the position at specific direction and update its status. The power is reduced by 1.25 
	 * and the step counter increments. If the nearest charging station in the access range is not pre-calculated 
	 * or not found, this method will try to find the closest charging station in the range again after every 
	 * movement and send transformation requests to the station if a station is found. This check pre-calculation
	 * feature is designed for stateless drone in order to avoid duplicate computation. Finally, new position is 
	 * added to the list of points and new status of the drone is written to text file by the print writer.
	 * 
	 * @param direction The direction which the drone should fly to.
	 * @param station	The pre-calculated station which should be connected by the drone after move. 
	 * 					If it is null, it will be calculated again in this function.
	 */
	void move(Direction direction , ChargingStation station) {
		Position p = position.nextPosition(direction);
		Position prev = position;		
		position = p;
		power -= POWER_CONSUMPTION;
		steps++;
		
		if (station == null) {
			station = findNearestStationInRange(position);
		}
		if (station != null) {
			station.transferCoins(this);
			station.transferPower(this);
		}
		points.add(positionToPoint(position));
		app.getWriter().println(prev.latitude +","+ prev.longitude +","+ direction +","+ position.latitude +","+ position.longitude +","+ coins +","+ power);
	}
	
	/**
	 * This method implements Euclidean distance from every charging stations in the App 
	 * to the specific position.
	 * 
	 * @param p The position for calculation.
	 * @return  The station can be connected or null.
	 */
	ChargingStation findNearestStationInRange(Position p) {
		double distance = 0;
		List<ChargingStation> stations = app.getStations();
		ChargingStation nearestStation = null;
		double minDistance = Double.MAX_VALUE;
		
		for (ChargingStation station : stations) {
			distance = station.distanceTo(p);
			if (distance <= ACCESS_RANGE && distance < minDistance) {
				minDistance = distance;
				nearestStation = station;
			}
		}
		
		return nearestStation;
	}
	
	/**
	 * It processes the transformation request from station and retrieves as many coins as the drone need.
	 * @param amount The amount of coins in the station.
	 * @return 		 The amount of coins the drone have taken.
	 */
	double transferCoins(double amount) {
		double sum = coins + amount;
		
		if (sum < MIN_PAYLOAD) { // If the the coins of drone is approaching negative, the coins of drone will be zero and return how many it takes.
			amount = -coins;
			coins = MIN_PAYLOAD;
			return amount;
		} else {				 // Takes all
			coins = sum;
			return amount;
		}
	}
	
	/**
	 * It processes the transformation request from station and retrieves as much power as the drone need.
	 * @param amount The amount of power in the station.
	 * @return 		 The amount of power the drone have taken.
	 */
	double transferPower(double amount) {
		double sum = power + amount;
		
		if (sum < MIN_PAYLOAD) {
			amount = -power;
			power = MIN_PAYLOAD;
			return amount;
		} else {
			power = sum;
			return amount;
		}
	}
	
	/**
	 * It checks if the game is over.
	 * @return A boolean value determines whether the game is over or not.
	 */
	boolean isGameOver() {
		return power < POWER_CONSUMPTION || steps == MAX_STEPS;
	}
	
	/**
	 * This function converts Position to Point.
	 * @param position The position need to be parsed.
	 * @return 		   The relevant Point data.
	 */
	private Point positionToPoint(Position position) {
		return Point.fromLngLat(position.longitude, position.latitude);
	}
	
	/**
	 * This converts the array of Points to a LineString
	 * @return The LineString represents the movements of the drone.
	 */
	Feature getLineString() {
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
	
	/**
	 * The actual function determines how the drone flies.
	 * @return The LineString of the path of drone.
	 */
    abstract Feature strategy();
}
