package automaticparking;
import java.util.Random;

public class SimulatedSensor implements Sensor{

    private final Random rng;
    
    @Override
    public int read() {
    	return rng.nextInt(201);
    }
    
    public SimulatedSensor() {
        this.rng = new Random();
    }
}
