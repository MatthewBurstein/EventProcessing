package eventprocessing.models;

import eventprocessing.GlobalConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BucketManager {
    private List<Bucket> buckets = new ArrayList<>();
    private long nextStartTime;

    public BucketManager(long initialTime) {

        nextStartTime = initialTime;
        while (buckets.size() < (2 * GlobalConstants.MAX_MESSAGE_DELAY_MINS)) {
            createBucket(nextStartTime);
        }
    }

    public long getNextStartTime() {
        return nextStartTime;
    }

    public void createBucket(long thisBucketStartTime) {
        Bucket bucket = new Bucket(thisBucketStartTime);
        buckets.add(bucket);
        nextStartTime = thisBucketStartTime + GlobalConstants.BUCKET_UPPER_BOUND;
    }

    public void createNextBucket() {
        createBucket(nextStartTime);
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void addResponseToBucket(SqsResponse sqsResponse) {
        long currentResponseTimestamp = sqsResponse.getMessageTimestamp();
        buckets.forEach(bucket -> {
            if (bucket.getTimeRange().contains(currentResponseTimestamp)) {
                bucket.addResponse(sqsResponse);
            }
        });
    }

    public void addMultipleResponsesToBucket(Bucket bucket) {
        for (SqsResponse sqsResponse : bucket.getSqsResponses()) {
            addResponseToBucket(sqsResponse);
        }
    }

    public List<Bucket> removeMultipleExpiredBuckets(long expiryTime) {
        List<Bucket> output = new ArrayList<>();
        for (Bucket bucket : buckets) {
            if (bucket.isExpiredAtTime(expiryTime)) {
                output.add(bucket);
            }
        }
        buckets.removeAll(output);
        return output;
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
}
