package automaticparking;

public class ParkingRecord {
	public ParkingMap parkingmap;
	public int position;
	
	public ParkingRecord(ParkingMap pm, CarStatus cs)
	{
		parkingmap = new ParkingMap();
		for (int i = 0; i < 500; i++)
			parkingmap.setSpotStatus(i, pm.getSpotStatus(i));
		position = cs.getPosition();
	}
	
}
