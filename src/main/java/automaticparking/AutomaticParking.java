package automaticparking;

import automaticparking.ParkingMap.SpotStatus;

public class AutomaticParking {
	private Sensor sensor_a;
	private Sensor sensor_b;

	private Actuator actuator;

	private double sensor_a_error = 0.0;
	private double sensor_b_error = 0.0;

	private ParkingMap parkingmap;
	private boolean isParked;
	private static final int STREET_LENGTH = 500;
	private static final int CAR_LENGTH = 5;

	private static final int SENSOR_SIM_AMOUNT = 5;

	public AutomaticParking(Sensor A, Sensor B, Actuator actuator) {
		this.sensor_a = A;
		this.sensor_b = B;
		this.actuator = actuator;

		isParked = false;
		parkingmap = new ParkingMap(STREET_LENGTH);
	}

	/**
	 * Description: Returns the car's current position and whether it is parked.
	 * Pre-condition: none
	 * Post-condition: State is unchanged. Returns a copy of the CarInfo holding the
	 * current position (0-495) and parked status.
	 * Test-cases: WhereIsTestStart, WhereIsTestAfterMoveThreeSteps,
	 * WhereIsTestAfterMove1000Steps, WhereIsAfterPark
	 */
	public CarInfo whereIs() {
		CarInfo info = new CarInfo();
		info.position = actuator.getPosition();
		info.isParked = isParked;
		return info;
	}

	/**
	 * Description: Records the 1 m spot that the rear sensor has just driven across.
	 * Pre-condition: position 0 - STREET_LENGTH
	 * Post-condition: If the spot was UNKNOWN it becomes FREE (isEmpty at or above 100 cm)
	 * or BLOCKED. A spot that has already been detected is never overwritten.
	 * Test-cases: MoveForwardRecordsFreeAt100, MoveForwardRecordsBlockedAt99,
	 * MoveBackwardsKeepsAlreadyRecordedSpot
	 */
	private void recordSpot(int index) {
		if (parkingmap.getSpotStatus(index) == SpotStatus.UNKNOWN) // MoveForwardKeepsAlreadyRecordedSpot: a measured meter is never overwritten
			parkingmap.setSpotStatus(index, isEmpty() >= 100 ? SpotStatus.FREE : SpotStatus.BLOCKED); // MoveForwardRecordsFreeAt100 and MoveForwardRecordsBlockedAt99: 100 cm or more is FREE, less is BLOCKED
	}
	/**
	 * Description: Moves the car 1 m forward, reads the sensors through
	 * isEmpty and records the spot just drove across.
	 * Returns the current position and the parking map so far.
	 * Pre-condition: none
	 * Post-condition: If position was below 495 and the car is not parked: position is
	 * increased by 1, and the spot at the old position is recorded as FREE or BLOCKED, unless it was already recorded.
	 * If position was 495 or the car was parked: nothing changes.
	 * Returns a ParkingRecord object that contains the current position and a copy
	 * of the detections so far.
	 * Test-cases: MoveForwardTestOneStepFromStart, MoveForwardMoreThanStreetLength,
	 * MoveForwardWhileParkedDoesNothing, MoveForwardRecordsFreeAt100,
	 * MoveForwardRecordsBlockedAt99, MoveForwardAccumulatesMap
	 * MoveForwardKeepsAlreadyRecordedSpot
	 */
	public ParkingRecord MoveForward() {
		if (!isParked) {
			int oldPos = actuator.getPosition();
			int newPos = actuator.moveForward();
			if (newPos != oldPos)       // the car actually moved
				recordSpot(oldPos);
		}
		return new ParkingRecord(parkingmap, whereIs());
	}

	/**
	 * Description: Moves the car 1 m backward, reads the sensors through
	 * isEmpty and records the spot the rear sensor just drove across.
	 * Returns the current position and the parking map so far.
	 * Pre-condition: none
	 * Post-condition: If the car is not parked and position is above 0: position is
	 * decreased by 1, and the spot at the new position is recorded unless it was
	 * already recorded.
	 * Returns a ParkingRecord with the current position and a copy of the map.
	 * Test-cases: MoveBackwardsFromStart, MoveBackwardsFromPositionFive,
	 * MoveBackwardsFromEnd, MoveBackwardsWhileParkedDoesNothing,
	 * MoveBackwardsKeepsAlreadyRecordedSpot
	 */
	public ParkingRecord MoveBackwards() {
		if (!isParked) {
			int oldPos = actuator.getPosition();
			int newPos = actuator.moveBackward();
			if (newPos != oldPos)       // the car actually moved
				recordSpot(newPos);     // backwards: record the meter the car is now over
		}
		return new ParkingRecord(parkingmap, whereIs());
	}
	
	private double calculateAvgError(int[] arr, float avg)
	{
		if (arr.length == 0)
			return 0;

		double error_final = 0; // isEmptyNoisySensorA: collects the differences
		for (int i = 0; i < arr.length; i++) // isEmptyNoisySensorA: check every reading
		{
			if (arr[i] < 0 || arr[i] > 200)
				return 100; // turn off if broken sensor.
			else
				error_final += Math.abs(((arr[i] / avg) - 1)); // isEmptyNoisySensorA: how far this reading is from the mean, as a part of the mean
		}
		return avg != 0 ? error_final / arr.length : 0;
	}

	/**
	 * Description: Reads each sensor 5 times, averages the readings and adds
	 * each sensor's relative error to its total error (if error > 0.075).
	 * A sensor with total error >= 1.0 is ignored permanently.
	 * Returns the distance in cm to the nearest object on the right.
	 * Pre-condition: none
	 * Post-condition: total errors are updated. Returns the smaller average
	 * if both sensors are trusted, the trusted sensor's average if only one is,
	 * and 0 if neither is.
	 * Test-cases: isEmptyTest, isEmptyNoisySensorA,
	 * isEmptyNoisySensorB, isEmptyBothSensorsNoisy, isEmptyNoisySensorARetiresOnFifthCall
	 */
	public int isEmpty() {
		int[] a_values = new int[SENSOR_SIM_AMOUNT], b_values = new int[SENSOR_SIM_AMOUNT]; // isEmptyNoisySensorA: the readings are needed to measure the noise
		int sum_a = 0, sum_b = 0; // isEmptyTest: sums are needed for the means

		for (int i = 0; i < SENSOR_SIM_AMOUNT; i++) { // isEmptyTest: each sensor is read 5 times, as the requirement says
			sum_a += a_values[i] = sensor_a.read(); // isEmptyTest: read sensor A and add to its sum
			sum_b += b_values[i] = sensor_b.read(); // isEmptyTest: read sensor B and add to its sum
		}

		float avg_a = (float) sum_a / SENSOR_SIM_AMOUNT; // isEmptyTest: the mean filters the noise of sensor A
		float avg_b = (float) sum_b / SENSOR_SIM_AMOUNT; // isEmptyTest: the mean filters the noise of sensor B

		double e_a = calculateAvgError(a_values, avg_a); // isEmptyNoisySensorA: measure how noisy this call was
		double e_b = calculateAvgError(b_values, avg_b); // isEmptyNoisySensorB: measure how noisy this call was

		if (e_a > 0.075) // isEmptyTest: a small error is normal and is not counted
			sensor_a_error += e_a; // isEmptyNoisySensorA: a noisy call is added to the total error
		if (e_b > 0.075) // isEmptyTest: a small error is normal and is not counted
			sensor_b_error += e_b; // isEmptyNoisySensorB: a noisy call is added to the total error

		boolean a_ok = sensor_a_error < 1.0 && avg_a >= 0 && avg_a <= 200; // isEmptyNoisySensorA: a sensor with total error 1.0 or more is ignored
		boolean b_ok = sensor_b_error < 1.0 && avg_b >= 0 && avg_b <= 200; // isEmptyNoisySensorB: a sensor with total error 1.0 or more is ignored

		if (a_ok && b_ok) return (int)Math.min(avg_a, avg_b); // isEmptyTest: return the distance to the nearest object
		if (a_ok) return (int)avg_a; // isEmptyNoisySensorB: only sensor A can be trusted
		if (b_ok) return (int)avg_b; // isEmptyNoisySensorA: only sensor B can be trusted
		return 0; // isEmptyBothSensorsNoisy: no sensor can be trusted, 0 cm means BLOCKED
	}

	private int getValidParkingPosition() {
		int counter = 0; // ParkTest: counts how many free meters in a row have been found

		for (int i = 0; i < STREET_LENGTH - CAR_LENGTH; i++) { // ParkNoFreeStretch: search the whole street, the last place starts at 490
			if (whereIs().position <= i) // ParkUsesFreeStretchBehindCar: meters behind the car are already measured
				MoveForward(); // ParkTest: drive forward to measure new meters

			if (parkingmap.getSpotStatus(i) == SpotStatus.FREE) // ParkTest: a free meter is part of a parking place
				counter++; // ParkTest: count this free meter
			else
				counter = 0; // ParkFourFreeMetersIsNotEnough: a blocked meter resets the count

			if (counter == CAR_LENGTH) // ParkTest: 5 free meters in a row is a parking place
				return i - CAR_LENGTH + 1; // ParkAtTheEndOfTheStreet: return the first meter of the place
		}

		return -1; // ParkNoFreeStretch: no parking place was found on the street
	}

	/**
	 * Description: Finds a 5 m free stretch and parks in it. First checks the
	 * already-detected spots from the start of the street, then moves forward until
	 * a stretch is detected. The car then reverses into the stretch and is marked parked.
	 * Pre-condition: none
	 * Post-condition: If the car was already parked, nothing changes. If a stretch was
	 * found, position is the start of the stretch and isParked is true. If no stretch
	 * was found, the car is at 495 and isParked is false.
	 * Test-cases: ParkTest, ParkUsesFreeStretchBehindCar, ParkNoFreeStretch, ParkWhenAlreadyParked,
	 * ParkFourFreeMetersIsNotEnough, ParkAtTheEndOfTheStreet
	 */

	/** A place is a perfect fit if it is exactly CAR_LENGTH long and closed by a BLOCKED meter. */
	private boolean isPerfectFit(int start) {
		int after = start + CAR_LENGTH;
		return after < parkingmap.length()
				&& parkingmap.getSpotStatus(after) == SpotStatus.BLOCKED;
	}

	public void Park() {
		if (isParked) return;

		int pos = findSmallestFreeStretch();
		while (pos == -1 || !isPerfectFit(pos)) {
			int before = actuator.getPosition();
			MoveForward();
			if (actuator.getPosition() == before)
				break;
			pos = findSmallestFreeStretch();
		}

		if (pos == -1) return;

		while (actuator.getPosition() > pos)
			MoveBackwards();

		isParked = true;
	}

	private int findSmallestFreeStretch() {
		int bestStart = -1;
		int bestLength = Integer.MAX_VALUE;
		int runStart = 0;
		int runLength = 0;

		for (int i = 0; i <= parkingmap.length(); i++) {
			boolean free = i < parkingmap.length()
					&& parkingmap.getSpotStatus(i) == SpotStatus.FREE;

			if (free) {
				if (runLength == 0) runStart = i;
				runLength++;
			} else {
				if (runLength >= CAR_LENGTH && runLength < bestLength) {
					bestLength = runLength;
					bestStart = runStart;
				}
				runLength = 0;
			}
		}
		return bestStart;
	}

	/**
	 * Description: Moves the car forward out of its parking spot.
	 * Pre-condition: none
	 * Post-condition: If the car was parked: isParked is false and the car has moved
	 * 5 m forward. If the car was not parked, nothing changes.
	 * Test-cases: UnParkMovesOut, UnParkWhenNotParked
	 */
	public void UnPark()
	{
		if (!isParked) return; // UnParkWhenNotParked: nothing happens if the car is not parked

		isParked = false; // UnParkMovesOut: the car is no longer parked, this must be set first so that MoveForward works
		for (int i = 0; i < CAR_LENGTH; i++) MoveForward(); // UnParkMovesOut: the car moves 5 m forward, out of the parking place
	}
}
