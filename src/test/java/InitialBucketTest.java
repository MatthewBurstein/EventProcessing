import com.google.common.collect.Lists;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class InitialBucketTest {

    private InitialBucket initialBucket;

    @Before
    public void createObjects() {
        initialBucket = new InitialBucket();
        Response mockResponse1 = Mockito.mock(Response.class);
        Response mockResponse2 = Mockito.mock(Response.class);
        Response mockResponse3 = Mockito.mock(Response.class);
        when(mockResponse1.getMessageTimestamp()).thenReturn((long) 9999);
        when(mockResponse2.getMessageTimestamp()).thenReturn((long) 333);
        when(mockResponse3.getMessageTimestamp()).thenReturn((long) 22);
        initialBucket.getResponses().addAll(Lists.newArrayList(mockResponse1, mockResponse2, mockResponse3));
    }

    @Test
    public void getEarliestTimestamp_returnsResponseWithEarliestTimestamp() {
        assertEquals(initialBucket.getEarliestTimestamp(), 22);
    }
}
