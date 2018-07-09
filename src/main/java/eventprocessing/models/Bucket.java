package eventprocessing.models;

import eventprocessing.GlobalConstants;
import org.apache.commons.lang3.Range;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bucket {

    private Range<Long> timeRange;
    private List<Reading> readings = new ArrayList<>();
    private GlobalConstants gc;

    public Bucket(long startTime, GlobalConstants gc) {
        this.gc = gc;
        long endTime = startTime + gc.THIS_BUCKET_RANGE;
        this.timeRange = Range.between(startTime, endTime);
    }

    public void addResponse(Reading reading) {
        if (!isDuplicateReading(reading)) {
            this.readings.add(reading);
        }
    }

    public List<Reading> getReadings() {
        return readings;
    }

    public List<String> getReadingIds() {
        return readings.stream()
                .map(Reading::getId)
                .collect(Collectors.toList());
    }

    public Range<Long> getTimeRange() {
        return timeRange;
    }

    public boolean isExpiredAtTime(Clock clock) {
        long maxMsgOffset = gc.BUCKET_UPPER_BOUND * gc.MAX_MESSAGE_DELAY_MINS;
        long expirationTime = timeRange.getMaximum() + maxMsgOffset;
        return clock.millis() >= expirationTime;
    }

    public double getAverageValue() {
        double total = getReadingValues()
                .stream()
                .reduce(0.0, Double::sum);

        return total / getReadings().size();
    }

    public boolean isDuplicateReading(Reading reading) {
        return getReadingIds().contains(reading.getId());
    }

    private List<Double> getReadingValues() {
        return readings
                .stream()
                .map(Reading::getValue)
                .collect(Collectors.toList());
    }
}
