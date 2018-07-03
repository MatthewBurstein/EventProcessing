import com.google.common.collect.Lists;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.SqsResponse;
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
        SqsResponse mockSqsResponse1 = Mockito.mock(SqsResponse.class);
        SqsResponse mockSqsResponse2 = Mockito.mock(SqsResponse.class);
        SqsResponse mockSqsResponse3 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse1.getMessageTimestamp()).thenReturn((long) 9999);
        when(mockSqsResponse2.getMessageTimestamp()).thenReturn((long) 333);
        when(mockSqsResponse3.getMessageTimestamp()).thenReturn((long) 22);
        initialBucket.getSqsResponses().addAll(Lists.newArrayList(mockSqsResponse1, mockSqsResponse2, mockSqsResponse3));
    }

    @Test
    public void getEarliestTimestamp_returnsResponseWithEarliestTimestamp() {
        assertEquals(initialBucket.getEarliestTimestamp(), 22);
    }
}
