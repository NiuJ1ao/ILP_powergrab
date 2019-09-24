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
		
		double newLatitude = r * Math.sin(degree);
		double newLongitude = r * Math.cos(degree);
		
		Position nextPos = new Position(newLatitude, newLongitude);
		
		return nextPos;				
	}
	
	public boolean inPlayArea() {
		return (latitude >= 5.942617 && latitude <= 55.94623
				&& longitude >= -3.184319 && longitude <= -3.19247);
	}
	
	private double directionToDegree(Direction direction ) {
		switch (direction) {
		case E:
			return 0;
		case ENE:
			return 22.5;
		case NE:
			return 45;
		case NNE:
			return 67.5;
		case N:
			return 90;
		case NNW:
			return 112.5;
		case NW:
			return 135;
		case WNW:
			return 157.5;
		case W:
			return 180;
		case WSW:
			return 202.5;
		case SW:
			return 225;
		case SSW:
			return 247.5;
		case S:
			return 270;
		case SSE:
			return 292.5;
		case SE:
			return 315;
		case ESE:
			return 337.5;
		default:
			return 0;
		}	
	}
}
