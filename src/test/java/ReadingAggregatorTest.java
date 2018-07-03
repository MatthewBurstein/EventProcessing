import com.google.common.collect.Lists;
import eventprocessing.fileservices.CSVFileWriter;
import eventprocessing.models.Bucket;
import eventprocessing.models.ReadingAggregator;
import eventprocessing.models.SqsResponse;
import org.apache.commons.lang3.Range;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ReadingAggregatorTest {

    private ReadingAggregator readingAggregator;
    private CSVFileWriter mockCsvFileWriter;

    @Before
    public void buildReadingAggregator() {
        mockCsvFileWriter = Mockito.mock(CSVFileWriter.class);
        readingAggregator = new ReadingAggregator(mockCsvFileWriter);
    }

    @Test
    public void process_receivesSingleEventAndSendsNothingToFileWriter() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        readingAggregator.process(mockSqsResponse);
        verify(mockCsvFileWriter, never()).write(any());
    }

    @Test
    public void process_receivesTwoEventsAndSendsOneBucketSummaryToFileWriter() {
        SqsResponse mockSqsResponse1 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse1.getMessageTimestamp()).thenReturn(System.currentTimeMillis() - 300002);
        Bucket mockBucket = Mockito.mock(Bucket.class);
        when(mockBucket.getTimeRange()).thenReturn(Range.between(System.currentTimeMillis() - 360000, System.currentTimeMillis() - 300001));
        readingAggregator.getBuckets().add(mockBucket);
        readingAggregator.process(mockSqsResponse1);
        verify(mockCsvFileWriter, times(1)).write(mockBucket);


    }
}
