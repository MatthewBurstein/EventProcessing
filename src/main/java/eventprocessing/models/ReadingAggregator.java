package eventprocessing.models;

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
        Bucket bucket = getExpiredBucket();
        if (bucket != null) {
            csvFileWriter.write(bucket);
        }
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
}
