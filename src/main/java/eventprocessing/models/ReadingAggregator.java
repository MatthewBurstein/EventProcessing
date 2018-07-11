package eventprocessing.models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.fileservices.CSVFileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class ReadingAggregator {
    private static final Logger logger = LogManager.getLogger("ReadingAggregator");
    private CSVFileService csvFileService;
    private Clock clock;
    private final List<Bucket> buckets = new ArrayList<>();
    private GlobalConstants gc;
    private SensorList sensorList;
    private int validMessageCount = 0;
    private int invalidSensorCount = 0;
    private int duplicateMessageCount = 0;

    public ReadingAggregator(CSVFileService csvFileService, Clock clock, GlobalConstants gc, SensorList sensorList) {
        this.csvFileService = csvFileService;
        this.clock = clock;
        this.gc = gc;
        this.sensorList = sensorList;
        createInitialBuckets();
    }

    public void process(Reading reading) {
        if (isValidReading(reading)) {
            assignResponseToBucket(reading);
            sensorList.storeSensorData(reading);
            validMessageCount++;
        }
        Bucket bucket = removeExpiredBucket();
        if (bucket != null) {
            csvFileService.write(bucket);
            createNextBucket();
        }
    }

    public void getTotalMessageCount() {
        logger.info("Total valid messages: " + validMessageCount);
        logger.info("Total duplicate messages: " + duplicateMessageCount);
        logger.info("Total faulty sensors: " + invalidSensorCount);
    }

    public void finalise() {
        processAllBuckets();
        processAllSensors();
    }

    private void processAllSensors() {
        sensorList.getSensors().forEach(sensor -> csvFileService.writeSensorData(sensor));
    }

    private void processAllBuckets() {
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

    private boolean isValidReading(Reading reading) {
        return isUniqueReading(reading) && isWorkingSensor(reading);
    }

    private boolean isUniqueReading(Reading reading) {
        if (getReadingIds().contains(reading.getId())) {
            return true;
        } else {
            duplicateMessageCount++;
            return false;
        }
    }

    private boolean isWorkingSensor(Reading reading) {
        if(sensorList.isWorkingSensor(reading)) {
            return true;
        } else {
            invalidSensorCount++;
            return false;
        }
    }
}
