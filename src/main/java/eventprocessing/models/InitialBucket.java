package eventprocessing.models;

import java.util.Comparator;
import java.util.Optional;

public class InitialBucket extends Bucket {

    public InitialBucket() {
        super();
    }

    public long getEarliestTimestamp() {
        Optional<Response> earliestResponse = getResponses().stream()
                .min(Comparator.comparing(Response::getTimestamp));

        return earliestResponse.get().getTimestamp();
    }
}
