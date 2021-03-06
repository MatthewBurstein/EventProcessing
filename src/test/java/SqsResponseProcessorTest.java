import com.google.common.collect.Lists;
import eventprocessing.models.*;
import eventprocessing.responseservices.ResponseProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SqsResponseProcessorTest {

    private Sensor mockSensor;
    private SensorList mockSensorList;
    private Message mockMessage;
    private SqsResponse mockSqsResponse;
    private Bucket mockBucket;
    private ResponseProcessor responseProcessor;

    @Before
    public void setup() {
        mockSensor = Mockito.mock(Sensor.class);
        when(mockSensor.getId()).thenReturn("validId");
        mockSensorList = Mockito.mock(SensorList.class);
        when(mockSensorList.getSensors()).thenReturn(Lists.newArrayList(mockSensor));
        mockMessage = Mockito.mock(Message.class);
        mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessage()).thenReturn(mockMessage);

        mockBucket = Mockito.mock(Bucket.class);
        when(mockBucket.getMessageIds()).thenReturn(Lists.newArrayList("messageId1", "messageId2"));

        responseProcessor = new ResponseProcessor();
    }

    @Test
    public void isWorkingSensor_returnsTrue() {
        when(mockMessage.getLocationId()).thenReturn("validId");
        assertTrue(responseProcessor.isWorkingSensor(mockSqsResponse, mockSensorList));
    }

    @Test
    public void isNotWorkingSensor_returnsFalse() {
        when(mockMessage.getLocationId()).thenReturn("invalidId");
        assertFalse(responseProcessor.isWorkingSensor(mockSqsResponse, mockSensorList));
    }

    @Test
    public void isDuplicateMessage_whenDuplicate_returnsTrue() {
        when(mockSqsResponse.getMessageId()).thenReturn("messageId1");
        assertTrue(responseProcessor.isDuplicateMessage(mockSqsResponse, mockBucket));
    }

    @Test
    public void isDuplicateMessage_whenNotDuplicate_returnsFalse() {
        when(mockSqsResponse.getMessageId()).thenReturn("newMessageId");
        assertFalse(responseProcessor.isDuplicateMessage(mockSqsResponse, mockBucket));
    }
}
