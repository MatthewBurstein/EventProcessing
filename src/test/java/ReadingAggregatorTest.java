import com.google.common.collect.Lists;
import eventprocessing.fileservices.CSVFileService;
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
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReadingAggregatorTest {

    private ReadingAggregator readingAggregator;
    private CSVFileService fileService;
    private MutableClock clock;

    @Before
    public void buildReadingAggregator() {
        clock = buildClock();
        fileService = Mockito.mock(CSVFileService.class);
        readingAggregator = new ReadingAggregator(fileService, clock);
    }

    @Test
    public void process_nothingWhileNoReadingsAreOlderThanDelayTime() {
        SqsResponse reading = buildReadingWithTimestampMinutes(0);
        readingAggregator.process(reading);
        verify(fileService, never()).write(any());
    }

    @Test
    public void process_singleReadingExceedingDelayTime_isSentToFileWriter() {
        SqsResponse oldReading = buildReadingWithTimestampMinutes(-5);
        SqsResponse newReading = buildReadingWithTimestampMinutes(0);

        readingAggregator.process(oldReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(1, oldReading);
    }

    @Test
    public void process_multipleReadingsExceedingDelayTime_areSentToFileWriter() {
        SqsResponse oldReading1 = buildReadingWithTimestampMinutesAndId(-5, "W");
        SqsResponse oldReading2 = buildReadingWithTimestampMinutesAndId(-4.9, "E");
        SqsResponse oldReading3 = buildReadingWithTimestampMinutesAndId(-4, "L");
        SqsResponse newReading = buildReadingWithTimestampMinutesAndId(0, "P");

        processMultipleReadings(oldReading1, oldReading2, oldReading3);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(1, oldReading1, oldReading2);
    }

    @Test
    public void process_sendsEachReadingToFileWriterOnlyOnce() {
        SqsResponse oldReading1 = buildReadingWithTimestampMinutes(-5);
        SqsResponse oldReading2 = buildReadingWithTimestampMinutes(-4.9);
        SqsResponse oldReading3 = buildReadingWithTimestampMinutes(-4);
        SqsResponse newReading = buildReadingWithTimestampMinutes(0);

        processMultipleReadings(oldReading1, oldReading2, oldReading3);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(2, oldReading3);
    }

    @Test
    public void process_ensuresAllReadingsCanBeProcessedCorrectlyAtAllTimes() {
        SqsResponse dummyReading = buildReadingWithTimestampMinutes(-5);
        SqsResponse newReading = buildReadingWithTimestampMinutes(1.5);

        advanceClockByMinutes(6);
        processMultipleReadings(dummyReading, dummyReading, dummyReading, dummyReading, dummyReading, dummyReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledTimesWithReadings(7, newReading);
    }

    @Test
    public void process_rejectsDuplicateIdMessages() {
        SqsResponse duplicateReading1 = buildReadingWithTimestampMinutesAndId(-4.8, "duplicateMessageId");
        SqsResponse duplicateReading2 = buildReadingWithTimestampMinutesAndId(-4.6, "duplicateMessageId");
        SqsResponse uniqueReading = buildReadingWithTimestampMinutesAndId(-4.8, "uniqueMessageId");

        readingAggregator.process(duplicateReading1);
        readingAggregator.process(duplicateReading2);
        advanceClockByMinutes(1);
        readingAggregator.process(uniqueReading);

        assertWriteCalledTimesWithReadings(1, duplicateReading1, uniqueReading);
    }

    @Test
    public void processAllBuckets_writesAllBucketsToFile() {
        SqsResponse firstReading = buildReadingWithTimestampMinutes(-4.8);
        SqsResponse secondReading = buildReadingWithTimestampMinutes(-3.8);
        SqsResponse thirdReading = buildReadingWithTimestampMinutes(-2.8);
        SqsResponse fourthReading = buildReadingWithTimestampMinutes(-1.8);
        SqsResponse fifthReading = buildReadingWithTimestampMinutes(-0.8);
        SqsResponse sixthReading = buildReadingWithTimestampMinutes(0.8);
        List<SqsResponse> readings = Lists.newArrayList(firstReading, secondReading, thirdReading, fourthReading, fifthReading, sixthReading);

        processMultipleReadings(firstReading, secondReading, thirdReading, fourthReading, fifthReading, sixthReading);
        readingAggregator.processAllBuckets();

        assertCalledOnceWithEachReading(readings);
    }

    private void assertCalledOnceWithEachReading(List<SqsResponse> readings) {
        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileService, times(readings.size())).write(bucketCaptor.capture());
        for (int i = 0; i < bucketCaptor.getAllValues().size(); i++) {
            assertThat(bucketCaptor.getAllValues().get(i).getSqsResponses()).containsOnly(readings.get(i));
        }
    }

    private void assertWriteCalledTimesWithReadings(int numberOfTimesCalled, SqsResponse... argsOfLastCall) {
        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileService, times(numberOfTimesCalled)).write(bucketCaptor.capture());
        assertThat(bucketCaptor.getValue().getSqsResponses()).containsOnly(argsOfLastCall);
    }

    private void processMultipleReadings(SqsResponse... sqsResponses) {
        for (SqsResponse response : sqsResponses) {
            readingAggregator.process(response);
        }
    }

    private SqsResponse buildReadingWithTimestampMinutes(double withMinutes) {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        String currentTime = String.valueOf(System.currentTimeMillis());
        when(mockSqsResponse.getMessageId()).thenReturn(currentTime);
        when(mockSqsResponse.getMessageTimestamp()).thenReturn(generateTimestamp(withMinutes));
        return mockSqsResponse;
    }

    private SqsResponse buildReadingWithTimestampMinutesAndId(double minutes, String id) {
        SqsResponse mockReading = Mockito.mock(SqsResponse.class);
        when(mockReading.getMessageId()).thenReturn(String.valueOf(id));
        when(mockReading.getMessageTimestamp()).thenReturn(generateTimestamp(minutes));
        return mockReading;
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
