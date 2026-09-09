package automaticparking;

public class CarStatus {
	private int position;
	private boolean isParked;

	public boolean getParkStatus() { return isParked; }
	public void setParkStatus(boolean status) {isParked = status; }
	
	public void moveForward() { position++; }
	public void moveBackwards() { position--; }
		
	public int getPosition() { return position; }
	
	public CarStatus() {
		position = 0;
		isParked = false;
	}
}
