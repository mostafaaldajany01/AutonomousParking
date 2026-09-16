package automaticparking;
import java.util.Random;

public class SimulatedSensor implements Sensor{
    private final Random rng;

    @Override
    public int read() {
    	int ran = rng.nextInt(201);
    	int sw = rng.nextInt(10);

    	if (sw == 9)
    		ran += rng.nextInt(40);
    	else if (sw == 8)
    		ran -= rng.nextInt(40);
    	else if (sw == 6 || sw == 7)
    		ran += rng.nextInt(10);
    	else if (sw == 4 || sw == 5)
    		ran -= rng.nextInt(10);
    	else if (sw == 2 || sw == 3)
    		ran += rng.nextInt(5);
    	else
    		ran -= rng.nextInt(5);

    	if (ran < 0) ran = 0;
    	if (ran > 200) ran = 200;

    	return ran;
    }

    public SimulatedSensor() {
        this.rng = new Random();
    }
}
