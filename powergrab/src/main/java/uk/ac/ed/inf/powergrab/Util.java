package uk.ac.ed.inf.powergrab;

public class Util {

	static double pythagoreanDistance(Position drone, Position station) {
		double distance = Math.sqrt(Math.pow(station.latitude - drone.latitude, 2) 
				+ Math.pow(station.longitude - drone.longitude, 2));
		return distance;
	}
}
