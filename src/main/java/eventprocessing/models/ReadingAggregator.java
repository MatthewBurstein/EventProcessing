package eventprocessing.models;

import com.google.common.collect.Iterables;
import eventprocessing.fileservices.CSVFileWriter;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class ReadingAggregator {
    private CSVFileWriter csvFileWriter;
    private Clock clock;
    private List<Bucket> buckets;

    public ReadingAggregator(CSVFileWriter csvFileWriter, Clock clock) {
        this.csvFileWriter = csvFileWriter;
        this.clock = clock;
        this.buckets = new ArrayList<>();
        createInitialBuckets();
        System.out.println("Constructor buckets " + buckets);
    }

    public void process(SqsResponse sqsResponse) {
        System.out.println("Process Buckets " + buckets);
        assignResponseToBucket(sqsResponse);
        Bucket bucket = removeExpiredBucket();
        if (bucket != null) {
            System.out.println("Removed Bucket " + bucket);
            System.out.println(bucket.getSqsResponses().size());
            csvFileWriter.write(bucket);
            createNextBucket();
        }
        System.out.println("New Buckets " + buckets.size());
    }

    private Bucket getExpiredBucket() {
        Bucket expiredBucket = null;
        for (Bucket bucket : buckets) {
            if (bucket.isExpiredAtTime(clock.millis())) {
                expiredBucket = bucket;
            }
        }
        return expiredBucket;
    }

    private void createBucket(long startTime) {
        buckets.add(new Bucket(startTime));
    }

    private void createInitialBuckets() {
        long currentTime = clock.millis();
        long thisBucketStartTime = currentTime - 300000;
        while (thisBucketStartTime <= currentTime) {
            createBucket(thisBucketStartTime);
            thisBucketStartTime += 60000;
        }
    }

    private void assignResponseToBucket(SqsResponse sqsResponse) {
        buckets.forEach(bucket -> {
            if (bucket.getTimeRange().contains(sqsResponse.getMessageTimestamp())) {
                bucket.addResponse(sqsResponse);
            }
        });
    }

    private Bucket removeExpiredBucket() {
        Bucket expiredBucket = getExpiredBucket();
        System.out.println("Expired bucket to be removed " + Iterables.getFirst(buckets, 0));
        System.out.println("buckets.remove(expiredBucket);" + buckets.remove(expiredBucket));
        return expiredBucket;
    }

    private void createNextBucket() {
        Bucket lastBucket = Iterables.getLast(buckets);
        long nextBucketStartTime = lastBucket.getTimeRange().getMaximum() + 1;
        createBucket(nextBucketStartTime);
        System.out.println("All buckets : " + buckets);
        System.out.println("New bucket " + Iterables.getLast(buckets));
    }
}
