package uk.ac.ed.inf.powergrab;

public class MyRunnable implements Runnable {

	private ChargingStation mStation;
	
	public MyRunnable(ChargingStation s) {
		mStation = s;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mStation.distanceToDrone();
	}

}
