package eventprocessing.models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
    }

    public void process(SqsResponse sqsResponse) {
        assignResponseToBucket(sqsResponse);
        Bucket bucket = removeExpiredBucket();
        if (bucket != null) {
            csvFileWriter.write(bucket);
            createNextBucket();
        }
    }

    private Bucket getExpiredBucket() {
        Bucket expiredBucket = Iterables.getFirst(buckets, null);
        if(expiredBucket.isExpiredAtTime(clock.millis())) {
            return expiredBucket;
        } else {
            return null;
        }
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
        buckets.remove(expiredBucket);
        return expiredBucket;
    }

    private void createNextBucket() {
        Bucket lastBucket = Iterables.getLast(buckets);
        long nextBucketStartTime = lastBucket.getTimeRange().getMaximum() + 1;
        createBucket(nextBucketStartTime);
    }
}
