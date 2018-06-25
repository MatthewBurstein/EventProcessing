package eventprocessing.storage;

import java.util.List;

public class MessageLog {

    private List<String> messageHistory;


    public List<String> getMessageHistory() {
        return messageHistory;
    }

    public void setMessageHistory(List<String> messageHistory) {
        this.messageHistory = messageHistory;
    }
}
