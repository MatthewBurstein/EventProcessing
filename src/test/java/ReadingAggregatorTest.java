import eventprocessing.fileservices.CSVFileWriter;
import eventprocessing.models.Bucket;
import eventprocessing.models.ReadingAggregator;
import eventprocessing.models.SqsResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.threeten.extra.MutableClock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReadingAggregatorTest {

    private ReadingAggregator readingAggregator;
    private CSVFileWriter fileWriter;
    private MutableClock clock;

    @Before
    public void buildReadingAggregator() {
        clock = buildClock();
        fileWriter = Mockito.mock(CSVFileWriter.class);
        readingAggregator = new ReadingAggregator(fileWriter, clock);
    }

    @Test
    public void process_nothingWhileNoReadingsAreOlderThanDelayTime() {
        SqsResponse reading = buildReading(0);
        readingAggregator.process(reading);
        verify(fileWriter, never()).write(any());
    }

    @Test
    public void process_singleReadingExceedingDelayTime_isSentToFileWriter() {
        SqsResponse oldReading = buildReading(-5);
        SqsResponse newReading = buildReading(0);

        readingAggregator.process(oldReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(1, oldReading);
    }

    @Test
    public void process_multipleReadingsExceedingDelayTime_areSentToFileWriter() {
        SqsResponse oldReading1 = buildReading(-5);
        SqsResponse oldReading2 = buildReading(-4.9);
        SqsResponse oldReading3 = buildReading(-4);
        SqsResponse newReading = buildReading(0);

        processMultipleReadings(oldReading1, oldReading2, oldReading3);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(1, oldReading1, oldReading2);
    }

    @Test
    public void process_sendsEachReadingToFileWriterOnlyOnce() {
        SqsResponse oldReading1 = buildReading(-5);
        SqsResponse oldReading2 = buildReading(-4.9);
        SqsResponse oldReading3 = buildReading(-4);
        SqsResponse newReading = buildReading(0);

        processMultipleReadings(oldReading1, oldReading2, oldReading3);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(2, oldReading3);
    }

    @Test
    public void process_ensuresAllReadingsCanBeProcessedCorrectlyAtAllTimes() {
        SqsResponse dummyReading = buildReading(-5);
        SqsResponse newReading = buildReading(1.5);

        advanceClockByMinutes(6);
        processMultipleReadings(dummyReading,dummyReading,dummyReading,dummyReading,dummyReading,dummyReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(7, newReading);
    }

    private void assertWriteCalledTimesWithReadings(int numberOfTimesCalled, SqsResponse ... argsOfLastCall) {
        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileWriter, times(numberOfTimesCalled)).write(bucketCaptor.capture());
        assertThat(bucketCaptor.getValue().getSqsResponses()).containsOnly(argsOfLastCall);
    }

    private void processMultipleReadings(SqsResponse ... sqsResponses) {
        for(SqsResponse response : sqsResponses) {
            readingAggregator.process(response);
        }
    }

    private SqsResponse buildReading(double withMinutes) {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessageTimestamp()).thenReturn(generateTimestamp(withMinutes));
        return mockSqsResponse;
    }

    private long generateTimestamp(double atMinute) {
        return clock.millis() + (long) (atMinute * 60000);
    }

    private void advanceClockByMinutes(int minutes) {
        clock.add(minutes * 60000, ChronoUnit.MILLIS);
    }

    private MutableClock buildClock() {
        Instant fixedInstant = Instant.ofEpochMilli(0);
        return MutableClock.of(fixedInstant, ZoneId.systemDefault());
    }
}
