import com.google.common.collect.Lists;
import eventprocessing.models.BucketManager;
import eventprocessing.models.InitialResponseList;
import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BucketManagerTest {

    private BucketManager bucketManager;
    private StopWatch stopWatch;

    @Before
    public void createObjects() {
        stopWatch = Mockito.mock(StopWatch.class);
        bucketManager = new BucketManager(0, stopWatch);
    }

    @After
    public void clearBucketManager() {
        bucketManager.getBuckets().clear();
    }

    @Test
    public void bucketManagerConstructor_createsFiveBuckets() {
        assertEquals(bucketManager.getBuckets().size(), 5);
    }

    @Test
    public void bucketManagerConstructor_setsNextStartTimeAppropriately() {
        assertEquals(bucketManager.getNextStartTime(), 300);
    }

    @Test
    public void createBucket_createsASingleBucket() {
        bucketManager.createBucket(300);
        assertEquals(bucketManager.getBuckets().size(), 6);
    }

    @Test
    public void createBuckets_updatesNextStartTime() {
        bucketManager.createBucket(300);
        assertEquals(bucketManager.getNextStartTime(), 360);
    }

    @Test
    public void addResponseToBucket_addsResponseToCorrectBucket() {
        Response mockResponse = Mockito.mock(Response.class);
        when(mockResponse.getTimestamp()).thenReturn((long) 110);
        bucketManager.addResponseToBucket(mockResponse);
        List<ResponseList> responseList = bucketManager.getBuckets();

        assertThat(responseList.get(1).getResponses()).containsOnly(mockResponse);
        assertThat(responseList.get(0).getResponses()).doesNotContain(mockResponse);
        assertThat(responseList.get(2).getResponses()).doesNotContain(mockResponse);
        assertThat(responseList.get(3).getResponses()).doesNotContain(mockResponse);
        assertThat(responseList.get(4).getResponses()).doesNotContain(mockResponse);
    }

    @Test
    public void addMultipleResponsesToBucket_addsResponsesToCorrectBuckets() {
        Response mockResponse1 = Mockito.mock(Response.class);
        when(mockResponse1.getTimestamp()).thenReturn((long) 110);
        Response mockResponse2 = Mockito.mock(Response.class);
        when(mockResponse2.getTimestamp()).thenReturn((long) 10);
        Response mockResponse3 = Mockito.mock(Response.class);
        when(mockResponse3.getTimestamp()).thenReturn((long) 288);
        Response mockResponse4 = Mockito.mock(Response.class);
        when(mockResponse4.getTimestamp()).thenReturn((long) 120);
        Response mockResponse5 = Mockito.mock(Response.class);
        when(mockResponse5.getTimestamp()).thenReturn((long) 200);

        InitialResponseList mockResponseList = Mockito.mock(InitialResponseList.class);
        when(mockResponseList.getResponses()).thenReturn(Lists.newArrayList(mockResponse1, mockResponse2,
                mockResponse3, mockResponse4, mockResponse5));
        bucketManager.addMultipleResponsesToBucket(mockResponseList);
        List<ResponseList> buckets = bucketManager.getBuckets();

        assertThat(buckets.get(0).getResponses()).containsOnly(mockResponse2);
        assertThat(buckets.get(1).getResponses()).containsOnly(mockResponse1);
        assertThat(buckets.get(2).getResponses()).containsOnly(mockResponse4);
        assertThat(buckets.get(3).getResponses()).containsOnly(mockResponse5);
        assertThat(buckets.get(4).getResponses()).containsOnly(mockResponse3);
    }

    @Test
    public void remove_removesSpecifiedBucket() {
        ResponseList mockResponseList1 = Mockito.mock(ResponseList.class);
        ResponseList mockResponseList2 = Mockito.mock(ResponseList.class);
        bucketManager.getBuckets().addAll(Lists.newArrayList(mockResponseList1, mockResponseList2));
        bucketManager.remove(mockResponseList2);
        assertThat(bucketManager.getBuckets()).contains(mockResponseList1);
        assertThat(bucketManager.getBuckets()).doesNotContain(mockResponseList2);
    }

    @Test
    public void remove_returnsRemovedBucket() {
        ResponseList mockResponseList1 = Mockito.mock(ResponseList.class);
        ResponseList mockResponseList2 = Mockito.mock(ResponseList.class);
        bucketManager.getBuckets().addAll(Lists.newArrayList(mockResponseList1, mockResponseList2));
        ResponseList responseList = bucketManager.remove(mockResponseList2);
        assertEquals(responseList, mockResponseList2);
    }

    @Test
    public void removeExpiredBucket_removesBucketIfExpired() {
        ResponseList firstBucket = bucketManager.getBuckets().get(0);
        when(stopWatch.getTime()).thenReturn((long) 60);

        ResponseList removedBucket = bucketManager.removeExpiredBucket();

        assertThat(bucketManager.getBuckets()).doesNotContain((firstBucket));
    }

    @Test
    public void removeExpiredBucket_returnsRemovedBucket() {
        ResponseList firstBucket = bucketManager.getBuckets().get(0);
        when(stopWatch.getTime()).thenReturn((long) 60);

        ResponseList removedBucket = bucketManager.removeExpiredBucket();

        assertEquals(removedBucket, firstBucket);
    }


}
