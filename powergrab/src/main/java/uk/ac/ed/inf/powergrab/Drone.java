package uk.ac.ed.inf.powergrab;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;


public abstract class Drone {
	
	public Position position;
	public double coins;
	public double power;
	protected Random rnd;
	final double MINPAYLOAD = 0.0;
	final double ACCESS_RANGE = 0.00025;
	final double POWER_CONSUMPTION = 1.25;
	protected int steps;
	private PrintWriter writer;
	
	public Drone(Position p, long seed, PrintWriter writer) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.rnd = new Random(seed);
		this.steps = 0;
		this.writer = writer;
	}
	
	public boolean move(Direction d) {
		Position p = position.nextPosition(d);
		Position prev = position;
		if (p.inPlayArea()) {			
			position = p;
			power = power - POWER_CONSUMPTION;
			steps++;
			
//			ChargingStation s = findNearestStation(position);
//			if (s.distanceToDrone <= ACCESS_RANGE) {
//				s.transferCoins(this);
//				s.transferPower(this);
//			}
			
			ChargingStation s = findNearestStationInRange(position);
			if (s != null) {
				s.transferCoins(this);
				s.transferPower(this);
			}
			
			writer.println(prev.latitude +" "+ prev.longitude +" "+ d +" "+ position.latitude +" "+ position.longitude +" "+ coins +" "+ power);
			return true;
		}
		return false;
	}
	
//	public ChargingStation findNearestStation(Position p){
//		double distance = 0;
//		List<ChargingStation> stations = App.stations;
//		int length = stations.size();
//		
//		ChargingStation nearestStation = stations.get(0);
//		double minDistance = nearestStation.distanceToDrone(p);
//		for (int i=1; i<length; i++) {
//			ChargingStation curStation = stations.get(i);
//			distance = curStation.distanceToDrone(p);
//			if (distance < minDistance) {
//				minDistance = distance;
//				nearestStation = curStation;
//			}
//		}
//		
//		return nearestStation;
//	}
	
	public ChargingStation findNearestStationInRange(Position p) {
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
	
	public double transferCoins(double amount) {
		double sum = coins + amount;
		
		if (sum < MINPAYLOAD) {
			amount = -coins;
			coins = MINPAYLOAD;
			return amount;
		} else {
			coins = sum;
			return amount;
		}
	}
	
	public double transferPower(double amount) {
		double sum = power + amount;
		
		if (sum < MINPAYLOAD) {
			amount = -power;
			power = MINPAYLOAD;
			return amount;
		} else {
			power = sum;
			return amount;
		}
	}
	
	public boolean isGameOver() {
		return power < POWER_CONSUMPTION || steps == 250;
	}
	
	public Point positionToPoint(Position position) {
		return Point.fromLngLat(position.longitude, position.latitude);
	}

	public abstract Feature strategy();
}
