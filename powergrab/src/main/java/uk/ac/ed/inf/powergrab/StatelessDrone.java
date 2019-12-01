package uk.ac.ed.inf.powergrab;

import java.util.List;
import java.util.ArrayList;
import com.mapbox.geojson.Feature;

/**
 * The StatelessDrone class implements the strategy of a stateless drone. A stateless drone is a 
 * memoryless drone and designed to against amateurs. Its decision of the next move to make can 
 * only be based on information about the charging stations which are within range of the sixteen 
 * positions where the drone can be after one move from its current position, and guided by the 
 * general gameplay of tries to move towards charging stations with positive value, while avoiding 
 * charging stations with negative value if possible.
 * 
 * @author s1740055
 */
public class StatelessDrone extends Drone{

	StatelessDrone(Position p, long seed, App app) {
		super(p, seed, app);
	}
	
	/**
	 * For the stateless drone, the strategy should perform random movement and avoid dangerous stations.
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
					nearestStation = findNearestStationInRange(nextP);	      // Find the closest station to the position.
					if (nearestStation != null && nearestStation.coins > 0) { // and it is a light house which was not connected before,
							move(direction, nearestStation);			      // move to that direction and retrieve the coins and power.
							isMoved = true;
							break;
					} 
					if (nearestStation == null || nearestStation.type == ChargingStation.LIGHTHOUSE) { // If there is no station can be connected, the direction is safe.
						validDirection.add(direction);					 							   // Add it to valid directions for random move.
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
