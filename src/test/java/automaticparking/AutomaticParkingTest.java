package automaticparking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import automaticparking.ParkingMap.SpotStatus;

class AutomaticParkingTest {

	private static final int[] CLEAN = {100};

	@Test
	public void WhereIsTestStart() {
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());
		
		CarInfo CarInfo = ap.whereIs();
		
		assertEquals(0, CarInfo.position, "Position should be 0 at start.");
		assertFalse(CarInfo.isParked, "Car should not be parked at start");
	}
	
	@Test
	public void WhereIsTestAfterMoveThreeSteps() {
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());

		ap.MoveForward();
		ap.MoveForward();
		ap.MoveForward();
		CarInfo CarInfo = ap.whereIs();
		
		assertEquals(3, CarInfo.position, "Position should be at position 3");
		assertFalse(CarInfo.isParked, "Car should not be parked at start");
	}
	
	@Test
	public void WhereIsTestAfterMove1000Steps() {
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());
		
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
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free), new CarActuator());

		ap.Park();

		assertEquals(0, ap.whereIs().position, "Car should park at 0");
		assertTrue(ap.whereIs().isParked, "Car should be parked");
	}
	
	@Test
	public void MoveForwardTestOneStepFromStart()
	{
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());
		
		ParkingRecord pr = ap.MoveForward();
		
		assertEquals(1, pr.position, "Position should be at 1 after a step from start");
	}

	@Test
	public void MoveForwardMoreThanStreetLength()
	{
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());

		ParkingRecord pr = null;
		for (int i = 0; i < 1000; i++)
			pr = ap.MoveForward();

		assertEquals(495, pr.position, "Car position should not exceed 495");

	}

	@Test
	public void MoveForwardWhileParkedDoesNothing()
	{
		int[] free = {150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free), new CarActuator());

		ap.Park();
		int parkedAt = ap.whereIs().position;
		ap.MoveForward();

		assertEquals(parkedAt, ap.whereIs().position, "A parked car should not move");
		assertTrue(ap.whereIs().isParked);
	}

	@Test
	public void MoveForwardRecordsFreeAt100()
	{
		int[] reading = {100};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(reading), new FakeSensor(reading), new CarActuator());

		ParkingRecord pr = ap.MoveForward();

		assertEquals(SpotStatus.FREE, pr.parkingmap.getSpotStatus(0), "100 cm counts as FREE");
	}

	@Test
	public void MoveForwardRecordsBlockedAt99()
	{
		int[] reading = {99};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(reading), new FakeSensor(reading), new CarActuator());

		ParkingRecord pr = ap.MoveForward();

		assertEquals(SpotStatus.BLOCKED, pr.parkingmap.getSpotStatus(0), "99 cm counts as BLOCKED");


	}

	@Test
	public void MoveForwardKeepsAlreadyRecordedSpot()
	{
		int[] blocked = {0};
		int[] free = {150};
		FakeSensor A = new FakeSensor(blocked);
		FakeSensor B = new FakeSensor(blocked);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		ap.MoveForward();                 // meter 0 is measured as BLOCKED
		ap.MoveBackwards();               // back to position 0
		A.setScript(free); B.setScript(free);
		ParkingRecord pr = ap.MoveForward();

		assertEquals(1, pr.position, "One step forward from 0 is 1");
		assertEquals(SpotStatus.BLOCKED, pr.parkingmap.getSpotStatus(0), "An already measured meter is never overwritten");
	}

	@Test
	public void MoveBackwardsFromStart()
	{
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());

		ParkingRecord pr = ap.MoveBackwards();
		assertEquals(0, pr.position, "Position should not go below 0");
	}

	@Test
	public void MoveBackwardsFromPositionFive()
	{
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());

		for (int i = 0; i < 5; i++) { ap.MoveForward(); }

		ParkingRecord pr = ap.MoveBackwards();
		assertEquals(4, pr.position, "Position should be at 4");
	}

	@Test
	public void MoveBackwardsFromEnd()
	{
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());
		ParkingRecord pr = null;
		for (int i = 0; i < 1000; i++) ap.MoveForward();

		pr = ap.MoveBackwards();

		assertEquals(494, pr.position, "Position should be 494 after one step back from the end");
	}

	@Test
	public void MoveBackwardsWhileParkedDoesNothing()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(blocked);
		FakeSensor B = new FakeSensor(blocked);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 10; i++) ap.MoveForward();
		A.setScript(free); B.setScript(free);
		ap.Park();
		ap.MoveBackwards();

		assertEquals(10, ap.whereIs().position, "A parked car should not move");
		assertTrue(ap.whereIs().isParked);
	}

	@Test
	public void isEmptyTest()
	{
		int[] Script1 = {125, 100, 90, 110, 195};
		int[] Script2 = {100, 100, 100, 100, 100};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(Script1), new FakeSensor(Script2), new CarActuator());

		ap.isEmpty();
		assertEquals(100, ap.isEmpty(), "Value should be 100 on the first run.");
		assertEquals(100, ap.isEmpty(), "Value should be 100 on the second run.");
	}
	@Test
	public void isEmptyNoisySensorARetiresOnFifthCall()
	{
		int[] noisy = {125, 100, 90, 110, 195};
		int[] clean = {150, 150, 150, 150, 150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(noisy), new FakeSensor(clean), new CarActuator());

		assertEquals(124, ap.isEmpty(), "error 0.23, A trusted -> min(124,150)");
		assertEquals(124, ap.isEmpty(), "error 0.46, A trusted");
		assertEquals(124, ap.isEmpty(), "error 0.70, A trusted");
		assertEquals(124, ap.isEmpty(), "error 0.93, A still trusted on the fourth call");
		assertEquals(150, ap.isEmpty(), "error 1.16, A is retired on the fifth call");
		assertEquals(150, ap.isEmpty(), "A stays retired");
	}
	@Test
	public void isEmptyNoisySensorA()
	{
		int[] noisy = {40, 100, 65, 40, 70};
		int[] clean = {150, 150, 150, 150, 150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(noisy), new FakeSensor(clean), new CarActuator());

		assertEquals(63,  ap.isEmpty(), "error 0.29, still trusted");
		assertEquals(63,  ap.isEmpty(), "error 0.58, still trusted");
		assertEquals(63,  ap.isEmpty(), "error 0.88, last trusted");
		assertEquals(150, ap.isEmpty(), "error 1.17");
		assertEquals(150, ap.isEmpty(), "A stays retired");
	}

	@Test
	public void isEmptyNoisySensorB()
	{
		int[] clean = {150, 150, 150, 150, 150};
		int[] noisy = {40, 100, 65, 40, 70};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(clean), new FakeSensor(noisy), new CarActuator());

		assertEquals(63,  ap.isEmpty(), "error 0.29, B still trusted -> min(150,63)");
		assertEquals(63,  ap.isEmpty(), "error 0.58, still trusted");
		assertEquals(63,  ap.isEmpty(), "error 0.88, last trusted");
		assertEquals(150, ap.isEmpty(), "error 1.17, B retired -> only A");
		assertEquals(150, ap.isEmpty(), "B stays retired");
	}

	@Test
	public void isEmptyBothSensorsNoisy()
	{
		int[] noisy = {40, 100, 65, 40, 70};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(noisy), new FakeSensor(noisy), new CarActuator());

		assertEquals(63, ap.isEmpty(), "both trusted");
		assertEquals(63, ap.isEmpty(), "both trusted");
		assertEquals(63, ap.isEmpty(), "both trusted, last call");
		assertEquals(0,  ap.isEmpty(), "both retired -> 0");
		assertEquals(0,  ap.isEmpty(), "both stay retired");
	}

	@Test
	public void ParkTest()
	{
		int[] ScriptFree = {100};
		int[] ScriptBlock = {0};

		FakeSensor A = new FakeSensor(ScriptBlock);
		FakeSensor B = new FakeSensor(ScriptBlock);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 10; i++) ap.MoveForward();
		A.setScript(ScriptFree);
		B.setScript(ScriptFree);
		ap.Park();
		assertEquals(10, ap.whereIs().position);
		assertTrue(ap.whereIs().isParked);
	}

	@Test
	public void ParkUsesFreeStretchBehindCar()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(free);
		FakeSensor B = new FakeSensor(free);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 10; i++) ap.MoveForward();
		A.setScript(blocked); B.setScript(blocked);
		for (int i = 0; i < 190; i++) ap.MoveForward();
		A.setScript(free); B.setScript(free);

		ap.Park();

		assertEquals(0, ap.whereIs().position, "Should reuse the detected stretch at 0");
		assertTrue(ap.whereIs().isParked);
	}

	@Test
	public void ParkNoFreeStretch()
	{
		int[] blocked = {0};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(blocked), new FakeSensor(blocked), new CarActuator());

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
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 10; i++) ap.MoveForward();
		A.setScript(free); B.setScript(free);
		ap.Park();
		ap.Park();

		assertEquals(10, ap.whereIs().position, "Second Park should not move the car");
		assertTrue(ap.whereIs().isParked);
	}

	@Test
	public void ParkFourFreeMetersIsNotEnough()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(free);
		FakeSensor B = new FakeSensor(free);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 4; i++) ap.MoveForward();   // meters 0-3 are FREE
		A.setScript(blocked); B.setScript(blocked);     // the rest of the street is BLOCKED
		ap.Park();

		assertEquals(495, ap.whereIs().position, "4 free meters are not a parking place");
		assertFalse(ap.whereIs().isParked, "The car should not park on 4 free meters");
	}

	@Test
	public void ParkAtTheEndOfTheStreet()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(blocked);
		FakeSensor B = new FakeSensor(blocked);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 490; i++) ap.MoveForward(); // meters 0-489 are BLOCKED
		A.setScript(free); B.setScript(free);           // meters 490-494 are FREE
		ap.Park();

		assertEquals(490, ap.whereIs().position, "The last parking place starts at meter 490");
		assertTrue(ap.whereIs().isParked, "The car should park in the last parking place");
	}

	@Test
	public void UnParkMovesOut()
	{
		int[] free = {150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free), new CarActuator());

		ap.Park();
		ap.UnPark();

		assertEquals(5, ap.whereIs().position, "Car should move 5 m forward out of the spot");
		assertFalse(ap.whereIs().isParked, "Car should no longer be parked");
	}

	@Test
	public void UnParkWhenNotParked()
	{
		AutomaticParking ap = new AutomaticParking(new FakeSensor(CLEAN), new FakeSensor(CLEAN), new CarActuator());

		ap.UnPark();

		assertEquals(0, ap.whereIs().position, "UnPark on an unparked car should not move it");
		assertFalse(ap.whereIs().isParked);
	}

	@Test
	public void MoveForwardAccumulatesMap()
	{
		int[] free = {150};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(free), new FakeSensor(free), new CarActuator());

		ap.MoveForward();
		ap.MoveForward();
		ParkingRecord pr = ap.MoveForward();

		assertEquals(3, pr.position, "Three steps should put the car at 3");
		assertEquals(SpotStatus.FREE, pr.parkingmap.getSpotStatus(0), "Metre 0 was detected on the first move");
		assertEquals(SpotStatus.FREE, pr.parkingmap.getSpotStatus(1), "Metre 1 was detected on the second move");
		assertEquals(SpotStatus.FREE, pr.parkingmap.getSpotStatus(2), "Metre 2 was detected on the third move");
		assertEquals(SpotStatus.UNKNOWN, pr.parkingmap.getSpotStatus(3), "The metre under the car has not been driven past yet");
	}

	@Test
	public void MoveBackwardsKeepsAlreadyRecordedSpot()
	{
		int[] blocked = {0};
		int[] free = {150};
		FakeSensor A = new FakeSensor(blocked);
		FakeSensor B = new FakeSensor(blocked);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 3; i++) ap.MoveForward();
		A.setScript(free); B.setScript(free);
		ParkingRecord pr = ap.MoveBackwards();

		assertEquals(2, pr.position, "One step back from 3 is 2");
		assertEquals(SpotStatus.BLOCKED, pr.parkingmap.getSpotStatus(2), "An already detected metre is never overwritten");
	}

	@Test
	public void SimulatedSensorStaysInRange()
	{
		SimulatedSensor s = new SimulatedSensor();

		for (int i = 0; i < 1000; i++) {
			int reading = s.read();
			assertTrue(reading >= 0 && reading <= 200, "Reading outside the 0-200 cm range: " + reading);
		}
	}

	@Test
	public void isEmptyBothSensorsBroken()
	{
		int[] tooHigh = {999};
		int[] tooLow = {-1};
		AutomaticParking ap = new AutomaticParking(new FakeSensor(tooHigh), new FakeSensor(tooLow), new CarActuator());

		assertEquals(0, ap.isEmpty(), "Neither sensor is in range, so nothing can be trusted");
	}

	@Test
	public void ParkChoosesSmallestFreeStretch()
	{
		int[] free = {150};
		int[] blocked = {0};
		FakeSensor A = new FakeSensor(free);
		FakeSensor B = new FakeSensor(free);
		AutomaticParking ap = new AutomaticParking(A, B, new CarActuator());

		for (int i = 0; i < 8; i++) ap.MoveForward();   // meters 0-7 FREE (8 m)
		A.setScript(blocked); B.setScript(blocked);
		for (int i = 0; i < 12; i++) ap.MoveForward();  // meters 8-19 BLOCKED
		A.setScript(free); B.setScript(free);
		for (int i = 0; i < 6; i++) ap.MoveForward();   // meters 20-25 FREE (6 m)
		A.setScript(blocked); B.setScript(blocked);     // rest of the street BLOCKED

		ap.Park();

		assertEquals(20, ap.whereIs().position, "Should choose the 6 m place, not the 8 m one");
		assertTrue(ap.whereIs().isParked);
	}
}
