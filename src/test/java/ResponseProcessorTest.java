import com.google.common.collect.Lists;
import eventprocessing.models.Message;
import eventprocessing.models.Response;
import eventprocessing.models.Sensor;
import eventprocessing.models.SensorList;
import eventprocessing.responseservices.ResponseProcessor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ResponseProcessorTest {

    //SensorList.getSensors -> List<Sensor>
    //Sensor.getId() -> return validId
    //response.getMessage() -> message
    //message.getLocationId() -> validLocation, invalidLocation

    private Sensor mockSensor;
    private SensorList mockSensorList;
    private Message mockMessage;
    private Response mockResponse;
    private ResponseProcessor responseProcessor;

    @Before
    public void setup() {
        mockSensor = Mockito.mock(Sensor.class);
        when(mockSensor.getId()).thenReturn("validId");
        mockSensorList = Mockito.mock(SensorList.class);
        when(mockSensorList.getSensors()).thenReturn(Lists.newArrayList(mockSensor));
        mockMessage = Mockito.mock(Message.class);
        mockResponse = Mockito.mock(Response.class);
        when(mockResponse.getMessage()).thenReturn(mockMessage);

        responseProcessor = new ResponseProcessor();
    }


    @Test
    public void isWorkingSensor_returnsTrue() {
        when(mockMessage.getLocationId()).thenReturn("validId");
        assertTrue(responseProcessor.isWorkingSensor(mockResponse, mockSensorList));
    }

    @Test
    public void isNotWorkingSensor_returnsFalse() {
        when(mockMessage.getLocationId()).thenReturn("invalidId");
        assertFalse(responseProcessor.isWorkingSensor(mockResponse, mockSensorList));
    }
}
