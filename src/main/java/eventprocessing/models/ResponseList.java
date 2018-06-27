package eventprocessing.models;

import org.apache.commons.lang3.Range;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResponseList {

    private Range<BigInteger> timeRange;
    private List<Response> responses = new ArrayList<>();

    public ResponseList(BigInteger startTime) {
        BigInteger endTime = startTime.add(BigInteger.valueOf(59));
        this.timeRange = Range.between(startTime, endTime);
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
}
