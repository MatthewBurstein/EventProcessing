package eventprocessing.models;

import eventprocessing.GlobalConstants;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bucket {

    private Range<Long> timeRange;
    private List<SqsResponse> sqsResponse = new ArrayList<>();

    public Bucket(long startTime) {
        long endTime = startTime + GlobalConstants.THIS_BUCKET_RANGE;
        this.timeRange = Range.between(startTime, endTime);
    }

    public Bucket() {
        //Constructor for InitialBucket
    }

    public void addResponse(SqsResponse sqsResponse) {
        this.sqsResponse.add(sqsResponse);
    }

    public List<SqsResponse> getSqsResponse() {
        return sqsResponse;
    }

    public List<Double> getMessageValues() {
        return sqsResponse
                .stream()
                .map(response -> response.getValue())
                .collect(Collectors.toList());
    }

    public List<String> getMessageIds() {
        return sqsResponse.stream()
                .map(response -> response.getMessageId())
                .collect(Collectors.toList());
    }

    public Range<Long> getTimeRange() {
        return timeRange;
    }

    public boolean isExpiredAtTime(long time) {

        long maxMsgOffset = GlobalConstants.BUCKET_UPPER_BOUND * GlobalConstants.MAX_MESSAGE_DELAY_MINS;
        long expirationTime = timeRange.getMaximum() + maxMsgOffset;
        return time >= expirationTime;
    }

}
