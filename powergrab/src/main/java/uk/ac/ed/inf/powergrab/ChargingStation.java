package uk.ac.ed.inf.powergrab;

public class ChargingStation { 
	
	private String id = new String();
	public int coins;
	public int power;
	private String icon = new String();
	private String brightness = new String();
	private Position position;
	
	public ChargingStation(String id, int coins, int power, String icon, String brightness, Position p) {
		this.id = id;
		this.coins = coins;
		this.power = power;
		this.icon = icon;
		this.brightness = brightness;
		this.position = p;
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
}
