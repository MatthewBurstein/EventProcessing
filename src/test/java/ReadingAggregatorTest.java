import com.google.common.collect.Lists;
import eventprocessing.fileservices.CSVFileWriter;
import eventprocessing.models.Bucket;
import eventprocessing.models.ReadingAggregator;
import eventprocessing.models.SqsResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.threeten.extra.MutableClock;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReadingAggregatorTest {

    private ReadingAggregator readingAggregator;
    private CSVFileWriter fileWriter;

    @Before
    public void buildReadingAggregator() {
        fileWriter = Mockito.mock(CSVFileWriter.class);
    }

    @Test
    public void process_nothingWhileNoReadingsAreOlderThanDelayTime() {
        readingAggregator = new ReadingAggregator(fileWriter, Clock.systemUTC());
        SqsResponse reading = buildReading(generateTimestamp(Clock.systemUTC(), 0));
        readingAggregator.process(reading);
        verify(fileWriter, never()).write(any());
    }

    @Test
    public void process_singleReadingExceedingDelayTime_isSentToFileWriter() {
        MutableClock clock = buildClock(0);
        SqsResponse oldReading = buildReading(generateTimestamp(clock, -5));
        SqsResponse newReading = buildReading(0);
        readingAggregator = new ReadingAggregator(fileWriter, clock);

        readingAggregator.process(oldReading);
        advanceByMinutes(clock, 1);
        readingAggregator.process(newReading);

        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileWriter, times(1)).write(bucketCaptor.capture());
        assertThat(bucketCaptor.getValue().getSqsResponses()).containsOnly(oldReading);
    }

    @Test
    public void process_multipleReadingsExceedingDelayTime_areSentToFileWriter() {
        MutableClock clock = buildClock(0);
        SqsResponse oldReading1 = buildReading(generateTimestamp(clock, -5));
        SqsResponse oldReading2 = buildReading(generateTimestamp(clock, -4.9));
        SqsResponse oldReading3 = buildReading(generateTimestamp(clock, -4));
        SqsResponse newReading = buildReading(0);
        readingAggregator = new ReadingAggregator(fileWriter, clock);

        readingAggregator.process(oldReading1);
        readingAggregator.process(oldReading2);
        readingAggregator.process(oldReading3);
        advanceByMinutes(clock, 1);
        readingAggregator.process(newReading);

        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileWriter, times(1)).write(bucketCaptor.capture());
        assertThat(bucketCaptor.getValue().getSqsResponses()).containsOnly(oldReading1, oldReading2);
    }

    private long generateTimestamp(Clock clock, double modifyByMinutes) {
        return clock.millis() + (long) (modifyByMinutes * 60000);
    }

    private void advanceByMinutes(MutableClock clock, int minutes) {
        clock.add(minutes * 60000, ChronoUnit.MILLIS);
    }

    private MutableClock buildClock(long clockTime) {
        Instant fixedInstant = Instant.ofEpochMilli(clockTime);
        return MutableClock.of(fixedInstant, ZoneId.systemDefault());
    }

    private SqsResponse buildReading(long messageTimeStamp) {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessageTimestamp()).thenReturn(messageTimeStamp);
        return mockSqsResponse;
    }
}
