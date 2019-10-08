package uk.ac.ed.inf.powergrab;

public class Drone {
	
	public Position position;
	public int coins;
	public int power;
	
	public Drone(Position p, int coins, int power) {
		this.position = p;
		this.coins = coins;
		this.power = power;
	}	
}
