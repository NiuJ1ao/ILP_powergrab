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
	public boolean move(Direction d) {
		Position p = position.nextPosition(d);
			
		if (p.inPlayArea()) {			
			position = p;
			power = power - POWER_CONSUMPTION;
			steps++;
			
			return true;
		}
		return false;
	}
	
	@Override
	public Feature strategy() throws Exception {
		List<Point> points = new ArrayList<Point>();
		points.add(positionToPoint(position));
		List<Direction> validDirection = new ArrayList<Direction>();
		Direction[] directions = Direction.values();
		Position nextP;
		ChargingStation nearestStation;
		boolean isMoved = false;
		
		
		while (!isGameOver()) {
			validDirection.clear();
			isMoved = false;
			
			for (Direction d : directions) {
				nextP = position.nextPosition(d);
			
				if (nextP.inPlayArea()) {		
					nearestStation = findNearestStation(nextP);
					
					if (nearestStation.distanceToDrone <= Constants.ACCESS_RANGE && nearestStation.coins != 0 
							&& nearestStation.type == ChargingStation.LIGHTHOUSE) 
					{
							isMoved = move(d);
							nearestStation.transferCoins(this);
							nearestStation.transferPower(this);
							points.add(positionToPoint(position));
							break;
					}
					
					if (nearestStation.type != ChargingStation.SKULL) {
						validDirection.add(d);
					}
				}
			}
			
			// Random move
			if (!isMoved && !validDirection.isEmpty()) {
				int idx = rnd.nextInt(validDirection.size());
				Direction nextd = validDirection.get(idx);
				isMoved = super.move(nextd);
				points.add(positionToPoint(position));
			}
			
			System.out.println(points.size()-1 + " - Coins: " + coins + "; Power: " + power);
		}
		
		LineString ls = LineString.fromLngLats(points);
		Feature f = Feature.fromGeometry(ls, new JsonObject());
		return f;
	}
}
