import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.fileservices.CSVFileService;
import eventprocessing.models.Bucket;
import eventprocessing.models.BucketManager;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.SqsResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import java.io.IOException;
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
        SqsResponse mockSqsResponse = buildMockSqsResponse(GlobalConstants.BUCKET_UPPER_BOUND);
        bucketManager.addResponseToBucket(mockSqsResponse);
        List<Bucket> bucket = bucketManager.getBuckets();

        assertThat(bucket.get(1).getSqsResponses()).containsOnly(mockSqsResponse);
        assertThat(bucket.get(0).getSqsResponses()).doesNotContain(mockSqsResponse);
        assertThat(bucket.get(2).getSqsResponses()).doesNotContain(mockSqsResponse);
        assertThat(bucket.get(3).getSqsResponses()).doesNotContain(mockSqsResponse);
        assertThat(bucket.get(4).getSqsResponses()).doesNotContain(mockSqsResponse);
    }

    @Test
    public void addMultipleResponsesToBucket_addsResponsesToCorrectBuckets() {
        SqsResponse mockSqsResponse1 = buildMockSqsResponse(GlobalConstants.BUCKET_UPPER_BOUND);
        SqsResponse mockSqsResponse2 = buildMockSqsResponse(0);
        SqsResponse mockSqsResponse3 = buildMockSqsResponse(GlobalConstants.BUCKET_UPPER_BOUND * 4);
        SqsResponse mockSqsResponse4 = buildMockSqsResponse(GlobalConstants.BUCKET_UPPER_BOUND * 2);
        SqsResponse mockSqsResponse5 = buildMockSqsResponse(GlobalConstants.BUCKET_UPPER_BOUND * 3);

        InitialBucket mockInitialBucket = Mockito.mock(InitialBucket.class);
        when(mockInitialBucket.getSqsResponses()).thenReturn(Lists.newArrayList(mockSqsResponse1, mockSqsResponse2,
                mockSqsResponse3, mockSqsResponse4, mockSqsResponse5));
        bucketManager.addMultipleResponsesToBucket(mockInitialBucket);
        List<Bucket> buckets = bucketManager.getBuckets();

        assertThat(buckets.get(0).getSqsResponses()).containsOnly(mockSqsResponse2);
        assertThat(buckets.get(1).getSqsResponses()).containsOnly(mockSqsResponse1);
        assertThat(buckets.get(2).getSqsResponses()).containsOnly(mockSqsResponse4);
        assertThat(buckets.get(3).getSqsResponses()).containsOnly(mockSqsResponse5);
        assertThat(buckets.get(4).getSqsResponses()).containsOnly(mockSqsResponse3);
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
    public void isDuplicateMessage_returnsTrueForDuplicateMessage() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        Bucket mockBucket1 = Mockito.mock(Bucket.class);
        when(mockBucket1.isDuplicateMessage(mockSqsResponse)).thenReturn(true);
        bucketManager.getBuckets().add(mockBucket1);
        assertTrue(bucketManager.isDuplicateMessage(mockSqsResponse));
    }

    @Test
    public void isDuplicateMessage_returnsFalseForNonDuplicateMessage() {
        SqsResponse mockSqsResponse = Mockito.mock(SqsResponse.class);
        Bucket mockBucket1 = Mockito.mock(Bucket.class);
        when(mockBucket1.isDuplicateMessage(mockSqsResponse)).thenReturn(false);
        bucketManager.getBuckets().add(mockBucket1);
        assertFalse(bucketManager.isDuplicateMessage(mockSqsResponse));
    }


    private SqsResponse buildMockSqsResponse(long timeStamp) {
        SqsResponse mockSqsResponse1 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse1.getMessageTimestamp()).thenReturn(timeStamp);
        return mockSqsResponse1;
    }
}