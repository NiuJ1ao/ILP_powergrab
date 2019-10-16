package uk.ac.ed.inf.powergrab;

public class ChargingStation { 
	
	private String id = new String();
	private double coins;
	private double power;
	private String icon = new String();
	private String brightness = new String();
	private Position position;
	private int type;
	private double MAXPAYLOAD = 125.0;
	
	public ChargingStation(String id, double coins, double power, String icon, String brightness, Position p) {
		this.id = id;
		this.coins = coins;
		this.power = power;
		this.icon = icon;
		this.brightness = brightness;
		this.position = p;
		
		type = (coins < 0 || power < 0) ? 0 : 1; // lighthouse = 1; danger = 0
	}
	
	public double transferCoins(double amount) {
		if (coins >= amount) {
			coins = coins - amount;
		} else {
			amount = coins;
			coins = 0;
		}
		
		return amount;
	}
	
	public double transferPower(double amount) {
		if (power >= amount) {
			power = power - amount;
		} else {
			amount = power;
			power = 0;
		}
		
		return amount;	
	}
	
	/***
	 * Getters for private variables.
	 */
	public String getId() {
		return id;
	}
	
	public String getBrightness() {
		return brightness;
	}

	public Position getPosition() {
		return position;
	}

	public String getIcon() {
		return icon;
	}
	
	public int getType() {
		return type;
	}
}
