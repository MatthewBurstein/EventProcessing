package eventprocessing.models;

import eventprocessing.GlobalConstants;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BucketManager {
    private List<Bucket> buckets = new ArrayList<>();
    private long nextStartTime;
    private StopWatch stopWatch;

    public BucketManager(long initialTime, StopWatch stopWatch) {
        this.stopWatch = stopWatch;

        nextStartTime = initialTime;
        while (buckets.size() <= GlobalConstants.MAX_MESSAGE_DELAY_MINS) {
            createBucket(nextStartTime);
        }
    }

    public void createBucket(long thisBucketStartTime) {
        Bucket bucket = new Bucket(thisBucketStartTime);
        buckets.add(bucket);
        nextStartTime = thisBucketStartTime + GlobalConstants.BUCKET_UPPER_BOUND;
    }

    public List<Bucket> getBuckets() {
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

    public void addMultipleResponsesToBucket(Bucket bucket) {
        for (Response response : bucket.getResponses()) {
            addResponseToBucket(response);
        }
    }

    public Bucket removeExpiredBucket(long expiryTime) {
        Bucket removedBucket = null;
        for (Bucket bucket : buckets) {
            if (bucket.isExpiredAtTime(expiryTime)) {
                removedBucket = bucket;
            }
        }
        return remove(removedBucket);
    }


    public Bucket remove(Bucket bucket) {
        buckets.remove(bucket);
        return bucket;
    }

    public List<String> getMessageIds() {
        return buckets.stream()
                .map(bucket -> bucket.getMessageIds())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
