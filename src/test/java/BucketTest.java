import eventprocessing.models.Response;
import eventprocessing.models.Bucket;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BucketTest {

    private Bucket bucket;

    @Before
    public void createResponseList() {
        bucket = new Bucket(100);
    }

    @Test
    public void isExpiredAtTime_returnsTrueWhenPassedArgumentIsBeforeRange() {
        assertFalse(bucket.isExpiredAtTime(90));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsInRange() {
        assertFalse(bucket.isExpiredAtTime(101));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsWithinFiveMins() {
        assertFalse(bucket.isExpiredAtTime(200));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsAfterFiveMins() {
        assertTrue(bucket.isExpiredAtTime(461));
    }
    
    @Test
    public void getMessageIds_returnsListOfMessageIds() {
        Response mockResponse1 = Mockito.mock(Response.class);
        when(mockResponse1.getMessageId()).thenReturn("mockResponse1id");
        Response mockResponse2 = Mockito.mock(Response.class);
        when(mockResponse2.getMessageId()).thenReturn("mockResponse2id");
        bucket.getResponses().addAll(Lists.newArrayList(mockResponse1, mockResponse2));
        List<String> expected = Lists.newArrayList("mockResponse1id", "mockResponse2id");
        assertEquals(bucket.getMessageIds(), expected);
    }
    
}
