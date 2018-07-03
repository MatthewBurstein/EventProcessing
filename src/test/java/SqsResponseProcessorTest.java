import com.google.common.collect.Lists;
import eventprocessing.models.*;
import eventprocessing.responseservices.ResponseProcessor;
import org.junit.Before;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class SqsResponseProcessorTest {

    private Message mockMessage;
    private SqsResponse mockSqsResponse;
    private Bucket mockBucket;
    private ResponseProcessor responseProcessor;

    @Before
    public void setup() {
        mockMessage = Mockito.mock(Message.class);
        mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessage()).thenReturn(mockMessage);

        mockBucket = Mockito.mock(Bucket.class);
        when(mockBucket.getMessageIds()).thenReturn(Lists.newArrayList("messageId1", "messageId2"));

        responseProcessor = new ResponseProcessor();
    }
}
