package eventprocessing.models;

import com.google.common.collect.Iterables;
import eventprocessing.Main;
import eventprocessing.fileservices.CSVFileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class ReadingAggregator {
    private CSVFileService csvFileService;
    private Clock clock;
    private List<Bucket> buckets;
    private int duplicateCounter = 0;

    static Logger logger = LogManager.getLogger(Main.class);

    public int getDuplicateCounter() {
        return duplicateCounter;
    }

    public ReadingAggregator(CSVFileService csvFileService, Clock clock) {
        this.csvFileService = csvFileService;
        this.clock = clock;
        this.buckets = new ArrayList<>();
        createInitialBuckets();
    }

    public void process(SqsResponse sqsResponse) {
        assignResponseToBucket(sqsResponse);
        Bucket bucket = removeExpiredBucket();
        if (bucket != null) {
            csvFileService.write(bucket);
            createNextBucket();
        }
    }

    public void processAllBuckets() {
        buckets.forEach(bucket -> {
            csvFileService.write(bucket);
        });
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
        Bucket newBucket = new Bucket(startTime);
        buckets.add(newBucket);
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
