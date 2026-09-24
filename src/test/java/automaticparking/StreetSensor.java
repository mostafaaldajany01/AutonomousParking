package automaticparking;

public class StreetSensor implements Sensor {
    private final Street street;
    private final Actuator actuator;
    private final boolean breaksAtMiddle;
    private boolean broken = false;

    public StreetSensor(Street street, Actuator actuator, boolean breaksAtMiddle) {
        this.street = street;
        this.actuator = actuator;
        this.breaksAtMiddle = breaksAtMiddle;
    }

    @Override
    public int read() {
        if (breaksAtMiddle && actuator.getPosition() >= Street.LENGTH / 2)
            broken = true;                 // breaks at the middle and stays broken
        if (broken)
            return -1;
        return street.distanceAt(actuator.getPosition() - 1);
    }

    public boolean isBroken() {
        return broken;
    }
}