package eventprocessing.models;

import org.apache.commons.lang3.Range;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BucketManager {
    private List<ResponseList> buckets = new ArrayList<>();
    private BigInteger nextStartTime;

    public BucketManager(BigInteger initialTime) {
        nextStartTime = initialTime;

        while (buckets.size() < 5) {
            createBucket(nextStartTime);
        }
    }

    public void createBucket(BigInteger thisBucketStartTime) {
        ResponseList bucket = new ResponseList(thisBucketStartTime);
        buckets.add(bucket);
        nextStartTime = thisBucketStartTime.add(BigInteger.valueOf(60));
    }

    public List<ResponseList> getBuckets() {
        return buckets;
    }

    public BigInteger getNextStartTime() {
        return nextStartTime;
    }
}
