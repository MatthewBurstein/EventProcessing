import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.models.Bucket;
import eventprocessing.models.BucketManager;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.SqsResponse;
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
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND);
        bucketManager.addResponseToBucket(mockSqsResponse);
        List<Bucket> bucket = bucketManager.getBuckets();

        assertThat(bucket.get(1).getSqsResponse()).containsOnly(mockSqsResponse);
        assertThat(bucket.get(0).getSqsResponse()).doesNotContain(mockSqsResponse);
        assertThat(bucket.get(2).getSqsResponse()).doesNotContain(mockSqsResponse);
        assertThat(bucket.get(3).getSqsResponse()).doesNotContain(mockSqsResponse);
        assertThat(bucket.get(4).getSqsResponse()).doesNotContain(mockSqsResponse);
    }

    @Test
    public void addMultipleResponsesToBucket_addsResponsesToCorrectBuckets() {
        SqsResponse mockSqsResponse1 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse1.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND);
        SqsResponse mockSqsResponse2 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse2.getMessageTimestamp()).thenReturn((long) 0);
        SqsResponse mockSqsResponse3 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse3.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND * 4);
        SqsResponse mockSqsResponse4 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse4.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND * 2);
        SqsResponse mockSqsResponse5 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse5.getMessageTimestamp()).thenReturn(GlobalConstants.BUCKET_UPPER_BOUND * 3);

        InitialBucket mockResponseList = Mockito.mock(InitialBucket.class);
        when(mockResponseList.getSqsResponse()).thenReturn(Lists.newArrayList(mockSqsResponse1, mockSqsResponse2,
                mockSqsResponse3, mockSqsResponse4, mockSqsResponse5));
        bucketManager.addMultipleResponsesToBucket(mockResponseList);
        List<Bucket> buckets = bucketManager.getBuckets();

        assertThat(buckets.get(0).getSqsResponse()).containsOnly(mockSqsResponse2);
        assertThat(buckets.get(1).getSqsResponse()).containsOnly(mockSqsResponse1);
        assertThat(buckets.get(2).getSqsResponse()).containsOnly(mockSqsResponse4);
        assertThat(buckets.get(3).getSqsResponse()).containsOnly(mockSqsResponse5);
        assertThat(buckets.get(4).getSqsResponse()).containsOnly(mockSqsResponse3);
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
