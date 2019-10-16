package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

public abstract class Drone {
	
	public Position position;
	public int coins;
	public int power;
	public long seed;
	private double EACHMOVE = 1.25;
	
	public Drone(Position p, long seed) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.seed = seed;
	}
	
	public void move(Direction d) {
		position = position.nextPosition(d);
	}
	
	public double transferCoins(double amount) {
		return 0;
	}
	
	public double transferPower(double amount) {
		return 0;
	}
	
	public abstract Feature strategy();
}
