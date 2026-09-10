package automaticparking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import automaticparking.ParkingMap.SpotStatus;

class AutomaticParkingTest {

	@Test
	public void WhereIsTestStart() {
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		CarInfo CarInfo = ap.whereIs();
		
		assertEquals(0, CarInfo.position, "Position should be 0 at start.");
		assertFalse(CarInfo.isParked, "Car should not be parked at start");
	}
	
	@Test
	public void WhereIsTestAfterMoveThreeSteps() {
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());

		ap.MoveForward();
		ap.MoveForward();
		ap.MoveForward();
		CarInfo CarInfo = ap.whereIs();
		
		assertEquals(3, CarInfo.position, "Position should be at position 3");
		assertFalse(CarInfo.isParked, "Car should not be parked at start");
	}
	
	@Test
	public void WhereIsTestAfterMove1000Steps() {
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		for (int i = 0; i < 1000; i++)
			ap.MoveForward();
		
		CarInfo CarInfo = ap.whereIs();
		
		assertEquals(495, CarInfo.position, "Position should be at position 495");
		assertFalse(CarInfo.isParked, "Car should not be parked at start");
	}
	
	@Test
	public void WhereIsAfterPark()
	{
		int[] free = {150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free));

		ap.Park();

		assertEquals(0, ap.whereIs().position, "Car should park at 0");
		assertTrue(ap.whereIs().isParked, "Car should be parked");
	}
	
	@Test
	public void MoveForwardTestOneStepFromStart()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		ParkingRecord pr = ap.MoveForward();
		
		assertEquals(1, pr.position, "Position should be at 1 after a step from start");
	}

	@Test
	public void MoveForwardMoreThanStreetLength()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		ParkingRecord pr = ap.MoveForward();
		
		for (int i = 0; i < 1000; i++)
			pr = ap.MoveForward();
		
		assertEquals(495, pr.position, "Car position should not exceed 495");
	}
	
	@Test
	public void MoveForwardWhileParkedDoesNothing()
	{
		int[] free = {150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free));

		ap.Park();                               
		int parkedAt = ap.whereIs().position;
		ap.MoveForward();	

		assertEquals(parkedAt, ap.whereIs().position, "A parked car should not move");
		assertTrue(ap.whereIs().isParked);
	}
	
	@Test
	public void MoveForwardRecordsFreeAt50()
	{
		int[] reading = {50};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(reading), new FakeSensor(reading));

		ParkingRecord pr = ap.MoveForward();

		assertEquals(SpotStatus.FREE, pr.parkingmap.getSpotStatus(0), "50 cm counts as FREE");
	}
	
	@Test
	public void MoveForwardRecordsBlockedAt49()
	{
		int[] reading = {49};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(reading), new FakeSensor(reading));

		ParkingRecord pr = ap.MoveForward();

		assertEquals(SpotStatus.BLOCKED, pr.parkingmap.getSpotStatus(0), "49 cm counts as BLOCKED");
	}
	
	@Test
	public void MoveBackwardsFromStart()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		ParkingRecord pr = ap.MoveBackwards();
		assertEquals(0, pr.position, "Position should not go below 0");
	}
	
	@Test
	public void MoveBackwardsFromPositionFive()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());
		
		for (int i = 0; i < 5; i++) { ap.MoveForward(); }
		
		ParkingRecord pr = ap.MoveBackwards();
		assertEquals(4, pr.position, "Position should be at 4");
	}
	
	@Test
	public void MoveBackwardsFromEnd()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());

		for (int i = 0; i < 1000; i++) ap.MoveForward();   // car at 495
		ParkingRecord pr = ap.MoveBackwards();

		assertEquals(494, pr.position, "Position should be 494 after one step back from the end");
	}
	
	@Test
	public void MoveBackwardsWhileParkedDoesNothing()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(blocked);
		FakeSensor B = new FakeSensor(blocked);
		AutomaticParking ap = new AutomaticParking(A, B);

		for (int i = 0; i < 10; i++) ap.MoveForward();     // spots 0-9 blocked
		A.setScript(free); B.setScript(free);
		ap.Park();                                         // parks at 10
		ap.MoveBackwards();

		assertEquals(10, ap.whereIs().position, "A parked car should not move");
		assertTrue(ap.whereIs().isParked);
	}
	
	@Test
	public void isEmptyTest()
	{
		int[] Script1 = {125, 100, 90, 110, 195}; 
		int[] Script2 = {100, 100, 100, 100, 100};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(Script1), new FakeSensor(Script2));
		
		ap.isEmpty();
		assertEquals(100, ap.isEmpty(), "Value should be 100 on the first run.");
		assertEquals(100, ap.isEmpty(), "Value should be 100 on the second run.");
	}
	
	@Test
	public void isEmptyNoisySensorA()
	{
		int[] noisy = {40, 100, 65, 40, 70};      // avg 63, 0.292 error per call
		int[] clean = {150, 150, 150, 150, 150};  // error 0
		AutomaticParking ap = new AutomaticParking(new FakeSensor(noisy), new FakeSensor(clean));

		assertEquals(63,  ap.isEmpty(), "error 0.29, still trusted");
		assertEquals(63,  ap.isEmpty(), "error 0.58, still trusted");
		assertEquals(63,  ap.isEmpty(), "error 0.88, last trusted");
		assertEquals(150, ap.isEmpty(), "error 1.17");
		assertEquals(150, ap.isEmpty(), "A stays retired");
	}
	
	@Test
	public void isEmptyNoisySensorB()
	{
		int[] clean = {150, 150, 150, 150, 150};  // error 0
		int[] noisy = {40, 100, 65, 40, 70};      // avg 63, 0.292 error per call
		AutomaticParking ap = new AutomaticParking(new FakeSensor(clean), new FakeSensor(noisy));

		assertEquals(63,  ap.isEmpty(), "error 0.29, B still trusted -> min(150,63)");
		assertEquals(63,  ap.isEmpty(), "error 0.58, still trusted");
		assertEquals(63,  ap.isEmpty(), "error 0.88, last trusted");
		assertEquals(150, ap.isEmpty(), "error 1.17, B retired -> only A");
		assertEquals(150, ap.isEmpty(), "B stays retired");
	}
	
	@Test
	public void isEmptyBothSensorsNoisy()
	{
		int[] noisy = {40, 100, 65, 40, 70};      // avg 63, 0.292 error per call
		AutomaticParking ap = new AutomaticParking(new FakeSensor(noisy), new FakeSensor(noisy));

		assertEquals(63, ap.isEmpty(), "both trusted");
		assertEquals(63, ap.isEmpty(), "both trusted");
		assertEquals(63, ap.isEmpty(), "both trusted, last call");
		assertEquals(0,  ap.isEmpty(), "both retired -> 0");
		assertEquals(0,  ap.isEmpty(), "both stay retired");
	}
	
	@Test
	public void ParkTest()
	{
		int[] ScriptFree = {50};
		int[] ScriptBlock = {0};

		FakeSensor A = new FakeSensor(ScriptBlock);
		FakeSensor B = new FakeSensor(ScriptBlock);
		AutomaticParking ap = new AutomaticParking(A, B);
		
		for (int i = 0; i < 10; i++) ap.MoveForward();
		A.setScript(ScriptFree);
		B.setScript(ScriptFree);
		ap.Park();
		assertEquals(10, ap.getCarInfo().position);
		assertTrue(ap.getCarInfo().isParked);
	}
	
	@Test
	public void ParkUsesFreeStretchBehindCar()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(free);
		FakeSensor B = new FakeSensor(free);
		AutomaticParking ap = new AutomaticParking(A, B);

		for (int i = 0; i < 10; i++) ap.MoveForward();     // spots 0-9 free
		A.setScript(blocked); B.setScript(blocked);
		for (int i = 0; i < 190; i++) ap.MoveForward();    // spots 10-199 blocked, car at 200
		A.setScript(free); B.setScript(free);

		ap.Park();

		assertEquals(0, ap.whereIs().position, "Should reuse the detected stretch at 0");
		assertTrue(ap.whereIs().isParked);
	}

	@Test
	public void ParkNoFreeStretch()
	{
		int[] blocked = {0};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(blocked), new FakeSensor(blocked));

		ap.Park();

		assertEquals(495, ap.whereIs().position, "Car should have searched to the end of the street");
		assertFalse(ap.whereIs().isParked, "No free stretch, so the car should not be parked");
	}

	@Test
	public void ParkWhenAlreadyParked()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(blocked);
		FakeSensor B = new FakeSensor(blocked);
		AutomaticParking ap = new AutomaticParking(A, B);

		for (int i = 0; i < 10; i++) ap.MoveForward();     // spots 0-9 blocked
		A.setScript(free); B.setScript(free);
		ap.Park();                                         // parks at 10
		ap.Park();                                         // second call should do nothing

		assertEquals(10, ap.whereIs().position, "Second Park should not move the car");
		assertTrue(ap.whereIs().isParked);
	}
	
	@Test
	public void UnParkMovesOut()
	{
		int[] free = {150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free));

		ap.Park();                                         // parks at 0
		ap.UnPark();

		assertEquals(5, ap.whereIs().position, "Car should move 5 m forward out of the spot");
		assertFalse(ap.whereIs().isParked, "Car should no longer be parked");
	}

	@Test
	public void UnParkWhenNotParked()
	{
		AutomaticParking ap = new AutomaticParking(new SimulatedSensor(), new SimulatedSensor());

		ap.UnPark();

		assertEquals(0, ap.whereIs().position, "UnPark on an unparked car should not move it");
		assertFalse(ap.whereIs().isParked);
	}
}
