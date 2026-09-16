package automaticparking;

public class ParkingMap {
	public enum SpotStatus { UNKNOWN, BLOCKED, FREE }
	private SpotStatus[] parkingstatus;

	public SpotStatus getSpotStatus(int position) throws IndexOutOfBoundsException
	{
		return parkingstatus[position];
	}

	public void setSpotStatus(int position, SpotStatus st) throws IndexOutOfBoundsException
	{
		parkingstatus[position] = st;
	}

	public int length()
	{
		return parkingstatus.length;
	}

	public ParkingMap(int street_length)
	{
		parkingstatus = new SpotStatus[street_length];
		for (int i = 0; i < street_length; i++)
			parkingstatus[i] = SpotStatus.UNKNOWN;
	}

	// For copy
	public ParkingMap(ParkingMap other)
	{
		parkingstatus = new SpotStatus[other.length()];
		for (int i = 0; i < parkingstatus.length; i++)
			parkingstatus[i] = other.getSpotStatus(i);
	}
}
