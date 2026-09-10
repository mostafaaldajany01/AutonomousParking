package automaticparking;

public class ParkingRecord {
	public ParkingMap parkingmap;
	public int position;
	
	public ParkingRecord(ParkingMap pm, CarInfo cs, int street_length)
	{
		parkingmap = new ParkingMap(street_length);
		for (int i = 0; i < street_length; i++)
			parkingmap.setSpotStatus(i, pm.getSpotStatus(i));
		position = cs.position;
	}
	
}
