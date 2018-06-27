import eventprocessing.models.BucketManager;
import eventprocessing.models.ResponseList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BucketManagerTest {

    private BucketManager bucketManager;

    @Before
    public void createObjects() {
        bucketManager = new BucketManager(BigInteger.valueOf(0));
    }

    @Test
    public void bucketManagerConstructor_createsFiveBuckets() {
        assertEquals(bucketManager.getBuckets().size(), 5);
    }

    @Test
    public void bucketManagerConstructor_setsNextStartTimeAppropriately() {
        BigInteger expected = BigInteger.valueOf(300);
        assertEquals(bucketManager.getNextStartTime(), expected);
    }

    @Test
    public void createBucket_createsASingleBucket() {
        bucketManager.createBucket(BigInteger.valueOf(300));
        assertEquals(bucketManager.getBuckets().size(), 6);
    }

    @Test
    public void createBuckets_updatesNextStartTime() {
        bucketManager.createBucket(BigInteger.valueOf(300));
        assertEquals(bucketManager.getNextStartTime(), BigInteger.valueOf(360));
    }


}
