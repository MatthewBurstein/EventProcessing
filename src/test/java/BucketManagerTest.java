//import com.google.common.collect.Lists;
//import eventprocessing.models.Bucket;
//import eventprocessing.models.BucketManager;
//import eventprocessing.models.InitialBucket;
//import eventprocessing.models.Response;
//import org.apache.commons.lang3.time.StopWatch;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import java.util.List;
//
//import static org.junit.Assert.*;
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.when;
//
//public class BucketManagerTest {
//
//    private BucketManager bucketManager;
//    private StopWatch stopWatch;
//
//    @Before
//    public void createObjects() {
//        stopWatch = Mockito.mock(StopWatch.class);
//        bucketManager = new BucketManager(0, stopWatch);
//    }
//
//    @After
//    public void clearBucketManager() {
//        bucketManager.getBuckets().clear();
//    }
//
//    @Test
//    public void bucketManagerConstructor_createsFiveBuckets() {
//        assertEquals(bucketManager.getBuckets().size(), 5);
//    }
//
//    @Test
//    public void bucketManagerConstructor_setsNextStartTimeAppropriately() {
//        assertEquals(bucketManager.getNextStartTime(), 300);
//    }
//
//    @Test
//    public void createBucket_createsASingleBucket() {
//        bucketManager.createBucket(300);
//        assertEquals(bucketManager.getBuckets().size(), 6);
//    }
//
//    @Test
//    public void createBuckets_updatesNextStartTime() {
//        bucketManager.createBucket(300);
//        assertEquals(bucketManager.getNextStartTime(), 360);
//    }
//
//    @Test
//    public void addResponseToBucket_addsResponseToCorrectBucket() {
//        Response mockResponse = Mockito.mock(Response.class);
//        when(mockResponse.getMessageTimestamp()).thenReturn((long) 110);
//        bucketManager.addResponseToBucket(mockResponse);
//        List<Bucket> bucket = bucketManager.getBuckets();
//
//        assertThat(bucket.get(1).getResponses()).containsOnly(mockResponse);
//        assertThat(bucket.get(0).getResponses()).doesNotContain(mockResponse);
//        assertThat(bucket.get(2).getResponses()).doesNotContain(mockResponse);
//        assertThat(bucket.get(3).getResponses()).doesNotContain(mockResponse);
//        assertThat(bucket.get(4).getResponses()).doesNotContain(mockResponse);
//    }
//
//    @Test
//    public void addMultipleResponsesToBucket_addsResponsesToCorrectBuckets() {
//        Response mockResponse1 = Mockito.mock(Response.class);
//        when(mockResponse1.getMessageTimestamp()).thenReturn((long) 110);
//        Response mockResponse2 = Mockito.mock(Response.class);
//        when(mockResponse2.getMessageTimestamp()).thenReturn((long) 10);
//        Response mockResponse3 = Mockito.mock(Response.class);
//        when(mockResponse3.getMessageTimestamp()).thenReturn((long) 288);
//        Response mockResponse4 = Mockito.mock(Response.class);
//        when(mockResponse4.getMessageTimestamp()).thenReturn((long) 120);
//        Response mockResponse5 = Mockito.mock(Response.class);
//        when(mockResponse5.getMessageTimestamp()).thenReturn((long) 200);
//
//        InitialBucket mockResponseList = Mockito.mock(InitialBucket.class);
//        when(mockResponseList.getResponses()).thenReturn(Lists.newArrayList(mockResponse1, mockResponse2,
//                mockResponse3, mockResponse4, mockResponse5));
//        bucketManager.addMultipleResponsesToBucket(mockResponseList);
//        List<Bucket> buckets = bucketManager.getBuckets();
//
//        assertThat(buckets.get(0).getResponses()).containsOnly(mockResponse2);
//        assertThat(buckets.get(1).getResponses()).containsOnly(mockResponse1);
//        assertThat(buckets.get(2).getResponses()).containsOnly(mockResponse4);
//        assertThat(buckets.get(3).getResponses()).containsOnly(mockResponse5);
//        assertThat(buckets.get(4).getResponses()).containsOnly(mockResponse3);
//    }
//
//    @Test
//    public void remove_removesSpecifiedBucket() {
//        Bucket mockBucket1 = Mockito.mock(Bucket.class);
//        Bucket mockBucket2 = Mockito.mock(Bucket.class);
//        bucketManager.getBuckets().addAll(Lists.newArrayList(mockBucket1, mockBucket2));
//        bucketManager.remove(mockBucket2);
//        assertThat(bucketManager.getBuckets()).contains(mockBucket1);
//        assertThat(bucketManager.getBuckets()).doesNotContain(mockBucket2);
//    }
//
//    @Test
//    public void remove_returnsRemovedBucket() {
//        Bucket mockBucket1 = Mockito.mock(Bucket.class);
//        Bucket mockBucket2 = Mockito.mock(Bucket.class);
//        bucketManager.getBuckets().addAll(Lists.newArrayList(mockBucket1, mockBucket2));
//        Bucket bucket = bucketManager.remove(mockBucket2);
//        assertEquals(bucket, mockBucket2);
//    }
//
//    @Test
//    public void removeExpiredBucket_removesBucketIfExpired() {
//        Bucket firstBucket = bucketManager.getBuckets().get(0);
//        when(stopWatch.getTime()).thenReturn((long) 360);
//        bucketManager.removeExpiredBucket();
//        assertThat(bucketManager.getBuckets()).doesNotContain((firstBucket));
//    }
//
//    @Test
//    public void removeExpiredBucket_returnsRemovedBucket() {
//        Bucket firstBucket = bucketManager.getBuckets().get(0);
//        when(stopWatch.getTime()).thenReturn((long) 360);
//        Bucket removedBucket = bucketManager.removeExpiredBucket();
//        assertEquals(removedBucket, firstBucket);
//    }
//
//    @Test
//    public void getMessageIds_returnsListOfMessageIds() {
//        Bucket mockBucket1 = Mockito.mock(Bucket.class);
//        when(mockBucket1.getMessageIds()).thenReturn(Lists.newArrayList("bucket1 id1", "bucket1 id2"));
//        Bucket mockBucket2 = Mockito.mock(Bucket.class);
//        when(mockBucket2.getMessageIds()).thenReturn(Lists.newArrayList("bucket2 id1"));
//        bucketManager.getBuckets().addAll(Lists.newArrayList(mockBucket1, mockBucket2));
//        List<String> expected = Lists.newArrayList("bucket1 id1", "bucket1 id2", "bucket2 id1");
//        assertEquals(bucketManager.getMessageIds(), expected);
//    }
//
//
//
//}
