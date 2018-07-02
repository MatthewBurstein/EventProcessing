package eventprocessing.analysis;

import eventprocessing.models.Bucket;

public class Analyser {

    private static double reduceValues(double currentValue, double accumulator) {
        return accumulator + currentValue;
    }

    public double getAverageValue(Bucket bucket) {

        double total = bucket.getMessageValues()
                .stream()
                .reduce(0.0, Double::sum);

        return total / bucket.getSqsResponse().size();
    }
}
