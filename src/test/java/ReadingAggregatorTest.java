import eventprocessing.fileservices.CSVFileWriter;
import eventprocessing.models.ReadingAggregator;
import eventprocessing.models.SqsResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ReadingAggregatorTest {

    private ReadingAggregator readingAggregator;
    private CSVFileWriter mockCsvFileWriter;

    @Before
    public void buildReadingAggregator() {
        mockCsvFileWriter = Mockito.mock(CSVFileWriter.class);
        readingAggregator = new ReadingAggregator(mockCsvFileWriter);
    }

    @Test
    public void sensorReadingProcessor_receivesSingleEventSendsNothingToFileWriter() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        readingAggregator.process(mockSqsResponse);
        verify(mockCsvFileWriter, never()).write();
    }
}
