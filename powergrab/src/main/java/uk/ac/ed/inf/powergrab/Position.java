package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

class Position {
	
	double latitude;
	double longitude;
	private final static double r = 0.0003;
	private static HashMap<Direction, Position> movement = new HashMap<Direction, Position>();
	
	/**
	 * Initiate hashmap which contains the unit movement of every direction by trigonometry.
	 */
	static {
		double radian = 0;
		double increment = Math.PI/8;
		Direction[] directions = Direction.values();
		
		for (Direction d : directions) {
			movement.put(d, new Position(r * Math.sin(radian), r * Math.cos(radian)));
			radian += increment;
		}
	}
	
	Position (double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Calculate the latitude and longitude of next position, given direction.
	 * @param direction The chosen direction from 16 directions
	 * @return The next position
	 */
	Position nextPosition(Direction direction) {
		Position move = movement.get(direction);
		Position nextPos = new Position(this.latitude+move.latitude, this.longitude+move.longitude);
		return nextPos;				
	}
	
	/**
	 * This function checks if the current location is in play area.
	 * @return A boolean value to determine whether current location is in play area.
	 */
	boolean inPlayArea() {
		double MINLATITUDE = 55.942617;
		double MAXLATITUDE = 55.946233;
		double MAXLONGITUDE = -3.184319;
		double MINLONGITUDE = -3.192473;
		
		return (latitude > MINLATITUDE && latitude < MAXLATITUDE 
				&& longitude > MINLONGITUDE && longitude < MAXLONGITUDE);
	}
}
