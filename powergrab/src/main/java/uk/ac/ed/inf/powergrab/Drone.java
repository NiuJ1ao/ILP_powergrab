package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;

public abstract class Drone {
	
	public Position position;
	public double coins;
	public double power;
	public long seed;
	private double EACHMOVE = 1.25;
	private double MINPAYLOAD = 0.0;
	
	public Drone(Position p, long seed) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.seed = seed;
	}
	
	public void move(Direction d) {
		position = position.nextPosition(d);
		power = power - EACHMOVE;
	}
	
	public void transferCoins(double amount) {
		coins = (coins + amount <= MINPAYLOAD) ? MINPAYLOAD : coins + amount;
	}
	
	public void transferPower(double amount) {
		power = (power + amount <= MINPAYLOAD) ? MINPAYLOAD : power + amount;
	}
	
	public boolean isGameOver() {
		return power == MINPAYLOAD;
	}
	
	public List<ChargingStation> stationsNearby() {
		List<ChargingStation> stations = new ArrayList<ChargingStation>();
		List<ChargingStation> not = new ArrayList<ChargingStation>();
		
		for (Direction d : Direction.values()) {
			Position nextP = position.nextPosition(d);
			for (ChargingStation s : App.stations) {
				if (nextP == s.getPosition()) {
					stations.add(s);
				}
			}
		}
		
		return stations;
	}
	
	public abstract Feature strategy();
}
