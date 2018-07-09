package eventprocessing.threads;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.models.*;
import eventprocessing.responseservices.SqsResponseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseProcessorThread extends Thread {
    private static Logger logger = LogManager.getLogger("ResponseProcessorThread");

    private final SqsResponseService sqsResponseService = new SqsResponseService();
    private TemporarySqsResponseStorage temporarySqsResponseStorage;
    private SensorList sensorList;
    private ReadingAggregator readingAggregator;
    private boolean running = true;
    private int notWorkingSensorCount = 0;
    private int messageCounter = 0;

    public ResponseProcessorThread(TemporarySqsResponseStorage temporarySqsResponseStorage, SensorList sensorList, ReadingAggregator readingAggregator) {
        this.temporarySqsResponseStorage = temporarySqsResponseStorage;
        this.sensorList = sensorList;
        this.readingAggregator = readingAggregator;
    }

    public void run() {
        while (running) {
            ReceiveMessageResult messageResult = getMessageResultFromStorage();
            processMessageResult(messageResult);
        }
        readingAggregator.finalise();
        logger.info("Total messages received: " + messageCounter);
        logger.info("Total faulty sensors in this run: " + notWorkingSensorCount);
    }

    private ReceiveMessageResult getMessageResultFromStorage() {
        try {
            ReceiveMessageResult messageResult = temporarySqsResponseStorage.take();
            return messageResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMessageResult(ReceiveMessageResult messageResult) {
        for (Message msg : messageResult.getMessages()) {
            try {
                processMessage(msg);
            } catch (InvalidSqsResponseException e) {
                logger.error("Invalid JSON string received" + e.getMessage());
                logger.error("Received json string: " + msg.getBody());
            }
        }
    }

    private void processMessage(Message msg) {
        SqsResponse sqsResponse = sqsResponseService.parseResponse(msg.getBody());
        Reading reading = new Reading(sqsResponse);
        if (sensorList.isWorkingSensor(reading)) {
            readingAggregator.process(reading);
            messageCounter++;
        } else {
            notWorkingSensorCount++;
        }
    }

    public void terminate() {
        running = false;
    }
}


