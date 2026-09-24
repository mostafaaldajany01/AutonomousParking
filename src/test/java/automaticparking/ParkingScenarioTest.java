package automaticparking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import automaticparking.ParkingMap.SpotStatus;

class ParkingScenarioTest {

    private Actuator actuator;
    private StreetSensor sensorA;
    private StreetSensor sensorB;

    @BeforeEach
    void setUp() {
        actuator = spy(new CarActuator());
    }

    /** Creates a car on a street with the given places {start, length}. Sensor A always breaks at the middle. */
    private AutomaticParking createCar(int[][] places, boolean sensorBBreaks) {
        Street street = new Street(places);
        sensorA = new StreetSensor(street, actuator, true);
        sensorB = new StreetSensor(street, actuator, sensorBBreaks);
        return new AutomaticParking(sensorA, sensorB, actuator);
    }

    @Test
    void scenario1_ParkInSmallestPlaceThenDriveToEnd() {
        AutomaticParking ap = createCar(new int[][] { {50, 4}, {150, 10}, {350, 7} }, false);

        ap.Park();
        assertEquals(350, ap.whereIs().position, "The 7 m place is the smallest that fits");
        assertTrue(ap.whereIs().isParked);
        verify(actuator, times(145)).moveBackward();   // scanned to 495, reversed to 350
        assertTrue(sensorA.isBroken());
        assertEquals(SpotStatus.FREE, ap.MoveForward().parkingmap.getSpotStatus(356), "Detected with sensor B only");

        ap.UnPark();
        assertEquals(355, ap.whereIs().position);
        assertFalse(ap.whereIs().isParked);

        while (ap.whereIs().position < 495) ap.MoveForward();
        assertEquals(495, ap.whereIs().position);
    }

    @Test
    void scenario2_StopScanningAtPerfectFit() {
        AutomaticParking ap = createCar(new int[][] { {100, 4}, {200, 8}, {300, 5} }, false);

        ap.Park();
        assertEquals(300, ap.whereIs().position, "The 5 m place is a perfect fit");
        assertTrue(ap.whereIs().isParked);
        verify(actuator, times(6)).moveBackward();     // stopped at 306, reversed to 300
        assertEquals(SpotStatus.UNKNOWN, ap.MoveForward().parkingmap.getSpotStatus(400), "Rest of street not scanned");
    }

    @Test
    void scenario3_BothSensorsBreakNoPlaceFound() {
        AutomaticParking ap = createCar(new int[][] { {300, 4}, {350, 7}, {400, 10} }, true);

        ap.Park();
        assertTrue(sensorA.isBroken() && sensorB.isBroken());
        assertEquals(495, ap.whereIs().position, "No place found, searched to the end");
        assertFalse(ap.whereIs().isParked);

        while (ap.whereIs().position > 0) ap.MoveBackwards();
        assertEquals(0, ap.MoveBackwards().position, "Cannot go below 0");
    }

    @Test
    void scenario4_ParkInKnownPlaceAheadOfCar() {
        AutomaticParking ap = createCar(new int[][] { {50, 4}, {100, 5}, {150, 10} }, false);

        for (int i = 0; i < 160; i++) ap.MoveForward();
        while (ap.whereIs().position > 0) ap.MoveBackwards();

        ap.Park();
        assertEquals(100, ap.whereIs().position, "The perfect fit is ahead, so the car drives forward");
        assertTrue(ap.whereIs().isParked);
        verify(actuator, times(160)).moveBackward();   // only the manual reversing, none by Park()
    }
}