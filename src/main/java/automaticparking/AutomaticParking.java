package automaticparking;

import automaticparking.ParkingMap.SpotStatus;

public class AutomaticParking {
	private Sensor sensor_a;
	private Sensor sensor_b;

	private double sensor_a_error = 0.0;
	private double sensor_b_error = 0.0;
	
	private CarInfo carinfo;
	private ParkingMap parkingmap;

	private static int STREET_LENGTH = 500;
	private static int CAR_LENGTH = 5;

	private static int SENSOR_SIM_AMOUNT = 5;

	public AutomaticParking(Sensor A, Sensor B) {
		this.sensor_a = A;
		this.sensor_b = B;

		carinfo = new CarInfo();
		parkingmap = new ParkingMap(STREET_LENGTH);
	}

	/**
	 * Description: Returns carinfo object containing the car's current position and whether it is parked.
	 * Pre-condition: none
	 * Post-condition: State is unchanged. Returns a CarInfo with the current
	 * position (0-495) and parked status.
	 * Test-cases: WhereIsTestStart, WhereIsTestAfterMoveThreeSteps, 
	 * WhereIsTestAfterMove1000Steps, WhereIsAfterPark
	 */

	public CarInfo whereIs() {
		return carinfo;
	}


	private void recordSpot(int index) {
	    if (parkingmap.getSpotStatus(index) == SpotStatus.UNKNOWN)
	        parkingmap.setSpotStatus(index, isEmpty() >= 50 ? SpotStatus.FREE : SpotStatus.BLOCKED);
	}
	
	/**
	 * Description: Moves the car 1 m forward, reads the sensors through
	 * isEmpty and records the spot the rear sensor just drove across.
	 * Returns the current position and the parking map so far.
	 * Pre-condition: none
	 * Post-condition: If position was below 495: position is increased by 1,
	 * and the spot at the old position is recorded as FREE (isEmpty >= 50)
	 * or BLOCKED, unless it was already recorded.
	 * If position was 495 or car was parked: nothing changes.
	 * Returns a ParkingRecord object that contains
	 * the current position and a copy of the previous detections.
	 * Test-cases: MoveForwardTestOneStepFromStart, MoveForwardMoreThanStreetLength, 
	 * MoveForwardWhileParkedDoesNothing, MoveForwardRecordsFreeAt50, MoveForwardRecordsBlockedAt49
	*/
	public ParkingRecord MoveForward() {
	    int position = carinfo.position;
	    boolean moved = position < STREET_LENGTH - CAR_LENGTH;

	    if (moved && !carinfo.isParked) {
	    	carinfo.position++;
	        recordSpot(position);
	    }

	    return new ParkingRecord(parkingmap, carinfo, STREET_LENGTH);
	}

	/**
	 * Description: Moves the car 1 m backward, reads the sensors through
	 * isEmpty and records the spot the rear sensor just drove across.
	 * Returns the current position and the parking map so far.
	 * Pre-condition: none
	 * Post-condition: If the car is not parked and position is above 0:
	 * position is decreased by 1, and the spot at the new position is
	 * recorded as FREE or BLOCKED, unless already recorded.
	 * Otherwise nothing changes.
	 * Returns a ParkingRecord with the current position and a copy of the map.
	 * Test-cases: MoveBackwardsFromStart, MoveBackwardsFromPositionFive, 
	 * MoveBackwardsFromEnd, MoveBackwardsWhileParkedDoesNothing
	*/
	public ParkingRecord MoveBackwards() {
	    int position = carinfo.position;
	    boolean moved = position > 0;

	    if (moved && !carinfo.isParked) {
	    	carinfo.position--;
	        recordSpot(carinfo.position);
	    }

	    return new ParkingRecord(parkingmap, carinfo, STREET_LENGTH);
	}

	
	private double calculateAvgError(int[] arr, float avg)
	{
		double error_final = 0;
		for (int i = 0; i < arr.length; i++)
		{
			error_final += Math.abs(((arr[i] / avg) - 1));
		}
		return error_final / arr.length;
	}
	/**
	 * Description: Reads each sensor 5 times, averages the readings and adds
	 * each sensor's relative error to its total error (only if the
	 * avg error is above 0.1). A sensor whose accumulated error reaches
	 * 1.0 is ignored permanently. 
	 * Returns the distance in cm to the nearest object on the right.
	 * Pre-condition: none
	 * Post-condition: Accumulated errors are updated. Returns the smaller
	 * average if both sensors are trusted, the trusted sensor's average if
	 * only one is, and 0 if neither is.
	 * Test-cases: isEmptyTest, isEmptyNoisySensorA, 
	 * isEmptyNoisySensorB, isEmptyBothSensorsNoisy
	*/
	public int isEmpty() {
		int[] a_values = new int[SENSOR_SIM_AMOUNT], b_values = new int[SENSOR_SIM_AMOUNT];
	    int sum_a = 0, sum_b = 0;

	    for (int i = 0; i < SENSOR_SIM_AMOUNT; i++) {
	        sum_a += a_values[i] = sensor_a.read();
	        sum_b += b_values[i] = sensor_b.read();
	    }
	    
	    
	    float avg_a = (float) sum_a / SENSOR_SIM_AMOUNT;
	    float avg_b = (float) sum_b / SENSOR_SIM_AMOUNT;
	    
	    double e_a = calculateAvgError(a_values, avg_a);
	    double e_b = calculateAvgError(b_values, avg_b);
	    
	    if (e_a > 0.1)
		    sensor_a_error += e_a;
	    if (e_b > 0.1)
		    sensor_b_error += e_b;
	    
	    boolean a_ok = sensor_a_error < 1.0;
	    boolean b_ok = sensor_b_error < 1.0;

	    if (a_ok && b_ok) return (int)Math.min(avg_a, avg_b);
	    if (a_ok) return (int)avg_a;
	    if (b_ok) return (int)avg_b;
	    return 0;
	}


	public CarInfo getCarInfo() {
		return carinfo;
	}

	public ParkingMap getParkingMap() {
		return parkingmap;
	}

	public int getValidParkingPosition() {
	    int counter = 0;

	    for (int i = 0; i < STREET_LENGTH - CAR_LENGTH; i++) {
	        if (carinfo.position <= i)
	            MoveForward();

	        if (parkingmap.getSpotStatus(i) == SpotStatus.FREE)
	            counter++;
	        else
	            counter = 0;

	        if (counter == CAR_LENGTH)
	            return i - CAR_LENGTH + 1;
	    }

	    return -1;
	}
	/**
	 * Description: Finds a 5 m free stretch and parks in it. First checks
	 * the already-detected spots from the start of the street, then moves
	 * forward until a stretch is detected. The car stands 1 m past the
	 * stretch, reverses 5 m into it and is marked as parked.
	 * Pre-condition: none
	 * Post-condition: If the car was already parked, nothing changes.
	 * If a stretch was found, position is the start of the stretch and
	 * isParked is true. If no stretch was found, the car is at 495 and
	 * isParked is false.
	 * Test-cases: ParkTest, ParkUsesFreeStretchBehindCar, ParkNoFreeStretch, ParkWhenAlreadyParked
	*/
	public void Park() {
		if (carinfo.isParked) return;
		
	    int pos = getValidParkingPosition();
	    if (pos == -1) return;

	    while (carinfo.position > pos)
	        MoveBackwards();
	    while (carinfo.position < pos)
	        MoveForward();

	    carinfo.isParked = true;
	}
	
	/**
	 * Description: Moves the car forward out of its parking spot.
	 * Pre-condition: none
	 * Post-condition: If the car was parked: isParked is false and the car
	 * has moved 5 m forward. If the car was not parked, nothing changes.
	 * Test-cases: UnParkMovesOut, UnParkWhenNotParked
	*/
	
	public void UnPark()
	{
		if (!carinfo.isParked) return;
		
		carinfo.isParked = false;
		for (int i = 0; i < 5; i++) MoveForward();
		
		
	}
}
