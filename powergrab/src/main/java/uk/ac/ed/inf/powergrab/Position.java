package uk.ac.ed.inf.powergrab;

public class Position {
	
	public double latitude;
	public double longitude;
	private final double r = 0.0003;
	
	public Position (double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public Position nextPosition(Direction direction) {
		double degree = directionToDegree(direction);
		
		double newLatitude = latitude + r * Math.sin(degree);
		double newLongitude = longitude + r * Math.cos(degree);
		
		Position nextPos = new Position(newLatitude, newLongitude);
		
		return nextPos;				
	}
	
	public boolean inPlayArea() {
		double MINLATITUDE = 55.942617;
		double MAXLATITUDE = 55.946233;
		double MAXLONGITUDE = -3.184319;
		double MINLONGITUDE = -3.192473;
		
		return (latitude > MINLATITUDE && latitude < MAXLATITUDE 
				&& longitude > MINLONGITUDE && longitude < MAXLONGITUDE);
	}
	
	public double directionToDegree(Direction direction ) {
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
