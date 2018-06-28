import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ResponseListTest {

    private ResponseList responseList;

    @Before
    public void createResponseList() {
        responseList = new ResponseList(100);
    }

    @Test
    public void isExpiredAtTime_returnsTrueWhenPassedArgumentIsBeforeRange() {
        assertFalse(responseList.isExpiredAtTime(90));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsInRange() {
        assertFalse(responseList.isExpiredAtTime(101));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsWithinFiveMins() {
        assertFalse(responseList.isExpiredAtTime(200));
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentIsAfterFiveMins() {
        assertTrue(responseList.isExpiredAtTime(461));
    }
    
    @Test
    public void getMessageIds_returnsListOfMessageIds() {
        Response mockResponse1 = Mockito.mock(Response.class);
        when(mockResponse1.getMessageId()).thenReturn("mockResponse1id");
        Response mockResponse2 = Mockito.mock(Response.class);
        when(mockResponse2.getMessageId()).thenReturn("mockResponse2id");
        responseList.getResponses().addAll(Lists.newArrayList(mockResponse1, mockResponse2));
        List<String> expected = Lists.newArrayList("mockResponse1id", "mockResponse2id");
        assertEquals(responseList.getMessageIds(), expected);
    }
    
}
