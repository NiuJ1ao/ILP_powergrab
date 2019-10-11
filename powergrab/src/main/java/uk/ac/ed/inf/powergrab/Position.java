package uk.ac.ed.inf.powergrab;

public class Position {
	
	public double latitude;
	public double longitude;
	private final double r = 0.0003;
	
	public Position (double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Use trigonometry to calculate the latitude and longitude of next position, given direction.
	 * @param direction The chosen direction from 16 directions
	 * @return The next position
	 */
	public Position nextPosition(Direction direction) {
		double radian = directionToRadians(direction);
		
		double newLatitude = latitude + r * Math.sin(radian);
		double newLongitude = longitude + r * Math.cos(radian);
		
		Position nextPos = new Position(newLatitude, newLongitude);
		
		return nextPos;				
	}
	
	/**
	 * This function checks if the current location is in play area.
	 * @return A boolean value to determine whether current location is in play area.
	 */
	public boolean inPlayArea() {
		double MINLATITUDE = 55.942617;
		double MAXLATITUDE = 55.946233;
		double MAXLONGITUDE = -3.184319;
		double MINLONGITUDE = -3.192473;
		
		return (latitude > MINLATITUDE && latitude < MAXLATITUDE 
				&& longitude > MINLONGITUDE && longitude < MAXLONGITUDE);
	}
	
	/**
	 * This function transfers a given direction to a radian for easy calculation.
	 * @param direction
	 * @return The radius of related direction
	 */
	private double directionToRadians(Direction direction ) {
		double pi = Math.PI;
		switch (direction) {
		case E:
			return 0;
		case ENE:
			return pi/8;
		case NE:
			return pi/4;
		case NNE:
			return 3*pi/8;
		case N:
			return pi/2;
		case NNW:
			return 5*pi/8;
		case NW:
			return 3*pi/4;
		case WNW:
			return 7*pi/8;
		case W:
			return pi;
		case WSW:
			return 9*pi/8;
		case SW:
			return 5*pi/4;
		case SSW:
			return 11*pi/8;
		case S:
			return 3*pi/2;
		case SSE:
			return 13*pi/8;
		case SE:
			return 7*pi/4;
		case ESE:
			return 15*pi/8;
		default:
			return 0;
		}	
	}
}
