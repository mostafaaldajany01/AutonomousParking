package automaticparking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import automaticparking.ParkingMap.SpotStatus;

class AutomaticParkingTest {

	@Test
	public void whereIsTestStart() {
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		CarStatus carstatus = ap.whereIs();
		
		assertEquals(0, carstatus.getPosition(), "Position should be 0 at start.");
		assertFalse(carstatus.getParkStatus(), "Car should not be parked at start");
	}
	
	@Test
	public void whereIsTestAfterMoveThreeSteps() {
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());

		ap.MoveForward();
		ap.MoveForward();
		ap.MoveForward();
		CarStatus carstatus = ap.whereIs();
		
		assertEquals(3, carstatus.getPosition(), "Position should be at position 3");
		assertFalse(carstatus.getParkStatus(), "Car should not be parked at start");
	}
	
	@Test
	public void whereIsTestAfterMove1000Steps() {
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		for (int i = 0; i < 1000; i++)
			ap.MoveForward();
		
		CarStatus carstatus = ap.whereIs();
		
		assertEquals(499, carstatus.getPosition(), "Position should be at position 499");
		assertFalse(carstatus.getParkStatus(), "Car should not be parked at start");
	}
	
	@Test
	public void moveForwardTestOneStepFromStart()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		ParkingRecord pr = ap.MoveForward();
		
		assertEquals(1, pr.position, "Position should be at 1 after a step from start");
	}

	@Test
	public void moveForwardMoreThanStreetLength()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		ParkingRecord pr = ap.MoveForward();
		
		for (int i = 0; i < 1000; i++)
			pr = ap.MoveForward();
		
		assertEquals(499, pr.position, "Position should not exceed 499");
	}
	
	@Test
	public void moveBackwardsFromStart()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		ParkingRecord pr = ap.MoveBackwards();
		assertEquals(0, pr.position, "Position should not go below 0");
	}
	
	@Test
	public void moveBackwardsFromPositionFive()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		for (int i = 0; i < 5; i++) { ap.MoveForward(); }
		
		ParkingRecord pr = ap.MoveBackwards();
		assertEquals(4, pr.position, "Position should be at 4");
	}
	
	@Test
	public void isEmptyTest()
	{
		int[] Script1 = {100, 105, 95, 100, 99, 50, 60,65,75, 40};
		int[] Script2 = {97, 90, 96, 102, 102, 55, 54,78, 81,40};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(Script1), new FakeSensor(Script2));
		
		assertEquals(99, ap.isEmpty(), "Value should be 99 on the first run.");
		assertEquals(61, ap.isEmpty(), "Value should be 59 on the second run.");
	}
	
	@Test
	public void testPark()
	{
		int[] ScriptFree = {50};
		int[] ScriptBlock = {0};

		FakeSensor A = new FakeSensor(ScriptBlock);
		FakeSensor B = new FakeSensor(ScriptBlock);
		AutomaticParking ap = new AutomaticParking(A, B);
		
		for (int i = 0; i < 494; i++) ap.MoveForward();
		A.setScript(ScriptFree);
		B.setScript(ScriptFree);
		for (int i = 0; i < 5; i++) ap.MoveForward();
		
		assertEquals(ap.Park(), ap.getCarStatus().getPosition());
		
	}

}
