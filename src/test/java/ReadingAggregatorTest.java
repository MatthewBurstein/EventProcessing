import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.models.*;
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
    private GlobalConstants gc;
    private SensorList sensorList;

    @Before
    public void buildReadingAggregator() {
        clock = buildClock();
        fileService = Mockito.mock(CSVFileService.class);
        gc = new GlobalConstants(60, 5);
        sensorList = Mockito.mock(SensorList.class);
        readingAggregator = new ReadingAggregator(fileService, clock, gc, sensorList);
    }

    @Test
    public void process_nothingWhileNoReadingsAreOlderThanDelayTime() {
        Reading reading = buildReadingWithTimestampMinutes(0);
        readingAggregator.process(reading);
        verify(fileService, never()).write(any());
    }

    @Test
    public void process_singleReadingExceedingDelayTime_isSentToFileWriter() {
        Reading oldReading = buildReadingWithTimestampMinutes(-5);
        Reading newReading = buildReadingWithTimestampMinutes(0);

        readingAggregator.process(oldReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledWithBucketContainingReadings(oldReading);
    }

    @Test
    public void process_multipleReadingsExceedingDelayTime_areSentToFileWriter() {
        Reading oldReading1 = buildReadingWithTimestampMinutesAndId(-5, "id1");
        Reading oldReading2 = buildReadingWithTimestampMinutesAndId(-4.9, "id2");
        Reading oldReading3 = buildReadingWithTimestampMinutesAndId(-4, "id3");
        Reading newReading = buildReadingWithTimestampMinutesAndId(0, "id4");

        processMultipleReadings(oldReading1, oldReading2, oldReading3);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledWithBucketContainingReadings(oldReading1, oldReading2);
    }

    @Test
    public void process_sendsEachReadingToFileWriterOnlyOnce() {
        Reading oldReading1 = buildReadingWithTimestampMinutesAndId(-5, "id1");
        Reading oldReading2 = buildReadingWithTimestampMinutesAndId(-4.9, "id2");
        Reading oldReading3 = buildReadingWithTimestampMinutesAndId(-4, "id3");
        Reading newReading = buildReadingWithTimestampMinutesAndId(0, "idNew");

        processMultipleReadings(oldReading1, oldReading2, oldReading3);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledWithBucketContainingReadings(oldReading3);
    }

    @Test
    public void process_ensuresAllReadingsCanBeProcessedCorrectlyAtAllTimes() {
        Reading dummyReading = buildReadingWithTimestampMinutes(-5);
        Reading newReading = buildReadingWithTimestampMinutes(1.5);

        advanceClockByMinutes(6);
        processMultipleReadings(dummyReading, dummyReading, dummyReading, dummyReading, dummyReading, dummyReading);
        advanceClockByMinutes(1);
        readingAggregator.process(newReading);

        assertWriteCalledWithBucketContainingReadings(newReading);
    }

    @Test
    public void process_rejectsDuplicateIdReadings() {
        Reading duplicateReading1 = buildReadingWithTimestampMinutesAndId(-4.8, "duplicateReadingId");
        Reading duplicateReading2 = buildReadingWithTimestampMinutesAndId(-4.6, "duplicateReadingId");
        Reading uniqueReading = buildReadingWithTimestampMinutesAndId(-4.8, "uniqueReadingId");

        readingAggregator.process(duplicateReading1);
        readingAggregator.process(duplicateReading2);
        advanceClockByMinutes(1);
        readingAggregator.process(uniqueReading);

        assertWriteCalledWithBucketContainingReadings(duplicateReading1, uniqueReading);
    }

    @Test
    public void processAllBuckets_writesAllBucketsToFile() {
        Reading firstReading = buildReadingWithTimestampMinutesAndId(-4.8, "id1");
        Reading secondReading = buildReadingWithTimestampMinutesAndId(-3.8, "id2");
        Reading thirdReading = buildReadingWithTimestampMinutesAndId(-2.8, "id3");
        Reading fourthReading = buildReadingWithTimestampMinutesAndId(-1.8, "id4");
        Reading fifthReading = buildReadingWithTimestampMinutesAndId(-0.8, "od5");
        Reading sixthReading = buildReadingWithTimestampMinutesAndId(0.8, "id6");
        List<Reading> readings = Lists.newArrayList(firstReading, secondReading, thirdReading, fourthReading, fifthReading, sixthReading);

        processMultipleReadings(firstReading, secondReading, thirdReading, fourthReading, fifthReading, sixthReading);
        readingAggregator.processAllBuckets();

        assertCalledOnceWithEachReading(readings);
    }

    @Test
    public void process_callsAddReadingOnSensorListWithPassedReading() {
        Reading reading = buildReadingWithLocationTimestampAndValue("locationId", (long) -4,  1.3);
        readingAggregator.process(reading);

        ArgumentCaptor<Reading> readingCaptor = ArgumentCaptor.forClass(Reading.class);
        verify(sensorList, times(1)).storeSensorData(reading);
    }

    @Test
    public void finalise_whenOneReadingProcessed_writesSensorDataToFile() {
        Sensor sensor1 = Mockito.mock(Sensor.class);
        Sensor sensor2 = Mockito.mock(Sensor.class);
        List<Sensor> sensors = Lists.newArrayList(sensor1, sensor2);
        when(sensorList.getSensors()).thenReturn(sensors);
        readingAggregator.finalise();

        ArgumentCaptor<Sensor> sensorCaptor = ArgumentCaptor.forClass(Sensor.class);
        verify(fileService, times(2)).writeSensorData(sensorCaptor.capture());
        List<Sensor> arguments = sensorCaptor.getAllValues();
        assertThat(arguments).isEqualTo(sensors);
    }

    private Reading buildReadingWithLocationTimestampAndValue(String locationId, long timestamp, double value) {
        Reading reading = Mockito.mock(Reading.class);
        when(reading.getLocationId()).thenReturn(locationId);
        when(reading.getTimestamp()).thenReturn(timestamp);
        when(reading.getValue()).thenReturn(value);
        return reading;
    }

    private void assertCalledOnceWithEachReading(List<Reading> readings) {
        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileService, times(readings.size())).write(bucketCaptor.capture());
        for (int i = 0; i < bucketCaptor.getAllValues().size(); i++) {
            assertThat(bucketCaptor.getAllValues().get(i).getReadings()).containsOnly(readings.get(i));
        }
    }

    private void assertWriteCalledWithBucketContainingReadings(Reading... argsOfLastCall) {
        ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
        verify(fileService, atLeastOnce()).write(bucketCaptor.capture());
        Bucket argumentOfLastCall = Iterables.getLast(bucketCaptor.getAllValues());
        assertThat(argumentOfLastCall.getReadings()).containsOnly(argsOfLastCall);
    }

    private void processMultipleReadings(Reading... readings) {
        for (Reading reading : readings) {
            readingAggregator.process(reading);
        }
    }

    private Reading buildReadingWithTimestampMinutes(double withMinutes) {
        Reading reading = Mockito.mock(Reading.class);
        String currentTime = String.valueOf(System.currentTimeMillis());
        when(reading.getId()).thenReturn(currentTime);
        when(reading.getTimestamp()).thenReturn(generateTimestamp(withMinutes));
        return reading;
    }

    private Reading buildReadingWithTimestampMinutesAndId(double minutes, String id) {
        Reading reading = Mockito.mock(Reading.class);
        when(reading.getId()).thenReturn(String.valueOf(id));
        when(reading.getTimestamp()).thenReturn(generateTimestamp(minutes));
        return reading;
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
