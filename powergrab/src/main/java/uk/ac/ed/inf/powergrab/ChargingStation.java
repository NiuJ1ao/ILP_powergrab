package uk.ac.ed.inf.powergrab;

public class ChargingStation { 
	
	private String id = new String();
	private double coins;
	private double power;
	private String icon = new String();
	private String brightness = new String();
	protected final Position position;
	protected final int type;
	protected static final int LIGHTHOUSE = 1;
	protected static final int SKULL = 0;
	
	public ChargingStation(String id, double coins, double power, String icon, String brightness, Position p) {
		this.id = id;
		this.coins = coins;
		this.power = power;
		this.icon = icon;
		this.brightness = brightness;
		this.position = p;
		
		type = (coins < 0 || power < 0) ? SKULL : LIGHTHOUSE;
	}
	
	public double transferCoins(Drone drone) {
		double balance = drone.coins;
		
		if (coins + balance < 0) {
			coins = coins + balance;
			return coins;
		} else {
			balance = coins;
			coins = 0;
			return balance;
		}
	}
	
	public double transferPower(Drone drone) {
		double balance = drone.power;
		
		if (power + balance < 0) {
			power = power + balance;
			return power;
		} else {
			balance = power;
			power = 0;
			return balance;
		}
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

	public String getIcon() {
		return icon;
	}
	
	public int getType() {
		return type;
	}
}
