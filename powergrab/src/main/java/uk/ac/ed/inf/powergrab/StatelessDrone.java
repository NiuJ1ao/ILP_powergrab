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
	public Feature strategy() {
		ArrayList<Point> points = new ArrayList<Point>();
		points.add(positionToPoint(position));
		List<Direction> validDirection = new ArrayList<Direction>();
		Direction[] directions = Direction.values();
		Position nextP;
		
		StationsManager mManager = StationsManager.getInstance();
		
		while (!isGameOver()) {
			validDirection.clear();
			boolean isMoved = false;
			
			
			for (Direction d : directions) {
				nextP = position.nextPosition(d);
				isMoved = false;
				boolean skullInRange = false;
			
				if (nextP.inPlayArea()) {
//					for (ChargingStation s : App.stations) {
//						if (s.coins != 0 && s.power != 0) {
//							double distance = Util.pythagoreanDistance(nextP, s.position);
//							if (distance <= ACCESSRANGE) {
//								if (s.type == ChargingStation.LIGHTHOUSE) {
//									isMoved = move(d);
//									points.add(positionToPoint(position));
//									s.transferCoins(this);
//									s.transferPower(this);
//								}
//								skullInRange = s.type == ChargingStation.SKULL;
//								break;
//							}
//						}
//					}
//					
//					if (!skullInRange) {
//						validDirection.add(d);
//					}
					mManager.computeDistance(d, points);
					
					
				}
				
				if (isMoved) {
					break;
				}
			}
			
			// Random move
			if (!isMoved && validDirection.size() != 0) {
				int idx = rnd.nextInt(validDirection.size());
				Direction nextd = validDirection.get(idx);
				isMoved = move(nextd);
				points.add(positionToPoint(position));
			}
			
			// No valid direction
			if (!isMoved) {
				int idx = rnd.nextInt(directions.length);
				Direction nextd = directions[idx];
				while (!move(nextd) && !isGameOver())
				points.add(positionToPoint(position));
			}
			
			//System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
		}
		
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
}
