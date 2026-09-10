package automaticparking;

import automaticparking.ParkingMap.SpotStatus;

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
	
	public ParkingMap(int street_length)
	{
		parkingstatus = new SpotStatus[street_length];
		for (int i = 0; i < street_length; i++)
			parkingstatus[i] = SpotStatus.UNKNOWN;
	}
}
