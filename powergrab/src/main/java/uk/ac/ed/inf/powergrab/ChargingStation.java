package uk.ac.ed.inf.powergrab;

/**
 * ChargingStation encapsulates the useful informations of a charging station.
 * The data includes:
 * <ul>
 * <li>The coins stored in the station
 * <li>The power stored in the station
 * <li>The position of the station.
 * <li>The type of drone.
 * </ul>
 * <p>
 * It also provides two constants for further checking the type of a charging station,
 * and several functions to update the informations of the station.
 * 
 * @author s1740055
 */
public class ChargingStation { 
	
	double coins;
	double power;
	final Position position;
	final boolean type;
	static final boolean LIGHTHOUSE = true;
	static final boolean SKULL = false;
	
	/**
	 * The constructor of charging station.
	 * @param coins
	 * @param power
	 * @param position
	 */
	ChargingStation(double coins, double power, Position position) {
		this.coins = coins;
		this.power = power;
		this.position = position;		
		type = coins > 0 || power > 0;
	}
	
	/**
	 * This function coins transformation request to the drone and update the coins of station by how many the drone takes.
	 * @param drone The drone which is connected to the station.
	 */
	void transferCoins(Drone drone) {
		double amount = drone.transferCoins(coins);
		coins -= amount;
	}
	
	/**
	 * This function sends power transformation request to the drone and update the power of station by how much the drone takes.
	 * @param drone The drone which is connected to the station.
	 */
	void transferPower(Drone drone) {
		double amount = drone.transferPower(power);
		power -= amount;
	}
	
	/**
	 * This calculates the Euclidean distance from this station to the given position.
	 * @param p The position of destination.
	 * @return  The distance from current station to the position.
	 */
	double distanceTo(Position p) {
		return Math.sqrt(Math.pow(position.latitude - p.latitude, 2) 
				+ Math.pow(position.longitude - p.longitude, 2));
	}
}
