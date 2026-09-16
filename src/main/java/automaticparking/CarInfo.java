package automaticparking;

public class CarInfo {
	public int position;
	public boolean isParked;

	public CarInfo() {
		position = 0;
		isParked = false;
	}

	// For copy
	public CarInfo(CarInfo other) {
		position = other.position;
		isParked = other.isParked;
	}
}
