import eventprocessing.GlobalConstants;
import eventprocessing.models.SqsResponse;
import eventprocessing.models.Bucket;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.threeten.extra.MutableClock;
import sun.security.action.GetLongAction;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BucketTest {

    private Bucket bucket;
    private long bucketStart;
    private MutableClock clock;
    private GlobalConstants gc;

    @Before
    public void createResponseList() {
        gc = new GlobalConstants(60, 5);
        bucketStart = 100000;
        bucket = new Bucket(bucketStart, gc);
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsBeforeRange() {
        long timeBeforeBucketStart = bucketStart - 1;
        clock = buildClock(timeBeforeBucketStart);
        assertFalse(bucket.isExpiredAtTime(clock));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsInRange() {
        long lastMilliOfBucket = bucketStart + gc.THIS_BUCKET_RANGE;
        clock = buildClock(lastMilliOfBucket);
        assertFalse(bucket.isExpiredAtTime(clock));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsWithinDelayTime() {
        long timeOutsideRangeInDelayTime = bucketStart + gc.MAX_MESSAGE_DELAY_MILLIS;
        clock = buildClock(timeOutsideRangeInDelayTime);
        assertFalse(bucket.isExpiredAtTime(clock));
    }

    @Test
    public void isExpiredAtTime_returnsTrueWhenPassedArgumentIsAfterMaxDelay() {
        long timeAfterBucketExpires = bucketStart + gc.THIS_BUCKET_RANGE + gc.MAX_MESSAGE_DELAY_MILLIS;
        clock = buildClock(timeAfterBucketExpires);
        assertTrue(bucket.isExpiredAtTime(clock));
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

        double expectedAverageValue = 2.0;
        assertEquals(expectedAverageValue, bucket.getAverageValue(), 0);
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

    private MutableClock buildClock(long millis) {
        Instant fixedInstant = Instant.ofEpochMilli(millis);
        return MutableClock.of(fixedInstant, ZoneId.systemDefault());
    }

}
