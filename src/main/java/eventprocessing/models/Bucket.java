package eventprocessing.models;

import eventprocessing.GlobalConstants;
import eventprocessing.Main;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bucket {

    private Range<Long> timeRange;
    private List<SqsResponse> sqsResponses = new ArrayList<>();

    static Logger logger = LogManager.getLogger(Main.class);

    public Bucket(long startTime) {
        long endTime = startTime + GlobalConstants.THIS_BUCKET_RANGE;
        this.timeRange = Range.between(startTime, endTime);
    }

    public Bucket() {
        //Constructor for InitialBucket
    }

    public void addResponse(SqsResponse sqsResponse) {
        if (!isDuplicateMessage(sqsResponse)) {
            this.sqsResponses.add(sqsResponse);
        }
    }

    public List<SqsResponse> getSqsResponses() {
        return sqsResponses;
    }

    public List<Double> getMessageValues() {
        return sqsResponses
                .stream()
                .map(response -> response.getValue())
                .collect(Collectors.toList());
    }

    public List<String> getMessageIds() {
        return sqsResponses.stream()
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

    public double getAverageValue() {
        double total = getMessageValues()
                .stream()
                .reduce(0.0, Double::sum);

        return total / getSqsResponses().size();
    }

    public boolean isDuplicateMessage(SqsResponse sqsResponse) {
        return getMessageIds().contains(sqsResponse.getMessageId());
    }
}
