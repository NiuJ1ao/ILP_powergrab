package uk.ac.ed.inf.powergrab;

/**
 * The ChargingStation class represents charging stations. A ChargingStation instance encapsulates
 *  state information about a charging station. These information includes: 
 *  <ul>
 *  <li> the number of coins/debts in the station> the power stored in the station
 *  <li> the position of the station
 *  <li> the type of the station
 *  </ul>
 *  Moreover, two static Boolean constants, LIGHTHOUSE and SKULL, indicates two types of charging station.
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
	 * Constructs a new charging station according to the specific data
	 * 
	 * @param coins      the number of coins/debts stored in the station
	 * @param power		 the power stored in the station
	 * @param position   the position of the station
	 */
	ChargingStation(double coins, double power, Position position) {
		this.coins = coins;
		this.power = power;
		this.position = position;		
		type = coins > 0 || power > 0;
	}
	
	/**
	 * Transfers coins/debts to the specific drone and updates the number of coins 
	 * in the charging station by how many coins the drone takes.
	 * 
	 * @param drone The drone which is connected to the station.
	 */
	void transferCoins(Drone drone) {
		double amount = drone.transferCoins(coins);
		coins -= amount;
	}
	
	/**
	 * Transfers power to the specific drone and updates the quantity of power in 
	 * the charging station by how much power the drone takes.
	 * 
	 * @param drone The drone which is connected to the station.
	 */
	void transferPower(Drone drone) {
		double amount = drone.transferPower(power);
		power -= amount;
	}
	
	/**
	 * Computes the Euclidean distance from the charging station to the specific position.
	 * @param p The destination position for computing distance.
	 * @return  The distance from current station to the position.
	 */
	double distanceTo(Position p) {
		return Math.sqrt(Math.pow(position.latitude - p.latitude, 2) 
				+ Math.pow(position.longitude - p.longitude, 2));
	}
}
