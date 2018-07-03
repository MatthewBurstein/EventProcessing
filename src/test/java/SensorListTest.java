import com.google.common.collect.Lists;
import eventprocessing.models.*;
import eventprocessing.responseservices.ResponseProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SensorListTest {

    private Sensor mockSensor1;
    private Sensor mockSensor2;
    private Message mockMessage;
    private SqsResponse mockSqsResponse;
    private SensorList sensorList;

    @Before
    public void setup() {
        mockSensor1 = Mockito.mock(Sensor.class);
        mockSensor2 = Mockito.mock(Sensor.class);
        when(mockSensor1.getId()).thenReturn("mockSensorId1");
        when(mockSensor2.getId()).thenReturn("mockSensorId2");
        sensorList = new SensorList(Lists.newArrayList(mockSensor1, mockSensor2));
        mockMessage = Mockito.mock(Message.class);
        mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessage()).thenReturn(mockMessage);
    }

    @Test
    public void isWorkingSensor_returnsTrue() {
        when(mockMessage.getLocationId()).thenReturn("mockSensorId2");
        assertTrue(sensorList.isWorkingSensor(mockSqsResponse));
    }

    @Test
    public void isNotWorkingSensor_returnsFalse() {
        when(mockMessage.getLocationId()).thenReturn("invalidId");
        assertFalse(sensorList.isWorkingSensor(mockSqsResponse));
    }

}
