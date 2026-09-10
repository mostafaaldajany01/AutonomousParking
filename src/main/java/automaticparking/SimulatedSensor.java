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
    	else if (sw >= 14 && sw <= 17)
    		ran += rng.nextInt(10);
    	else if (sw >= 9 && sw <= 13)
    		ran -= rng.nextInt(10);
    	else if (sw >= 5 && sw <= 8)
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
