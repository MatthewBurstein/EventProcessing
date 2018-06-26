import com.google.common.collect.Lists;
import eventprocessing.storage.MessageLog;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

public class MessageLogTest {
    private MessageLog messageLog;

    @Before
    public void createObjects() {
        messageLog = new MessageLog(2);
    }

    @Test
    public void truncateIfExceedsMaxSize_whenGreaterThanMaxSize_truncatesMessageLog() {
        ArrayList<String> messageLogIds = Lists.newArrayList("item1", "item2", "item3", "item4");
        messageLog.getMessageHistory().addAll(messageLogIds);
        messageLog.truncateIfExceedsMaxSize();
        ArrayList<String> expectedMessageLog = Lists.newArrayList("item3", "item4");
        assertArrayEquals(expectedMessageLog.toArray(), messageLog.getMessageHistory().toArray());
    }

    @Test
    public void truncateIfExceedsMaxSize_whenSmallerThanMaxSize_doesNothing() {
        messageLog.getMessageHistory().add("item1");
        messageLog.truncateIfExceedsMaxSize();
        ArrayList<String> expectedMessageLog = Lists.newArrayList("item1");
        assertArrayEquals(expectedMessageLog.toArray(), messageLog.getMessageHistory().toArray());
    }
}
