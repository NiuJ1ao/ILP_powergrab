package uk.ac.ed.inf.powergrab;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MyTest {
	
	boolean approxEq(double d0, double d1) {
		final double epsilon = 1.0E-12d;
		return Math.abs(d0 - d1) < epsilon;
	}

	boolean approxEq(Position p0, Position p1) {
		return approxEq(p0.latitude, p1.latitude) && approxEq(p0.longitude, p1.longitude); 
	}
    
	Drone testDrone = new StatelessDrone(new Position(55.944425, -3.188396), 1);
	ChargingStation station = new ChargingStation("1", 0, 0, "1", "1", new Position(1,1));
	@Test
	public void testPosCoins() {
		testDrone.transferCoins(10.0);
		assertTrue(approxEq(testDrone.coins, 10.0));
	}
	
	@Test
	public void testNegCoins() {
		testDrone.transferCoins(-20.0);
		assertTrue(approxEq(testDrone.coins, 0.0));
	}
	
	@Test
	public void testPosPower() {
		testDrone.transferPower(20.0);
		assertTrue(approxEq(testDrone.power, 250.0));
	}
	
	@Test
	public void testNegPower() {
		testDrone.transferPower(-200.0);
		assertTrue(approxEq(testDrone.power, 50.0));
	}
	
	@Test
	public void testNegPower1() {
		testDrone.transferPower(-270.0);
		assertTrue(approxEq(testDrone.power, 0.0));
	}
	
	@Test
	public void testMove() {
		testDrone.move(Direction.N);
		assertTrue(approxEq(testDrone.power, 250-1.25));
	}
	
	@Test
	public void testStationTransferCoins() {
		station.coins = 20;
		testDrone.move(Direction.S);
		System.out.println(station.coins);
		station.transferCoins(testDrone);
		System.out.println(station.coins);
		station.coins = -15;
		System.out.println(station.coins);
		station.transferCoins(testDrone);
		System.out.println(station.coins);
		station.coins = -20;
		System.out.println(station.coins);
		station.transferCoins(testDrone);
		System.out.println(station.coins);
		assertTrue(approxEq(station.coins, -15));
	}
	
	@Test
	public void testStationTransferPower() {
		station.power = 30;
		testDrone.move(Direction.S);
		station.transferPower(testDrone);
		station.power = -270;
		station.transferPower(testDrone);
		assertTrue(approxEq(station.power, -20));
	}
}

