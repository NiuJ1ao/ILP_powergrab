package uk.ac.ed.inf.powergrab;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.mapbox.geojson.*;

public class StatelessDrone extends Drone{

	public StatelessDrone(Position p, long seed) {
		super(p, seed);
	}
	
	@Override
	public Feature strategy() throws Exception {
		List<Point> points = new ArrayList<Point>();
		points.add(positionToPoint(position));
		List<Direction> validDirection = new ArrayList<Direction>();
		Direction[] directions = Direction.values();
//		Position nextP;
		
		while (!isGameOver()) {
			validDirection.clear();
			boolean isMoved = false;
			
			
			for (Direction d : directions) {
//				nextP = position.nextPosition(d);
				isMoved = false;
				boolean skullInRange = false;
//			
//				if (nextP.inPlayArea()) {
//					for (ChargingStation s : App.stations) {
//						if (s.coins != 0 && s.power != 0) {
//							double distance = Util.pythagoreanDistance(nextP, s.position);
//							if (distance <= Constants.ACCESS_RANGE) {
//								if (s.type == ChargingStation.SKULL) {
//									skullInRange = true;
//								} else if (s.type == ChargingStation.LIGHTHOUSE) {
//									isMoved = move(d);
//									points.add(positionToPoint(position));
//									s.transferCoins(this);
//									s.transferPower(this);
//								}
//								break;
//							}
//						}
//					}
//					
//					if (!skullInRange) {
//						validDirection.add(d);
//					}
//				}
//				
//				if (isMoved) {
//					break;
//				}
				Drone prevStatus = this;
				isMoved = move(d);
				
				if (isMoved) {
					if (closestStation.type == ChargingStation.LIGHTHOUSE && closestStation.getDistance() < Constants.ACCESS_RANGE) {
						points.add(positionToPoint(position));
						break;
					} else if (closestStation.type == ChargingStation.SKULL && closestStation.getDistance() < Constants.ACCESS_RANGE){
						rollBack(prevStatus);
						skullInRange = true;
					} else {
						rollBack(prevStatus);
						if (!skullInRange) {
							validDirection.add(d);
						}
					}
				}
				
			}
			
			// Random move
			if (!isMoved && validDirection.size() != 0) {
				int idx = rnd.nextInt(validDirection.size());
				Direction nextd = validDirection.get(idx);
				isMoved = move(nextd);
				points.add(positionToPoint(position));
			}
			
//			// No valid direction
//			if (!isMoved) {
//				int idx = rnd.nextInt(directions.length);
//				Direction nextd = directions[idx];
//				while (move(nextd) || isGameOver())
//				points.add(positionToPoint(position));
//			}
			
			System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
		}
		
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
}
