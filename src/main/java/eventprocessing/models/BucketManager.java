package eventprocessing.models;

import eventprocessing.GlobalConstants;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BucketManager {
    private List<ResponseList> buckets = new ArrayList<>();
    private long nextStartTime;
    private StopWatch stopWatch;

    public BucketManager(long initialTime, StopWatch stopWatch) {
        this.stopWatch = stopWatch;

        nextStartTime = initialTime;
        while (buckets.size() < GlobalConstants.MAX_MESSAGE_DELAY_MINS) {
            createBucket(nextStartTime);
        }
    }

    public void createBucket(long thisBucketStartTime) {
        ResponseList bucket = new ResponseList(thisBucketStartTime);
        buckets.add(bucket);
        nextStartTime = thisBucketStartTime + GlobalConstants.BUCKET_UPPER_BOUND;
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

    public ResponseList removeExpiredBucket() {
        ResponseList removedBucket = null;
        for (ResponseList bucket : buckets) {
            if (bucket.isExpiredAtTime(stopWatch.getTime())) {
                removedBucket = bucket;
            }
        }
        return remove(removedBucket);
    }

    public void addMultipleResponsesToBucket(ResponseList responseList) {
        for (Response response : responseList.getResponses()) {
            addResponseToBucket(response);
        }
    }

    public ResponseList remove(ResponseList responseList) {
        buckets.remove(responseList);
        return responseList;
    }

    public List<String> getMessageIds() {
        return buckets.stream()
                .map(bucket -> bucket.getMessageIds())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
