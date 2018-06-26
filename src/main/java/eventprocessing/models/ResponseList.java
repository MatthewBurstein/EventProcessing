package eventprocessing.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResponseList {

    private List<Response> responses = new ArrayList<>();

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
