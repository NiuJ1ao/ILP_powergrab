package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

public class Drone {
	
	public Position position;
	public int coins;
	public int power;
	public int seed;
	
	public Drone(Position p, int seed) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.seed = seed;
	}
	
	public void move(Direction d) {
		position = position.nextPosition(d);
	}
	
	public int coinTransfer() {
		return 0;
	}
	
	public int powerTransfer() {
		return 0;
	}
	
	public Feature strategy() {
		return null;
	}
}
