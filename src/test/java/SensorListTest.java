import com.google.common.collect.Lists;
import eventprocessing.models.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SensorListTest {

    private Reading reading;
    private SensorList sensorList;
    private Sensor sensor2;
    private Sensor sensor1;

    @Before
    public void setup() {
        sensor1 = Mockito.mock(Sensor.class);
        sensor2 = Mockito.mock(Sensor.class);
        when(sensor1.getLocationId()).thenReturn("mockSensorId1");
        when(sensor2.getLocationId()).thenReturn("mockSensorId2");
        sensorList = new SensorList(Lists.newArrayList(sensor1, sensor2));
        reading = Mockito.mock(Reading.class);
    }

    @Test
    public void isWorkingSensor_returnsTrue_whenSensorInSensorList() {
        when(reading.getLocationId()).thenReturn("mockSensorId2");
        assertTrue(sensorList.isWorkingSensor(reading));
    }

    @Test
    public void isNotWorkingSensor_returnsFalse_whenSensorNotInSensorList() {
        when(reading.getLocationId()).thenReturn("invalidId");
        assertFalse(sensorList.isWorkingSensor(reading));
    }

    @Test
    public void storeSensorData_callsAddReadingOnAppropriateSensorWithPassedReading() {
        reading = buildReadingWithLocationAndValue("mockSensorId2", 1.3);
        sensorList.storeSensorData(reading);
        verify(sensor2, times(1)).addReading(reading);

    }

    private Reading buildReadingWithLocationAndValue(String locationId, double value) {
        Reading reading = Mockito.mock(Reading.class);
        when(reading.getLocationId()).thenReturn(locationId);
        when(reading.getValue()).thenReturn(value);
        return reading;
    }

}
