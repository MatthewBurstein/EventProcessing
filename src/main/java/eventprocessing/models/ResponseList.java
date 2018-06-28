package eventprocessing.models;

import org.apache.commons.lang3.Range;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResponseList {

    private Range<Long> timeRange;
    private List<Response> responses = new ArrayList<>();

    public ResponseList(long startTime) {
        long endTime = startTime + 59;
        this.timeRange = Range.between(startTime, endTime);
    }

    public ResponseList() {
    }

    public List<Response> getResponses() {
        return responses;
    }

    public List<Double> getMessageValues() {
        return responses
                .stream()
                .map(response -> response.getValue())
                .collect(Collectors.toList());
    }

    public void addResponse(Response response) {
        responses.add(response);
    }

    public Range<Long> getTimeRange() {
        return timeRange;
    }

    public boolean isExpiredAtTime(long time) {
        return !timeRange.contains(time);
    }

    public List<String> getMessageIds() {
        return responses.stream()
                .map(response -> response.getMessageId())
                .collect(Collectors.toList());
    }

}
