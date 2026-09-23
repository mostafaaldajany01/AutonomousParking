package automaticparking;

public class CarActuator implements Actuator{

    private final int STREET_LENGTH = 500;
    private final int CAR_LENGTH = 5;

    private int position;

    public CarActuator() {
        position = 0;
    }
    /**
     * Description: Moves the car 1 m forward.
     * Pre-condition: none
     * Post-condition: If position was below 495, position is increased by 1.
     * If position was 495, nothing changes. Returns the position after the call.
     * Test-cases: ActuatorMoveForwardOneFromZero, ActuatorMoveForwardMoreThanStreetLength,
     */

    @Override
    public int moveForward() {

        if (position >= STREET_LENGTH - CAR_LENGTH)
            return position;
        return ++position;
    }
    /**
     * Description: Moves the car 1 m backward.
     * Pre-condition: none
     * Post-condition: If position was above 0, position is decreased by 1.
     * If position was 0, nothing changes. Returns the position after the call.
     * Test-cases: ActuatorMoveBackFromStart, Actuator5StepForward1StepBack,
     */
    @Override
    public int moveBackward() {
        if (position <= 0)
            return position;
        return --position;
    }
    /**
     * Description: Returns the current position
     * Pre-condition: none
     * Post-condition: none
     */
    @Override
    public int getPosition() {
        return position;
    }
}
