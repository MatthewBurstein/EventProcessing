package eventprocessing.models;

import org.apache.commons.lang3.Range;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TimeManager {
    private List<Range<BigInteger>> ranges = new ArrayList<>();

    public TimeManager(BigInteger initialTime) {
        BigInteger currTime = initialTime;

        while (ranges.size() < 5) {
            createTimeRange(currTime);
            currTime = currTime.add(BigInteger.valueOf(60));
        }
    }

    public void createTimeRange(BigInteger startTime) {
        Range<BigInteger> range = Range.between(startTime, startTime.add(BigInteger.valueOf(59)));
        ranges.add(range);
    }
}
