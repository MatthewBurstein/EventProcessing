package eventprocessing.storage;

import java.util.ArrayList;
import java.util.List;

public class MessageLog {

    private List<String> messageHistory = new ArrayList<>();
    private int maxSize;

    public MessageLog(int maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }

    public void truncateIfExceedsMaxSize() {
        int currentSize = messageHistory.size();
        while (currentSize > maxSize) {
            messageHistory.remove(0);
            currentSize = messageHistory.size();
        }
    }
}
