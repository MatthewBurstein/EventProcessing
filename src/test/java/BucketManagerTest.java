import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.models.Bucket;
import eventprocessing.models.BucketManager;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

public class BucketManagerTest {

    private BucketManager bucketManager;

    @Before
    public void createObjects() {
        bucketManager = new BucketManager(0);
    }

    @After
    public void clearBucketManager() {
        bucketManager.getBuckets().clear();
    }

    @Test
    public void bucketManagerConstructor_createsFiveBuckets() {
        assertEquals(10, bucketManager.getBuckets().size());
    }

    @Test
    public void bucketManagerConstructor_setsNextStartTimeAppropriately() {
        assertEquals(GlobalConstants.BUCKET_UPPER_BOUND*10, bucketManager.getNextStartTime());
    }

    @Test
    public void createBucket_createsASingleBucket() {
        bucketManager.createBucket(0);
        assertEquals(11, bucketManager.getBuckets().size());
    }

    @Test
    public void createBuckets_updatesNextStartTime() {
        bucketManager.createBucket(GlobalConstants.BUCKET_UPPER_BOUND*10);
        assertEquals(GlobalConstants.BUCKET_UPPER_BOUND*11, bucketManager.getNextStartTime());
    }

    @Test
    public void addResponseToBucket_addsResponseToCorrectBucket() {
        Response mockResponse = Mockito.mock(Response.class);
        when(mockResponse.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND);
        bucketManager.addResponseToBucket(mockResponse);
        List<Bucket> bucket = bucketManager.getBuckets();

        assertThat(bucket.get(1).getResponses()).containsOnly(mockResponse);
        assertThat(bucket.get(0).getResponses()).doesNotContain(mockResponse);
        assertThat(bucket.get(2).getResponses()).doesNotContain(mockResponse);
        assertThat(bucket.get(3).getResponses()).doesNotContain(mockResponse);
        assertThat(bucket.get(4).getResponses()).doesNotContain(mockResponse);
    }

    @Test
    public void addMultipleResponsesToBucket_addsResponsesToCorrectBuckets() {
        Response mockResponse1 = Mockito.mock(Response.class);
        when(mockResponse1.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND);
        Response mockResponse2 = Mockito.mock(Response.class);
        when(mockResponse2.getMessageTimestamp()).thenReturn((long) 0);
        Response mockResponse3 = Mockito.mock(Response.class);
        when(mockResponse3.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND * 4);
        Response mockResponse4 = Mockito.mock(Response.class);
        when(mockResponse4.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND * 2);
        Response mockResponse5 = Mockito.mock(Response.class);
        when(mockResponse5.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND * 3);

        InitialBucket mockResponseList = Mockito.mock(InitialBucket.class);
        when(mockResponseList.getResponses()).thenReturn(Lists.newArrayList(mockResponse1, mockResponse2,
                mockResponse3, mockResponse4, mockResponse5));
        bucketManager.addMultipleResponsesToBucket(mockResponseList);
        List<Bucket> buckets = bucketManager.getBuckets();

        assertThat(buckets.get(0).getResponses()).containsOnly(mockResponse2);
        assertThat(buckets.get(1).getResponses()).containsOnly(mockResponse1);
        assertThat(buckets.get(2).getResponses()).containsOnly(mockResponse4);
        assertThat(buckets.get(3).getResponses()).containsOnly(mockResponse5);
        assertThat(buckets.get(4).getResponses()).containsOnly(mockResponse3);
    }

    @Test
    public void remove_removesSpecifiedBucket() {
        Bucket mockBucket1 = Mockito.mock(Bucket.class);
        Bucket mockBucket2 = Mockito.mock(Bucket.class);
        bucketManager.getBuckets().addAll(Lists.newArrayList(mockBucket1, mockBucket2));
        bucketManager.remove(mockBucket2);
        assertThat(bucketManager.getBuckets()).contains(mockBucket1);
        assertThat(bucketManager.getBuckets()).doesNotContain(mockBucket2);
    }

    @Test
    public void remove_returnsRemovedBucket() {
        Bucket mockBucket1 = Mockito.mock(Bucket.class);
        Bucket mockBucket2 = Mockito.mock(Bucket.class);
        bucketManager.getBuckets().addAll(Lists.newArrayList(mockBucket1, mockBucket2));
        Bucket bucket = bucketManager.remove(mockBucket2);
        assertEquals(bucket, mockBucket2);
    }

    @Test
    public void removeExpiredBucket_removesBucketIfExpired() {
        Bucket firstBucket = bucketManager.getBuckets().get(0);
        bucketManager.removeExpiredBucket(GlobalConstants.BUCKET_UPPER_BOUND * 6);
        assertThat(bucketManager.getBuckets()).doesNotContain((firstBucket));
    }

    @Test
    public void removeExpiredBucket_returnsRemovedBucket() {
        Bucket firstBucket = bucketManager.getBuckets().get(0);
        Bucket removedBucket = bucketManager.removeExpiredBucket(GlobalConstants.BUCKET_UPPER_BOUND * 6);
        assertEquals(removedBucket, firstBucket);
    }

    @Test
    public void getMessageIds_returnsListOfMessageIds() {
        Bucket mockBucket1 = Mockito.mock(Bucket.class);
        when(mockBucket1.getMessageIds()).thenReturn(Lists.newArrayList("bucket1 id1", "bucket1 id2"));
        Bucket mockBucket2 = Mockito.mock(Bucket.class);
        when(mockBucket2.getMessageIds()).thenReturn(Lists.newArrayList("bucket2 id1"));
        bucketManager.getBuckets().addAll(Lists.newArrayList(mockBucket1, mockBucket2));
        List<String> expected = Lists.newArrayList("bucket1 id1", "bucket1 id2", "bucket2 id1");
        assertEquals(bucketManager.getMessageIds(), expected);
    }



}
