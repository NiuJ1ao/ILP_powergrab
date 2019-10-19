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
		
		while (!isGameOver()) {
			List<Direction> validDirection = new ArrayList<Direction>();
			Direction[] directions = Direction.values();
			boolean isMoved = false;
			
			
			for (Direction d : directions) {
				Position nextP = position.nextPosition(d);
				isMoved = false;
				boolean skullInRange = false;
				
				if (nextP.inPlayArea()) {
					for (ChargingStation s : App.stations) {
						double distance = Util.pythagoreanDistance(nextP, s.position);
						if (distance <= ACCESSRANGE) {
							if (s.type == ChargingStation.LIGHTHOUSE) {
								isMoved = move(d);
								points.add(positionToPoint(position));
								transferCoins(s.transferCoins(this));
								transferPower(s.transferPower(this));
							} else {
								skullInRange = true;
							}
							break;
						}
					}
					
					if (!skullInRange) {
						validDirection.add(d);
					}
				}
				
				if (isMoved) {
					break;
				}
			}
			
			// Random move
			if (!isMoved && validDirection.size() !=0) {
				int idx = rnd.nextInt(validDirection.size());
				Direction nextd = validDirection.get(idx);
				isMoved = move(nextd);
				points.add(positionToPoint(position));
			}
			
			// No valid direction
			if (!isMoved ) {
				int idx = rnd.nextInt(directions.length);
				Direction nextd = directions[idx];
				while (!move(nextd))
				points.add(positionToPoint(position));
			}
			
			System.out.println(points.size());
		}
		
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
	
	
}
