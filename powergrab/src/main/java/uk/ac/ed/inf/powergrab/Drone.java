package uk.ac.ed.inf.powergrab;

import java.util.Random;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;


public abstract class Drone {
	
	public Position position;
	public double coins;
	public double power;
	protected Random rnd;
	private final double POWER_CONSUMPTION = 1.25;
	private final double MINPAYLOAD = 0.0;
	protected final double ACCESSRANGE = 0.00025;
	
	public Drone(Position p, long seed) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.rnd = new Random(seed);
	}
	
	public boolean move(Direction d) {
		Position p = position.nextPosition(d);
		if (p.inPlayArea()) {
			position = p;
			power = power - POWER_CONSUMPTION;
			return true;
		}
		return false;
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
	
	public Point positionToPoint(Position position) {
		return Point.fromLngLat(position.longitude, position.latitude);
	}
	
	public abstract Feature strategy();
}
