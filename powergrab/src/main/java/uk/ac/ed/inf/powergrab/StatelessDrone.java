package uk.ac.ed.inf.powergrab;

import java.util.List;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.mapbox.geojson.Feature;

public class StatelessDrone extends Drone{

	StatelessDrone(Position p, long seed, PrintWriter writer) {
		super(p, seed, writer);
	}
	
	/**
	 * For the stateless drone, the strategy should perform random move and avoid dangerous stations.
	 */
	@Override
	Feature strategy() {
		List<Direction> validDirection = new ArrayList<Direction>();
		Direction[] directions = Direction.values();
		Position nextP;
		ChargingStation nearestStation;
		boolean isMoved = false;
		
		while (!isGameOver()) {
			// Initialise variables
			validDirection.clear();
			isMoved = false;
			
			// Search surrounding in one-step range
			for (Direction direction : directions) {
				nextP = position.nextPosition(direction);
				if (nextP.inPlayArea()) {		
					nearestStation = findNearestStationInRange(nextP);	// Find the closest station to the position.
					if (nearestStation != null) {						// If a station can be connected
						if (nearestStation.coins > 0) {					// and it is a light house which was not connected before,
							move(direction, nearestStation);			// move to that direction and retrieve the coins and power.
							isMoved = true;
							break;
						}
					} else {											// If there is no station can be connected, the direction is safe.
						validDirection.add(direction);					// Add it to valid directions for random move.
					}
				}
			}
			
			// Random move but avoid skulls.
			while (!isMoved && !validDirection.isEmpty()) {
				int idx = rnd.nextInt(validDirection.size());
				Direction nextd = validDirection.get(idx);
				if (position.nextPosition(nextd).inPlayArea()) {
					move(nextd, null);
					isMoved = true;
				}
			}
			
			// No valid direction (perhaps the drone is surrounded by skulls), move to a random direction.
			while (!isMoved && validDirection.isEmpty()) {
				int idx = rnd.nextInt(directions.length);
				Direction nextd = validDirection.get(idx);
				if (position.nextPosition(nextd).inPlayArea()) {
					move(nextd, null);
					isMoved = true;
				}
			}
		}
		
		return getLineString();
	}
}
