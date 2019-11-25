package uk.ac.ed.inf.powergrab;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public abstract class Drone {
	
	Position position;
	double coins;
	double power;
	Random rnd;
	private int steps;
	private PrintWriter writer;
	List<Point> points = new ArrayList<Point>();
	
	final double ACCESS_RANGE = 0.00025;
	private final double POWER_CONSUMPTION = 1.25;
	private final double MIN_PAYLOAD = 0.0;
	
	/**
	 * Constructor of Drone. It initialises the drone.
	 * @param position The position where the drone starts.
	 * @param seed	   The random seed for random moves.
	 * @param writer   The writer to record the status of the drone in text file.
	 */
	Drone(Position position, long seed, PrintWriter writer) {
		this.position = position;
		this.coins = 0;
		this.power = 250;
		this.rnd = new Random(seed);
		this.steps = 0;
		this.writer = writer;
		points.add(positionToPoint(position));
	}
	
	/**
	 * This function moves the drone by given direction. It also transfers coins and power when the drone can connect to any station.
	 * @param direction The direction which the drone should fly to.
	 * @param station	The pre-calculated station which should be connected by the drone after move. 
	 * 					If it is null, it will be calculated again in this function.
	 */
	void move(Direction direction, ChargingStation station) {
		Position p = position.nextPosition(direction);
		Position prev = position;		
		position = p;
		power = power - POWER_CONSUMPTION;
		steps++;
		
		if (station == null) {
			station = findNearestStationInRange(position);
		}
		if (station != null) {
			station.transferCoins(this);
			station.transferPower(this);
		}
		points.add(positionToPoint(position));
		writer.println(prev.latitude +" "+ prev.longitude +" "+ direction +" "+ position.latitude +" "+ position.longitude +" "+ coins +" "+ power);
//		System.out.println(steps+" - coins: "+coins+", power: "+power);
	}
	
	/**
	 * Find out which station can be connected from given position.
	 * @param p The position for calculation.
	 * @return  The station can be connected or null.
	 */
	ChargingStation findNearestStationInRange(Position p) {
		double distance = 0;
		List<ChargingStation> stations = App.stations;
		ChargingStation nearestStation = null;
		double minDistance = Double.MAX_VALUE;
		
		for (ChargingStation station : stations) {
			double deltaX = Math.abs(station.position.longitude - p.longitude);
			double deltaY = Math.abs(station.position.latitude - p.latitude);
			if (deltaX <= ACCESS_RANGE && deltaY <= ACCESS_RANGE) {
				distance = Math.sqrt(Math.pow(deltaY, 2) + Math.pow(deltaX, 2));
				if (distance <= ACCESS_RANGE && distance < minDistance) {
					minDistance = distance;
					nearestStation = station;
				}
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
		return power < POWER_CONSUMPTION || steps == 250;
	}
	
	/**
	 * This function parses Position to Point.
	 * @param position The position need to be parsed.
	 * @return 		   The relevant Point data.
	 */
	Point positionToPoint(Position position) {
		return Point.fromLngLat(position.longitude, position.latitude);
	}
	
	/**
	 * This changes the array of Points to a LineString
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
