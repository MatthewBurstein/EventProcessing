package eventprocessing.models;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Optional;

public class InitialResponseList extends ResponseList {

    public InitialResponseList() {
        super();
    }

    public BigInteger getEarliestTimestamp() {
        Optional<Response> earliestResponse = getResponses().stream()
                .min(Comparator.comparing(Response::getTimestamp));

        return earliestResponse.get().getTimestamp();
    }
}
