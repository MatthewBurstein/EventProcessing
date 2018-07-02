package eventprocessing.models;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import java.util.concurrent.*;

public class TemporarySqsResponseStorage {

    private BlockingQueue<ReceiveMessageResult> queue = new LinkedBlockingQueue<>();

    public void put(ReceiveMessageResult receiveMessageResult) throws InterruptedException {
        queue.put(receiveMessageResult);
    }

    public ReceiveMessageResult take() throws InterruptedException {
        return queue.take();
    }

}
