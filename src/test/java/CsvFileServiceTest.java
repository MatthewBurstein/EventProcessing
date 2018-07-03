import com.google.common.collect.Lists;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.models.Bucket;
import eventprocessing.models.SqsResponse;
import org.apache.commons.lang3.Range;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.when;

public class CsvFileServiceTest {

    @Test
    public void writeBucketDataAndWriteMultipleBucketData_createFileWithCalculatedAveragesForExpiredBuckets() throws IOException {
        String testDataFileName = "TestResponseData.csv";
        Files.deleteIfExists(Paths.get(testDataFileName));
        CSVFileService csvFileService = new CSVFileService(testDataFileName);
        Bucket mockBucket1 = setupMockBucket(0, 5999, 1);
        Bucket mockBucket2 = setupMockBucket(6000, 11999, 2);
        Bucket mockBucket3 = setupMockBucket(12000, 17999, 3);

        csvFileService.writeBucketDataToFile(mockBucket1);

        List<Bucket> mockBuckets = Lists.newArrayList(mockBucket2, mockBucket3);
        csvFileService.writeMultipleBucketDataToFile(mockBuckets);
        /*No assertions for this test - please open TestResponseData.csv for results.
        Expected outputs are as follows:

        Start Time,End Time,Number of Responses,Average Value
        0,5999,1,0.0 => this line will not be printed if writing the file overwrited previous information
        6000,11999,2,0.0
        12000,17999,3,0.0

        */
    }

    private Bucket setupMockBucket(long rangeStart, long rangeEnd, double averageValue) {
        Bucket mockBucket = Mockito.mock(Bucket.class);
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockBucket.getTimeRange()).thenReturn(Range.between(rangeStart, rangeEnd));
        when(mockBucket.getSqsResponses()).thenReturn(Lists.newArrayList(mockSqsResponse));
        when(mockBucket.getAverageValue()).thenReturn(averageValue);
        return mockBucket;
    }

}
