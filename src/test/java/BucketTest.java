import eventprocessing.GlobalConstants;
import eventprocessing.models.Reading;
import eventprocessing.models.Bucket;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.threeten.extra.MutableClock;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
    public void getReadingIds_returnsListOfReadingIds() {
        List<Reading> readings = buildMultipleReadingsWithIds("reading1Id", "reading2Id");
        bucket.getReadings().addAll(readings);
        List<String> expected = Lists.newArrayList("reading1Id", "reading2Id");
        assertEquals(bucket.getReadingIds(), expected);
    }

    @Test
    public void getAverageValue_returnsAverageOfReadingValues() {
        List<Reading> readings = buildMultipleReadingsWithValues(1.0, 2.0, 3.0);
        bucket.getReadings().addAll(readings);
        double expectedAverageValue = 2.0;
        assertEquals(expectedAverageValue, bucket.getAverageValue(), 0);
    }

    @Test
    public void isDuplicateReading_whenDuplicate_returnsTrue() {
        Reading reading = buildReadingWithId("readingId");
        bucket.addResponse(reading);
        assertTrue(bucket.isDuplicateReading(reading));
    }

    @Test
    public void isDuplicateReading_whenNotDuplicate_returnsFalse() {
        Reading reading = buildReadingWithId("readingId");
        assertFalse(bucket.isDuplicateReading(reading));
    }

    private Reading buildReadingWithId(String readingId) {
        Reading reading = Mockito.mock(Reading.class);
        when(reading.getId()).thenReturn(readingId);
        return reading;
    }

    private List<Reading> buildMultipleReadingsWithIds(String ... readingIds) {
        List<Reading> readings = new ArrayList<>();
        for(String readingId : readingIds) {
            readings.add(buildReadingWithId(readingId));
        }
        return readings;
    }

    private Reading buildReadingWithValue(double value) {
        Reading reading = Mockito.mock(Reading.class);
        when(reading.getValue()).thenReturn(value);
        return reading;
    }

    private List<Reading> buildMultipleReadingsWithValues(double ... values) {
        List<Reading> readings = new ArrayList<>();
        for(double value : values) {
            readings.add(buildReadingWithValue(value));
        }
        return readings;
    }


    private MutableClock buildClock(long millis) {
        Instant fixedInstant = Instant.ofEpochMilli(millis);
        return MutableClock.of(fixedInstant, ZoneId.systemDefault());
    }

}
