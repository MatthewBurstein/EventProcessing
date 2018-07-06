import com.google.common.collect.Lists;
import eventprocessing.models.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SensorListTest {

    private Reading reading;
    private SensorList sensorList;

    @Before
    public void setup() {
        Sensor sensor1 = Mockito.mock(Sensor.class);
        Sensor sensor2 = Mockito.mock(Sensor.class);
        when(sensor1.getId()).thenReturn("mockSensorId1");
        when(sensor2.getId()).thenReturn("mockSensorId2");
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

}
