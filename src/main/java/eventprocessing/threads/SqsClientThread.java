package eventprocessing.threads;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.amazonservices.SqsClient;
import eventprocessing.models.TemporarySqsResponseStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqsClientThread extends Thread {
    private static Logger logger = LogManager.getLogger("SqsClientThread ");

    private final SqsClient sqsClient;
    private final String queueUrl;
    private TemporarySqsResponseStorage temporarySqsResponseStorage;
    private ReceiveMessageRequest receiveMessageRequest;
    private volatile boolean running = true;

    public SqsClientThread(TemporarySqsResponseStorage temporarySqsResponseStorage, SqsClient sqsClient, ReceiveMessageRequest receiveMessageRequest, String queueUrl) {
        this.temporarySqsResponseStorage = temporarySqsResponseStorage;
        this.sqsClient = sqsClient;
        this.receiveMessageRequest = receiveMessageRequest;
        this.queueUrl = queueUrl;
    }

    public void run() {
        while (running) {
            ReceiveMessageResult messageResult = sqsClient.getSqs().receiveMessage(receiveMessageRequest);
            try {
                temporarySqsResponseStorage.put(messageResult);
                if (messageResult.getMessages().size() > 0) {
                    final String messageReceiptHandle = messageResult.getMessages().get(0).getReceiptHandle();
                    sqsClient.getSqs().deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
                }
            } catch (InterruptedException e) {
                logger.error(e.getStackTrace());
            }
        }
    }

    public void terminate() {
        running = false;
    }
}
