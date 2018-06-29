import com.google.common.collect.Lists;
import eventprocessing.analysis.Analyser;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.models.Bucket;
import eventprocessing.models.Response;
import org.apache.commons.lang3.Range;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.when;

public class CsvFileServiceTest {

    @Test
    public void writeRemovedBucketsToFile_createsFileWithCalculatedAveragesForExpiredBuckets() throws IOException {
        CSVFileService csvFileService = new CSVFileService("testResponseData.csv");
        Analyser mockAnalyser = Mockito.mock(Analyser.class);
        Bucket mockBucket1 = Mockito.mock(Bucket.class);
        Bucket mockBucket2 = Mockito.mock(Bucket.class);
        Bucket mockBucket3 = Mockito.mock(Bucket.class);
        Response mockResponse = Mockito.mock(Response.class);

        when(mockBucket1.getTimeRange()).thenReturn(Range.between((long) 0, (long) 5999));
        when(mockBucket1.getResponses()).thenReturn(Lists.newArrayList(mockResponse));
        when(mockAnalyser.getAverageValue(mockBucket1)).thenReturn(1.0);

        when(mockBucket2.getTimeRange()).thenReturn(Range.between((long) 6000, (long) 11999));
        when(mockBucket2.getResponses()).thenReturn(Lists.newArrayList(mockResponse, mockResponse));
        when(mockAnalyser.getAverageValue(mockBucket2)).thenReturn(2.0);

        when(mockBucket3.getTimeRange()).thenReturn(Range.between((long) 12000, (long) 17999));
        when(mockBucket3.getResponses()).thenReturn(Lists.newArrayList(mockResponse, mockResponse, mockResponse));
        when(mockAnalyser.getAverageValue(mockBucket3)).thenReturn(3.0);

        List<Bucket> mockBuckets = Lists.newArrayList(mockBucket1, mockBucket2, mockBucket3);
        csvFileService.generateOutputLine(mockBuckets);
        //No assertions for this test - please open testResponseData.csv for results
    }
}
