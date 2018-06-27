import eventprocessing.models.ResponseList;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ResponseListTest {

    private ResponseList responseList;

    @Before
    public void createResponseList() {
        responseList = new ResponseList(100);
    }

    @Test
    public void isExpiredAtTime_returnsFalseWhenPassedArgumentInRange() {
        assertFalse(responseList.isExpiredAtTime(101));
    }

    @Test public void isExpiredAtTime_returnsTrueWhenPassedArgumentOutsideRange() {
        TestCase.assertTrue(responseList.isExpiredAtTime(160));
    }
}
