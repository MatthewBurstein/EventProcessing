import com.google.common.collect.Lists;
import eventprocessing.GlobalConstants;
import eventprocessing.models.Bucket;
import eventprocessing.models.BucketManager;
import eventprocessing.models.InitialBucket;
import eventprocessing.models.SqsResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BucketRemovalFeatureTest {
    
    private long startTime = System.currentTimeMillis();
    private SqsResponse mockSqsResponse1;
    private SqsResponse mockSqsResponse2;
    private SqsResponse mockSqsResponse3;
    private SqsResponse mockSqsResponse4;
    private SqsResponse mockSqsResponse5;
    private SqsResponse mockSqsResponse6;
    private SqsResponse mockSqsResponse7;
    private SqsResponse mockSqsResponse8;
    private SqsResponse mockSqsResponse9;
    private SqsResponse mockSqsResponse10;
    private SqsResponse mockSqsResponse11;
    private SqsResponse mockSqsResponse12;
    private SqsResponse mockSqsResponse13;
    private SqsResponse mockSqsResponse14;
    private SqsResponse mockSqsResponse15;
    private SqsResponse mockSqsResponse16;
    private SqsResponse mockSqsResponse17;
    private SqsResponse mockSqsResponse18;
    private SqsResponse mockSqsResponse19;
    private SqsResponse mockSqsResponse20;
    
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
        assertEquals(expectedBuckets, removedBuckets);
    }

    public void createResponses() {
        mockSqsResponse1 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse1.getMessageTimestamp()).thenReturn(startTime);
        mockSqsResponse2 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse2.getMessageTimestamp()).thenReturn(startTime + GlobalConstants.THIS_BUCKET_RANGE);
        mockSqsResponse3 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse3.getMessageTimestamp()).thenReturn(startTime + GlobalConstants.BUCKET_UPPER_BOUND);
        mockSqsResponse4 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse4.getMessageTimestamp()).thenReturn(startTime + (2 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse5 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse5.getMessageTimestamp()).thenReturn(startTime + (2 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse6 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse6.getMessageTimestamp()).thenReturn(startTime + (3 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse7 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse7.getMessageTimestamp()).thenReturn(startTime + (3 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse8 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse8.getMessageTimestamp()).thenReturn(startTime + (4 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse9 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse9.getMessageTimestamp()).thenReturn(startTime + (4 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse10 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse10.getMessageTimestamp()).thenReturn(startTime + (5 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse11 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse11.getMessageTimestamp()).thenReturn(startTime + (5 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse12 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse12.getMessageTimestamp()).thenReturn(startTime + (6 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse13 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse13.getMessageTimestamp()).thenReturn(startTime + (6 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse14 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse14.getMessageTimestamp()).thenReturn(startTime + (7 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse15 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse15.getMessageTimestamp()).thenReturn(startTime + (7 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse16 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse16.getMessageTimestamp()).thenReturn(startTime + (8 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse17 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse17.getMessageTimestamp()).thenReturn(startTime + (8 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse18 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse18.getMessageTimestamp()).thenReturn(startTime + (9 * GlobalConstants.THIS_BUCKET_RANGE));
        mockSqsResponse19 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse19.getMessageTimestamp()).thenReturn(startTime + (9 * GlobalConstants.BUCKET_UPPER_BOUND));
        mockSqsResponse20 = Mockito.mock(SqsResponse.class);
        when(mockSqsResponse20.getMessageTimestamp()).thenReturn(startTime + (10 * GlobalConstants.THIS_BUCKET_RANGE));
    }
    
    public void createInitialBucket() {
        initialBucket = new InitialBucket();
        initialBucket.getSqsResponses().addAll(Lists.newArrayList(mockSqsResponse1, mockSqsResponse2, mockSqsResponse3, mockSqsResponse4, mockSqsResponse5, mockSqsResponse6, mockSqsResponse7, mockSqsResponse8, mockSqsResponse9, mockSqsResponse10, mockSqsResponse11, mockSqsResponse12, mockSqsResponse13, mockSqsResponse14, mockSqsResponse15, mockSqsResponse16, mockSqsResponse17, mockSqsResponse18, mockSqsResponse19, mockSqsResponse20));

        earliestTimestamp = initialBucket.getEarliestTimestamp();
        expiryTime = earliestTimestamp
                + GlobalConstants.BUCKET_UPPER_BOUND
                + (GlobalConstants.MAX_MESSAGE_DELAY_MINS * GlobalConstants.BUCKET_UPPER_BOUND);
    }

    public void createBucketManager() {
        bucketManager = new BucketManager(earliestTimestamp);
    }
    
}
