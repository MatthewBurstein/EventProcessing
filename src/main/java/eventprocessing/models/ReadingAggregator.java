package eventprocessing.models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.fileservices.CSVFileService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class ReadingAggregator {
    private CSVFileService csvFileService;
    private Clock clock;
    private final List<Bucket> buckets = new ArrayList<>();
    private GlobalConstants gc;
    private SensorList sensorList;

    public ReadingAggregator(CSVFileService csvFileService, Clock clock, GlobalConstants gc, SensorList sensorList) {
        this.csvFileService = csvFileService;
        this.clock = clock;
        this.gc = gc;
        this.sensorList = sensorList;
        createInitialBuckets();
    }

    public void process(Reading reading) {
        if (!isDuplicateReading(reading)) {
            assignResponseToBucket(reading);
            sensorList.storeSensorData(reading);
        }
        Bucket bucket = removeExpiredBucket();
        if (bucket != null) {
            csvFileService.write(bucket);
            createNextBucket();
        }
    }

    public void finalise() {
        sensorList.getSensors().forEach(sensor -> csvFileService.writeSensorData(sensor));
    }

    public void processAllBuckets() {
        buckets.forEach(bucket -> csvFileService.write(bucket));
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

    private void assignResponseToBucket(Reading reading) {
        buckets.forEach(bucket -> {
            if (bucket.getTimeRange().contains(reading.getTimestamp())) {
                bucket.addResponse(reading);
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

    private List<String> getReadingIds() {
        List<String> ids = Lists.newArrayList();
        buckets.forEach(bucket -> {
            bucket.getReadingIds().forEach(id -> ids.add(id));
        });
        return ids;
    }

    private boolean isDuplicateReading(Reading reading) {
        return getReadingIds().contains(reading.getId());
    }
}
