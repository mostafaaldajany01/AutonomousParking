package automaticparking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarActuatorTest {

    @Test
    public void ActuatorZeroTest() {
        Actuator act = new CarActuator();
        assertEquals(0, act.getPosition(), "Should be at zero before starting.");
    }

    @Test
    public void ActuatorMoveForwardOneFromZero() {
        Actuator act = new CarActuator();
        act.moveForward();
        assertEquals(1, act.getPosition(), "Should be at position 1");
    }

    @Test
    public void ActuatorMoveForwardMoreThanStreetLength() {
        Actuator act = new CarActuator();

        for (int i = 0; i < 501; i++) act.moveForward();
        assertEquals(495, act.getPosition(), "Should be at 495.");
    }


    @Test
    public void ActuatorMoveBackFromStart() {
        Actuator act = new CarActuator();
        act.moveBackward();
        assertEquals(0, act.getPosition(), "Should be at 0");
    }


    @Test
    public void Actuator5StepForward1StepBack() {
        Actuator act = new CarActuator();
        for (int i = 0; i < 5; i++) act.moveForward();
        act.moveBackward();
        assertEquals(4, act.getPosition(), "Should be at pos 4");
    }

    @Test
    public void ActuatorMoveForwardReturnsNewPosition() {
        Actuator act = new CarActuator();
        assertEquals(1, act.moveForward(), "Should return 1 after one step from 0");
    }

    @Test
    public void ActuatorMoveBackwardReturnsNewPosition() {
        Actuator act = new CarActuator();
        for (int i = 0; i < 5; i++) act.moveForward();
        assertEquals(4, act.moveBackward(), "Should return 4 after one step back from 5");
    }
}