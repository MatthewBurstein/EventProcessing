package eventprocessing.models;

import com.google.common.collect.Iterables;
import eventprocessing.Main;
import eventprocessing.fileservices.CSVFileService;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadingAggregator {
    private CSVFileService csvFileService;
    private Clock clock;
    private List<Bucket> buckets;
    private int duplicateCounter = 0;
    private Map<String, MutableInt> duplicateLogger = new LinkedHashMap<>();

    public Map<String, MutableInt> getDuplicateLogger() {
        return duplicateLogger;
    }

    static Logger logger = LogManager.getLogger(Main.class);

    public int getDuplicateCounter() {
        return duplicateCounter;
    }

    public List<Bucket> getBuckets() {
        return buckets;
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
        logger.debug("New bucket: " + newBucket.getTimeRange());
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
