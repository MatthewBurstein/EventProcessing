import com.google.common.collect.Lists;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.models.Bucket;
import eventprocessing.models.Reading;
import eventprocessing.models.Sensor;
import org.apache.commons.lang3.Range;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;

public class CsvFileServiceTest {

    @Test
    public void writeBucketDataAndWriteMultipleBucketData_createFileWithCalculatedAveragesForExpiredBuckets() throws IOException {
        String testDataFileName = "TestResponseData.csv";
        Files.deleteIfExists(Paths.get(testDataFileName));
        CSVFileService csvFileService = new CSVFileService(testDataFileName, "sensorCsvFile");
        Bucket bucket1 = buildBucketWithRangeAndValue(0, 5999, 1);
        Bucket bucket2 = buildBucketWithRangeAndValue(6000, 11999, 2);
        Bucket bucket3 = buildBucketWithRangeAndValue(12000, 17999, 3);

        csvFileService.write(bucket1);
        csvFileService.write(bucket2);
        csvFileService.write(bucket3);
        /*No assertions for this test - please open TestResponseData.csv for results.
        Expected outputs are as follows:

        Start Time,End Time,Number of Responses,Average Value
        0,5999,1,0.0 => this line will not be printed if writing the file overwrited previous information
        6000,11999,2,0.0
        12000,17999,3,0.0

        */
    }

    @Test
    public void writeSensorData_createsFileWithAverageValuesForEachSensor() throws IOException {
        String testDataFileName = "TestSensorAverages.csv";
        Files.deleteIfExists(Paths.get(testDataFileName));
        CSVFileService csvFileService = new CSVFileService("bucketCsvFile", testDataFileName);
        Sensor sensor1 = buildSensorWithLocationAndTotalValueAndNumberOfReadings("location1", 5.0, 2);
        Sensor sensor2 = buildSensorWithLocationAndTotalValueAndNumberOfReadings("location2", 4.0, 5);
        Sensor sensor3 = buildSensorWithLocationAndTotalValueAndNumberOfReadings("location3", 5.0, 6);
        csvFileService.writeSensorData(sensor1);
        csvFileService.writeSensorData(sensor2);
        csvFileService.writeSensorData(sensor3);
        /*No assertions for this test - please open TestResponseData.csv for results.
        Expected outputs are as follows:

        Location Id,Number of Readings,Average Value
        location1,2,5
        location2,5,4
        location3,6,5
        */
    }

    private Bucket buildBucketWithRangeAndValue(long rangeStart, long rangeEnd, double averageValue) {
        Bucket bucket = Mockito.mock(Bucket.class);
        Reading reading = Mockito.mock(Reading.class);
        when(bucket.getTimeRange()).thenReturn(Range.between(rangeStart, rangeEnd));
        when(bucket.getReadings()).thenReturn(Lists.newArrayList(reading));
        when(bucket.getAverageValue()).thenReturn(averageValue);
        return bucket;
    }

    private Sensor buildSensorWithLocationAndTotalValueAndNumberOfReadings(String locationId, double averageValue, int numberOfReadings) {
        Sensor sensor = Mockito.mock(Sensor.class);
        when(sensor.getLocationId()).thenReturn(locationId);
        when(sensor.getAverageValue()).thenReturn(averageValue);
        when(sensor.getNumberOfReadings()).thenReturn(numberOfReadings);
        return sensor;
    }

}
