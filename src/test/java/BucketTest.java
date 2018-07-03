import eventprocessing.GlobalConstants;
import eventprocessing.models.SqsResponse;
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
    private long bucketStart;

    @Before
    public void createResponseList() {
        bucketStart = 100000;
        bucket = new Bucket(bucketStart);
    }

    @Test
    public void isExpiredAtTime_returnsTrueWhenPassedArgumentIsBeforeRange() {
        long timeStampBeforeBucket = bucketStart - 1000;
        assertFalse(bucket.isExpiredAtTime(timeStampBeforeBucket));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsInRange() {
        long timeStampDuringBucket = bucketStart + GlobalConstants.THIS_BUCKET_RANGE;
        assertFalse(bucket.isExpiredAtTime(timeStampDuringBucket));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsWithinDelayTime() {
        long timeStampAfterBucketWithinDelayPeriod = bucketStart + GlobalConstants.THIS_BUCKET_RANGE * GlobalConstants.MAX_MESSAGE_DELAY_MINS;
        assertFalse(bucket.isExpiredAtTime(timeStampAfterBucketWithinDelayPeriod));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsAfterFiveMins() {
        long timeStampAfterDelayPeriod = bucketStart + GlobalConstants.THIS_BUCKET_RANGE + GlobalConstants.BUCKET_UPPER_BOUND * GlobalConstants.MAX_MESSAGE_DELAY_MINS;
        assertTrue(bucket.isExpiredAtTime(timeStampAfterDelayPeriod));
    }
    
    @Test
    public void getMessageIds_returnsListOfMessageIds() {
        SqsResponse mockSqsResponse1 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse1.getMessageId()).thenReturn("mockResponse1id");
        SqsResponse mockSqsResponse2 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse2.getMessageId()).thenReturn("mockResponse2id");
        bucket.getSqsResponses().addAll(Lists.newArrayList(mockSqsResponse1, mockSqsResponse2));
        List<String> expected = Lists.newArrayList("mockResponse1id", "mockResponse2id");
        assertEquals(bucket.getMessageIds(), expected);
    }

    @Test
    public void getAverageValue_returnsAverageOfMessageValues() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        List<SqsResponse> mockSqsResponses = Lists.newArrayList(mockSqsResponse, mockSqsResponse, mockSqsResponse);
        bucket.getSqsResponses().addAll(mockSqsResponses);
        when(mockSqsResponse.getValue()).thenReturn(1.0, 2.0, 3.0);

        double expectedValue = 2.0;
        assertEquals(expectedValue, bucket.getAverageValue(), 0);
    }

    @Test
    public void isDuplicateMessage_whenDuplicate_returnsTrue() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessageId()).thenReturn("messageId1");
        bucket.addResponse(mockSqsResponse);
        assertTrue(bucket.isDuplicateMessage(mockSqsResponse));
    }

    @Test
    public void isDuplicateMessage_whenNotDuplicate_returnsFalse() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessageId()).thenReturn("newMessageId");
        assertFalse(bucket.isDuplicateMessage(mockSqsResponse));
    }

}
