package eventprocessing.models;

import org.apache.commons.lang3.Range;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BucketManager {
    private List<ResponseList> buckets = new ArrayList<>();
    private long nextStartTime;

    public BucketManager(long initialTime) {
        nextStartTime = initialTime;

        while (buckets.size() < 5) {
            createBucket(nextStartTime);
        }
    }

    public void createBucket(long thisBucketStartTime) {
        ResponseList bucket = new ResponseList(thisBucketStartTime);
        buckets.add(bucket);
        nextStartTime = thisBucketStartTime + 60;
    }

    public List<ResponseList> getBuckets() {
        return buckets;
    }

    public long getNextStartTime() {
        return nextStartTime;
    }

    public void addResponseToBucket(Response response) {
        long currentResponseTimestamp = response.getTimestamp();
        buckets.forEach(bucket -> {
            if (bucket.getTimeRange().contains(currentResponseTimestamp)) {
                bucket.addResponse(response);
            }
        });
    }

    public void addMultipleResponsesToBucket(ResponseList responseList) {
        for (Response response : responseList.getResponses()) {
            addResponseToBucket(response);
        }
    }
}
