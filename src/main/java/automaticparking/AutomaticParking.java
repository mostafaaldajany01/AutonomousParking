package automaticparking;

import automaticparking.ParkingMap.SpotStatus;

public class AutomaticParking {
	private Sensor A;
	private Sensor B;
	
	private CarStatus carstatus;
	private ParkingMap parkingmap;
	
	public AutomaticParking(Sensor A, Sensor B)
	{
		this.A = A;
		this.B = B;

		carstatus = new CarStatus();
		parkingmap = new ParkingMap();
	}

	
	/**
	 Description
	 Returns an object containing the cars current position and whether it's parked.
	 Pre-condition: none
	 Post-condition: returns a CarStatus where position is the car's current
	 Test-cases:
	 TC1 whereIsTestStart: Tests start, position 0, isParked false
	 TC2 whereIsTestAfterMoveThreeSteps: After 3x moveForward, position 3, isParked false
	 TC3 whereIsTestAfterMove1000Steps: After 1000x moveForward, position should still be 500
	*/
	
	public CarStatus whereIs() {
		return carstatus;
	}
	
	/**
	 Description: Increments the position up to the end of street (500) and returns an object 
	 containing the current position and previous parking conditions.
	 
	 Pre-condition: position is at 0-500
	 Post-condition: previous position is incremented by one, except at end of street (500)
	 Test-cases: 
	 TC1: MoveForward from start, should be at position 1.
	 TC2: MoveForward more than end of street, should be at position 500
	*/
	
	public ParkingRecord MoveForward() {
		if (carstatus.getPosition() < 500) carstatus.moveForward();
		
		return new ParkingRecord(parkingmap, carstatus);
			
	}
	
	/**
	 Description: position goes down by one but not further back than start of street, returns an object 
	 containing the current position and previous parking conditions.
	 
	 Pre-condition: position is at 0-500
	 Post-condition: previous position is decremented by one, up to the start of the street.
	 Test-cases: 
	 TC1: MoveBackwardsFromStart, should be at position 0.
	 TC2: MoveBackwardsFromPositionFive from any position, should be at position 4.
	*/
	
	public ParkingRecord MoveBackwards() {
		if (carstatus.getPosition() > 0) carstatus.moveBackwards();
		
		return new ParkingRecord(parkingmap, carstatus);
	}
	
	
	public int isEmpty() {
		int avgA = 0;
		int avgB = 0;
		
		for (int i = 0; i < 5; i++)
		{
			avgA += A.read();
			avgB += B.read();
		}
		
		return avgA >= avgB ? avgA/5 : avgB/5;
		
	}


	public CarStatus getCarStatus() {
		return carstatus;
	}
	
	public ParkingMap getParkingMap() {
		return parkingmap;
	}
	
}
