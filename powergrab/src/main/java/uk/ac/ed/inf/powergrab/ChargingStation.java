package uk.ac.ed.inf.powergrab;

public class ChargingStation { 
	
	private String id = new String();
	protected double coins;
	protected double power;
	private String icon = new String();
	private String brightness = new String();
	protected final Position position;
	protected final int type;
	protected static final int LIGHTHOUSE = 1;
	protected static final int SKULL = 0;
	
	private Thread mThread;
	private Runnable mRunnable;
	private StationsManager sManager = StationsManager.getInstance();
	
	
	public ChargingStation(String id, double coins, double power, String icon, String brightness, Position p) {
		this.id = id;
		this.coins = coins;
		this.power = power;
		this.icon = icon;
		this.brightness = brightness;
		this.position = p;		
		type = (coins < 0 || power < 0) ? SKULL : LIGHTHOUSE;
		
		mRunnable = new StationRunnable(this, App.testDrone);
	}

	public void transferCoins(Drone drone) {
		double amount = drone.transferCoins(coins);
		coins -= amount;
	}
	
	public void transferPower(Drone drone) {
		double amount = drone.transferPower(power);
		power -= amount;
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

	public void setThread(Thread currentThread) {
		mThread = currentThread;
	}
	
	public Thread getThread() {
		return mThread;
	}
	
	public Runnable getRunnable() {
		return mRunnable;
	}
	
	public void handleRunnableStates(int state) {
		sManager.handleState(this, state);
	}
}
