package uk.ac.ed.inf.powergrab;

public class Util {

	static double pythagoreanDistance(Position drone, Position station) {
		double distance = Math.sqrt(Math.pow(station.latitude - drone.latitude, 2) 
				+ Math.pow(station.longitude - drone.longitude, 2));
		return distance;
	}
	
    static boolean approxEq(double d0, double d1) {
		final double epsilon = 1.0E-20d;
		return Math.abs(d0 - d1) < epsilon;
	}
	
	static boolean approxEq(Position p0, Position p1) {
		return approxEq(p0.latitude, p1.latitude) && approxEq(p0.longitude, p1.longitude); 
	}
}
