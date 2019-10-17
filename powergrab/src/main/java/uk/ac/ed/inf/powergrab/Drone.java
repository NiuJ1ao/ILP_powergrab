package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

public abstract class Drone {
	
	public Position position;
	public double coins;
	public double power;
	public long seed;
	private final double POWER_CONSUMPTION = 1.25;
	private final double MINPAYLOAD = 0.0;
	protected final double ACCESSRANGE = 0.00025;
	
	public Drone(Position p, long seed) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.seed = seed;
	}
	
	public void move(Direction d) {
		position = position.nextPosition(d);
		power = power - POWER_CONSUMPTION;
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
	
	public abstract Feature strategy();
}
