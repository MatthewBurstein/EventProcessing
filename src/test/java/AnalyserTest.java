import com.google.common.collect.Lists;
import eventprocessing.analysis.Analyser;
import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AnalyserTest {
    @Test
    public void getAverageValue_returnsAverageOfMessageValues() {
        Analyser analyser = new Analyser();
        ResponseList mockResponseList = Mockito.mock(ResponseList.class);
        Response mockResponse = Mockito.mock(Response.class);
        List<Response> mockResponses = Lists.newArrayList(mockResponse, mockResponse, mockResponse);
        ArrayList<Double> mockValues = Lists.newArrayList(1.0,2.0,3.0);
        when(mockResponseList.getMessageValues()).thenReturn(mockValues);
        when(mockResponseList.getResponses()).thenReturn(mockResponses);

        double expectedValue = 2.0;
        assertEquals(expectedValue, analyser.getAverageValue(mockResponseList), 0);
    }
}