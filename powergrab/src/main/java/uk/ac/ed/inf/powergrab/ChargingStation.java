package uk.ac.ed.inf.powergrab;

public class ChargingStation { 
	
	private String id = new String();
	protected double coins;
	protected double power;
	private String icon = new String();
	private String brightness = new String();
	protected final Position position;
	protected final boolean type;
	protected double distanceToDrone;
	protected static final boolean LIGHTHOUSE = true;
	protected static final boolean SKULL = false;
	
	public ChargingStation(String id, double coins, double power, String icon, String brightness, Position p) {
		this.id = id;
		this.coins = coins;
		this.power = power;
		this.icon = icon;
		this.brightness = brightness;
		this.position = p;		
		type = coins > 0 || power > 0;
	}

	public void transferCoins(Drone drone) {
		double amount = drone.transferCoins(coins);
		coins -= amount;
	}
	
	public void transferPower(Drone drone) {
		double amount = drone.transferPower(power);
		power -= amount;
	}
	
	public double distanceToDrone(Position drone) {
		distanceToDrone = Util.pythagoreanDistance(drone, position);
		return distanceToDrone;
	}
	
	public double distanceToDrone() {
		distanceToDrone = Util.pythagoreanDistance(App.testDrone.position, this.position);
		return distanceToDrone;
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
}
