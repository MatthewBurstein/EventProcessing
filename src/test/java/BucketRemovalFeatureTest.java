import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.models.Bucket;
import eventprocessing.models.BucketManager;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BucketRemovalFeatureTest {
    
    private long startTime = System.currentTimeMillis();
    private Response mockResponse1;
    private Response mockResponse2;
    private Response mockResponse3;
    private Response mockResponse4;
    private Response mockResponse5;
    private Response mockResponse6;
    private Response mockResponse7;
    private Response mockResponse8;
    private Response mockResponse9;
    private Response mockResponse10;
    private Response mockResponse11;
    private Response mockResponse12;
    private Response mockResponse13;
    private Response mockResponse14;
    private Response mockResponse15;
    private Response mockResponse16;
    private Response mockResponse17;
    private Response mockResponse18;
    private Response mockResponse19;
    private Response mockResponse20;
    
    private InitialBucket initialBucket;
    private BucketManager bucketManager;
    private long earliestTimestamp;
    private long expiryTime;

    @Before
    public void setUp() {
        createResponses();
        createInitialBucket();
        createBucketManager();
        bucketManager.addMultipleResponsesToBucket(initialBucket);
    }

    @Test
    public void removeBucket_removesBucketWhenBucketIsExpired() {
        Bucket expectedBucket = bucketManager.getBuckets().get(0);

        Bucket removedBucket = bucketManager.removeExpiredBucket(expiryTime);
        System.out.println("Removed bucket " + removedBucket);
        assertEquals(expectedBucket, removedBucket);
    }

    @Test
    public void removeMultipleExpiredBuckets_returnsListOfExpiredBuckets() {
        System.out.println(bucketManager.getBuckets());
        expiryTime = earliestTimestamp
                + GlobalConstants.BUCKET_UPPER_BOUND
                + GlobalConstants.BUCKET_UPPER_BOUND
                + GlobalConstants.BUCKET_UPPER_BOUND
                + (GlobalConstants.MAX_MESSAGE_DELAY_MINS * GlobalConstants.BUCKET_UPPER_BOUND);
        List<Bucket> expectedBuckets = Lists.newArrayList(bucketManager.getBuckets().get(0),
                bucketManager.getBuckets().get(1),
                bucketManager.getBuckets().get(2));

        List<Bucket> removedBuckets = bucketManager.removeMultipleExpiredBuckets(expiryTime);
        System.out.println("Removed bucket " + removedBuckets);
        assertEquals(expectedBuckets, removedBuckets);
    }

    public void createResponses() {
        mockResponse1 = Mockito.mock(Response.class);
        when(mockResponse1.getMessageTimestamp()).thenReturn(startTime);
        mockResponse2 = Mockito.mock(Response.class);
        when(mockResponse2.getMessageTimestamp()).thenReturn(startTime + GlobalConstants.THIS_BUCKET_RANGE);
        mockResponse3 = Mockito.mock(Response.class);
        when(mockResponse3.getMessageTimestamp()).thenReturn(startTime + GlobalConstants.BUCKET_UPPER_BOUND);
        mockResponse4 = Mockito.mock(Response.class);
        when(mockResponse4.getMessageTimestamp()).thenReturn(startTime + (2 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse5 = Mockito.mock(Response.class);
        when(mockResponse5.getMessageTimestamp()).thenReturn(startTime + (2 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse6 = Mockito.mock(Response.class);
        when(mockResponse6.getMessageTimestamp()).thenReturn(startTime + (3 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse7 = Mockito.mock(Response.class);
        when(mockResponse7.getMessageTimestamp()).thenReturn(startTime + (3 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse8 = Mockito.mock(Response.class);
        when(mockResponse8.getMessageTimestamp()).thenReturn(startTime + (4 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse9 = Mockito.mock(Response.class);
        when(mockResponse9.getMessageTimestamp()).thenReturn(startTime + (4 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse10 = Mockito.mock(Response.class);
        when(mockResponse10.getMessageTimestamp()).thenReturn(startTime + (5 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse11 = Mockito.mock(Response.class);
        when(mockResponse11.getMessageTimestamp()).thenReturn(startTime + (5 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse12 = Mockito.mock(Response.class);
        when(mockResponse12.getMessageTimestamp()).thenReturn(startTime + (6 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse13 = Mockito.mock(Response.class);
        when(mockResponse13.getMessageTimestamp()).thenReturn(startTime + (6 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse14 = Mockito.mock(Response.class);
        when(mockResponse14.getMessageTimestamp()).thenReturn(startTime + (7 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse15 = Mockito.mock(Response.class);
        when(mockResponse15.getMessageTimestamp()).thenReturn(startTime + (7 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse16 = Mockito.mock(Response.class);
        when(mockResponse16.getMessageTimestamp()).thenReturn(startTime + (8 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse17 = Mockito.mock(Response.class);
        when(mockResponse17.getMessageTimestamp()).thenReturn(startTime + (8 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse18 = Mockito.mock(Response.class);
        when(mockResponse18.getMessageTimestamp()).thenReturn(startTime + (9 * GlobalConstants.THIS_BUCKET_RANGE));
        mockResponse19 = Mockito.mock(Response.class);
        when(mockResponse19.getMessageTimestamp()).thenReturn(startTime + (9 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockResponse20 = Mockito.mock(Response.class);
        when(mockResponse20.getMessageTimestamp()).thenReturn(startTime + (10 * GlobalConstants.THIS_BUCKET_RANGE));
    }
    
    public void createInitialBucket() {
        initialBucket = new InitialBucket();
        initialBucket.getResponses().addAll(Lists.newArrayList(mockResponse1, mockResponse2, mockResponse3, mockResponse4, mockResponse5, mockResponse6, mockResponse7, mockResponse8, mockResponse9, mockResponse10, mockResponse11, mockResponse12, mockResponse13, mockResponse14, mockResponse15, mockResponse16, mockResponse17, mockResponse18, mockResponse19, mockResponse20));

        earliestTimestamp = initialBucket.getEarliestTimestamp();
        expiryTime = earliestTimestamp
                + GlobalConstants.BUCKET_UPPER_BOUND
                + (GlobalConstants.MAX_MESSAGE_DELAY_MINS * GlobalConstants.BUCKET_UPPER_BOUND);
    }

    public void createBucketManager() {
        bucketManager = new BucketManager(earliestTimestamp);
    }
    
}
