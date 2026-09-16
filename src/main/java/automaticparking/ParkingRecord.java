package automaticparking;

public class ParkingRecord {
	public ParkingMap parkingmap;
	public int position;

	public ParkingRecord(ParkingMap pm, CarInfo cs)
	{
		parkingmap = new ParkingMap(pm);
		position = cs.position;
	}
}
