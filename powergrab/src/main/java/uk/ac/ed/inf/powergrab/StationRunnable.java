package uk.ac.ed.inf.powergrab;

public class StationRunnable implements Runnable {
	
	private final double receiveRange = 0.00025;
	private final ChargingStation mStation;
	private final Drone mDrone;
	private final int SKULL_NEARBY = 2;
	private final int LIGHTHOUSE_NEARBY = 1;
	private final int COMPUTE_COMPLETE = 0;
	
	
	StationRunnable(ChargingStation station, Drone drone) {
		mStation = station;
		mDrone = drone;
	}
	
	@Override
	public void run() {
		// Get current thread
		mStation.setThread(Thread.currentThread());
		
		if (Thread.interrupted()) {
            // Interrupted
        }
		
        // check if drone is in range
    	double distance = Util.pythagoreanDistance(mDrone.position, mStation.position);
        if (distance <= receiveRange) {
        	if (mStation.type == ChargingStation.SKULL) {
        		mStation.handleRunnableStates(SKULL_NEARBY);
        	} else {
        		mStation.handleRunnableStates(LIGHTHOUSE_NEARBY);
        	}
        }
		mStation.handleRunnableStates(COMPUTE_COMPLETE);
	}
	
}
