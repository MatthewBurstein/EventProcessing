import com.google.common.collect.Lists;
import eventprocessing.models.InitialResponseList;
import eventprocessing.models.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class InitialResponseListTest {

    private InitialResponseList initialResponseList;

    @Before
    public void createObjects() {
        initialResponseList = new InitialResponseList();
        Response mockResponse1 = Mockito.mock(Response.class);
        Response mockResponse2 = Mockito.mock(Response.class);
        Response mockResponse3 = Mockito.mock(Response.class);
        when(mockResponse1.getTimestamp()).thenReturn((long) 9999);
        when(mockResponse2.getTimestamp()).thenReturn((long) 333);
        when(mockResponse3.getTimestamp()).thenReturn((long) 22);
        initialResponseList.getResponses().addAll(Lists.newArrayList(mockResponse1, mockResponse2, mockResponse3));
    }

    @Test
    public void getEarliestTimestamp_returnsResponseWithEarliestTimestamp() {
        assertEquals(initialResponseList.getEarliestTimestamp(), 22);
    }
}
