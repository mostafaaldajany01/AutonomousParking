package automaticparking;

import automaticparking.ParkingMap.SpotStatus;

public class ParkingMap {
	public enum SpotStatus { UNKNOWN, BLOCKED, FREE }
	private SpotStatus[] parkingStatus = new SpotStatus[500];
	
	public SpotStatus getSpotStatus(int position) throws IndexOutOfBoundsException
	{
		return parkingStatus[position];
	}
	
	public void setSpotStatus(int position, SpotStatus st) throws IndexOutOfBoundsException
	{
		parkingStatus[position] = st;
	}
	
	public ParkingMap()
	{
		for (int i = 0; i < 500; i++)
			parkingStatus[i] = SpotStatus.UNKNOWN;
	}
}
