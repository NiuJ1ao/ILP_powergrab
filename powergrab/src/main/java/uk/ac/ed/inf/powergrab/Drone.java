package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;


public abstract class Drone {
	
	public Position position;
	public double coins;
	public double power;
	protected Random rnd;
	protected final double POWER_CONSUMPTION = 1.25;
	private int steps;
	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	protected ChargingStation closestStation = null;
	
	public Drone(Position p, long seed) {
		this.position = p;
		this.coins = 0;
		this.power = 250;
		this.rnd = new Random(seed);
		this.steps = 0;
	}
	
	public boolean move(Direction d) throws Exception {
		Position p = position.nextPosition(d);
			
		if (p.inPlayArea()) {			
			position = p;
			power = power - POWER_CONSUMPTION;
			steps++;
			
			closestStation = findClosestStation(p);
			if (closestStation.getDistance() < Constants.ACCESS_RANGE) {
				closestStation.transferCoins(this);
				closestStation.transferPower(this);
			}
			
			return true;
		}
		return false;
	}
	
	public ChargingStation findClosestStation(Position p) throws InterruptedException, ExecutionException {
		//List<Future<ChargingStation>> futures = executorService.invokeAll(createSubtasks(p));
		List<Future<ChargingStation>> futures = new ArrayList<Future<ChargingStation>>();
		List<ComputeDistance> callables = createSubtasks(p);
		
		for(int i=0; i< callables.size(); i++){
            //submit Callable tasks to be executed by thread pool
            Future<ChargingStation> future = executorService.submit(callables.get(i));
            //add Future to the list, we can get return value using Future
            futures.add(future);
        }

		ChargingStation minStation = futures.get(0).get();
		for (int i=1; i<5; i++) {
			ChargingStation curStation = futures.get(i).get();
			if (curStation.getDistance() < minStation.getDistance()) {
				minStation = curStation;
			}
		}
		
		executorService.shutdown();
//		ChargingStation minStation = App.stations.get(0);
//		minStation.distanceToDrone(this.position);
//		for (int i=1; i<App.stations.size(); i++) {
//			if (App.stations.get(i).distanceToDrone(this.position) < minStation.getDistance()) {
//				minStation = App.stations.get(i);
//			}
//		}
		
		return minStation;
	}
	
	private List<ComputeDistance> createSubtasks(Position p) {
		List<ChargingStation> mStations = App.stations;
		List<ComputeDistance> dividedTasks = new ArrayList<>();
		dividedTasks.add(new ComputeDistance(mStations.subList(0, 10), p));
		dividedTasks.add(new ComputeDistance(mStations.subList(10, 20), p));
		dividedTasks.add(new ComputeDistance(mStations.subList(20, 30), p));
		dividedTasks.add(new ComputeDistance(mStations.subList(30, 40), p));
		dividedTasks.add(new ComputeDistance(mStations.subList(40, 50), p));
		return dividedTasks;	
	}
	
	public double transferCoins(double amount) {
		double sum = coins + amount;
		
		if (sum < Constants.MINPAYLOAD) {
			amount = -coins;
			coins = Constants.MINPAYLOAD;
			return amount;
		} else {
			coins = sum;
			return amount;
		}
	}
	
	public double transferPower(double amount) {
		double sum = power + amount;
		
		if (sum >= Constants.MAXPOWER) {
			amount = power;
			power = Constants.MAXPOWER;
			return Constants.MAXPOWER - amount;
		} else if (sum < Constants.MINPAYLOAD) {
			amount = -power;
			power = Constants.MINPAYLOAD;
			return amount;
		} else {
			power = sum;
			return amount;
		}
	}
	
	public boolean isGameOver() {
		return power < POWER_CONSUMPTION || steps == 250;
	}
	
	public Point positionToPoint(Position position) {
		return Point.fromLngLat(position.longitude, position.latitude);
	}
	
	public void rollBack (Drone prevStatus) {
		this.coins = prevStatus.coins;
		this.position = prevStatus.position;
		this.power = prevStatus.power;
		this.steps = prevStatus.steps;
	}
	
	public abstract Feature strategy() throws Exception;
}
