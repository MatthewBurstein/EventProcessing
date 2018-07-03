package eventprocessing.models;

import eventprocessing.fileservices.CSVFileWriter;

import java.util.ArrayList;
import java.util.List;

public class ReadingAggregator {
    private CSVFileWriter csvFileWriter;
    private List<Bucket> buckets;

    public ReadingAggregator(CSVFileWriter csvFileWriter) {
        this.csvFileWriter = csvFileWriter;
        this.buckets = new ArrayList<>();
        createInitialBuckets();
    }

    public void process(SqsResponse sqsResponse) {
        putResponseInBucket(sqsResponse);
        Bucket removedBucket = removeExpiredBucket();
        if(removedBucket != null) {
            csvFileWriter.write(removedBucket);
        }
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    private void putResponseInBucket(SqsResponse sqsResponse) {
        buckets.forEach(bucket -> {
            if (bucket.getTimeRange().contains(sqsResponse.getResponseTimestamp())) {
                bucket.addResponse(sqsResponse);
            }
        });
    }

    private void createBucket(long startTime) {
        buckets.add(new Bucket(startTime));
    }

    private void createInitialBuckets() {
        long currentTime = System.currentTimeMillis();
        long thisBucketStartTime = currentTime - 30000;
        while(thisBucketStartTime < currentTime) {
            createBucket(thisBucketStartTime);
            thisBucketStartTime += 60000;
        }
    }

    private Bucket removeExpiredBucket() {
        long expiryTime = System.currentTimeMillis() - 300000;
        Bucket removedBucket = null;
        for(Bucket bucket : buckets) {
            if(bucket.getTimeRange().getMaximum() < expiryTime) {
                removedBucket = bucket;
            }
        }
        buckets.remove(removedBucket);
        return removedBucket;
    }
}
