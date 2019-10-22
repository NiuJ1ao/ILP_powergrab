package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mapbox.geojson.Point;

public class StationsManager {
	
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
	private static StationsManager sInstance = null;
	private LinkedBlockingQueue<ChargingStation> mStationsWorkQueue; 
	
	static {
		// The time unit for "keep alive" is in seconds
		KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

		// Creates a single static instance of PhotoManager
	    sInstance = new StationsManager();
	}
	
	private final LinkedBlockingQueue<Runnable> mComputeWorkQueue;
	private int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
	private long KEEP_ALIVE_TIME = 1L;
	private final ThreadPoolExecutor mComputingThreadPool;
	
	private StationsManager() {
		/*
	     * Creates a work queue for the pool of Thread objects used for downloading, using a linked
	     * list queue that blocks when the queue is empty.
	     */
		mComputeWorkQueue = new LinkedBlockingQueue<Runnable>();
		
		for (ChargingStation s : App.stations) {
			mStationsWorkQueue.add(s);
		}

	    /*
	     * Creates a new pool of Thread objects for the download work queue
	     */
	    mComputingThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
	            KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mComputeWorkQueue);
	}

	public static StationsManager getInstance() {
		return sInstance;
	}
	
	private Direction d;
	private ArrayList<Point> points;
	public void computeDistance(Direction d, ArrayList<Point> points) {
		ChargingStation station  = sInstance.mStationsWorkQueue.poll();
		sInstance.d = d;
		sInstance.points = points;
		sInstance.mComputingThreadPool.execute(station.getRunnable());
	}
	
	static final int SKULL_NEARBY = 2;
	static final int LIGHTHOUSE_NEARBY = 1;
	static final int COMPUTE_COMPLETE = 0;
	private Drone drone = App.testDrone;
	public void handleState(ChargingStation station, int state) {
		switch (state) {
			case SKULL_NEARBY:
				
			case LIGHTHOUSE_NEARBY:
				if (sInstance.drone.move(sInstance.d)) {
					sInstance.points.add(sInstance.drone.positionToPoint(sInstance.drone.position));
					station.transferCoins(sInstance.drone);
					station.transferPower(sInstance.drone);
				}
			case COMPUTE_COMPLETE:
		}
	}
}
