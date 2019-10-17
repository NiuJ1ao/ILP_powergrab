package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

public class StatelessDrone extends Drone{

	public StatelessDrone(Position p, long seed) {
		super(p, seed);
	}
	
	@Override
	public Feature strategy() {
		ChargingStation stationToConnect = null;
		
		while (isGameOver()) {
			
			for (Direction d : Direction.values()) {
				Position nextP = position.nextPosition(d);
				for (ChargingStation s : App.stations) {
					if (s.getType() == ChargingStation.SKULL) {
						break;
					}
					Position stationP = s.getPosition();
					double distance = Math.sqrt(Math.pow(stationP.latitude - nextP.latitude, 2) 
												+ Math.pow(stationP.longitude - nextP.longitude, 2));
					if (distance <= ACCESSRANGE) {
						stationToConnect = s;
						break;
					}
				}
				
				if (!stationToConnect.equals(null)) {
					move(d);
					
					break;
				}
			}
			
			if (stationToConnect.equals(null)) {
				// random move;
			}
			
			
		}
		
		return null;
	}
}
