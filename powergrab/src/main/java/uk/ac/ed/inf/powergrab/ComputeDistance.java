package uk.ac.ed.inf.powergrab;

import java.util.List;
import java.util.concurrent.Callable;

public class ComputeDistance implements Callable<ChargingStation> {
	
	private List<ChargingStation> mStations;
	private Position mDrone;
	
	public ComputeDistance(List<ChargingStation> stations, Position drone) {
		this.mStations = stations;
		this.mDrone = drone;
	}

	@Override
	public ChargingStation call() throws Exception {
		ChargingStation minStation = mStations.get(0);
		double min = minStation.distanceToDrone(mDrone);
		double distance = 0;
		int length = mStations.size();
		
		for (int i=1; i<length; i++) {
			ChargingStation s = mStations.get(i);
			distance = s.distanceToDrone(mDrone);
			if (distance < min) {
				min = distance;
				minStation = s;
			}
		}
		
		return minStation;
	}
	
}
