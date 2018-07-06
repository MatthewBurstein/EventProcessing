package eventprocessing.models;

import com.google.common.collect.Iterables;
import eventprocessing.GlobalConstants;
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
    private final List<Bucket> buckets = new ArrayList<>();
    private int duplicateCounter = 0;
    private GlobalConstants gc;

    static Logger logger = LogManager.getLogger(Main.class);

    public int getDuplicateCounter() {
        return duplicateCounter;
    }

    public ReadingAggregator(CSVFileService csvFileService, Clock clock, GlobalConstants gc) {
        this.csvFileService = csvFileService;
        this.clock = clock;
        this.gc = gc;
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
        if(expiredBucket.isExpiredAtTime(clock)) {
            return expiredBucket;
        } else {
            return null;
        }
    }

    private void createBucket(long startTime) {
        Bucket newBucket = new Bucket(startTime, gc);
        buckets.add(newBucket);
    }

    private void createInitialBuckets() {
        long currentTime = clock.millis();
        long thisBucketStartTime = currentTime - gc.MAX_MESSAGE_DELAY_MILLIS;
        while (thisBucketStartTime <= currentTime) {
            createBucket(thisBucketStartTime);
            thisBucketStartTime += gc.BUCKET_UPPER_BOUND;
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
