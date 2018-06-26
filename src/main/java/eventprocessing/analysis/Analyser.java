package eventprocessing.analysis;

import eventprocessing.models.Response;
import eventprocessing.models.ResponseList;

import java.util.Optional;

public class Analyser {

    private static double reduceValues(double currentValue, double accumulator) {
        return accumulator + currentValue;
    }

    public double getAverageValue(ResponseList responseList) {

        double total = responseList.getMessageValues()
                .stream()
                .reduce(0.0, Double::sum);

        return total / responseList.getResponses().size();
    }

}
