package eventprocessing.analysis;

import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;

public class Analyser {

    private static double reduceValues(double currentValue, double accumulator) {
        return accumulator + currentValue;
    }

    public double getAverageValue(ResponseList responseList) {
        double total = responseList.getResponses().stream()
                .map(response -> response.getMessage().getValue())
                .reduce(Double.valueOf(0), Analyser::reduceValues);
        return total / responseList.getResponses().size();
    }

}
