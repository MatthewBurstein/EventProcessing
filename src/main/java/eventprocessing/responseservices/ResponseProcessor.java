package eventprocessing.responseservices;

import eventprocessing.models.BucketManager;
import eventprocessing.models.SqsResponse;
import eventprocessing.models.Bucket;
import eventprocessing.models.SensorList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseProcessor {
    private final static Logger logger = LogManager.getLogger("ResponseProcessor");

    public boolean isValidMessage(SqsResponse sqsResponse, SensorList sensorList, BucketManager bucketManager) {
        boolean isValid = true;
        for(Bucket bucket : bucketManager.getBuckets()) {
            if(!sensorList.isWorkingSensor(sqsResponse)) {
                isValid = false;
            }
            if (isDuplicateMessage(sqsResponse, bucket)) {
                isValid = false;
            }
        }
        return isValid;
    }

    public boolean isValidMessage(SqsResponse sqsResponse, SensorList sensorList, Bucket bucket) {
        return sensorList.isWorkingSensor(sqsResponse) && !isDuplicateMessage(sqsResponse, bucket);
    }

    public boolean isDuplicateMessage(SqsResponse sqsResponse, Bucket bucket) {
        boolean result = bucket.getMessageIds().contains(sqsResponse.getMessageId());
        return result;
    }
}
