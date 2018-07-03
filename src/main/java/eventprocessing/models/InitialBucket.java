package eventprocessing.models;

import java.util.Comparator;
import java.util.Optional;

public class InitialBucket extends Bucket {

    public InitialBucket() {
        super();
    }

    public long getEarliestTimestamp() {
        Optional<SqsResponse> earliestResponse = getSqsResponses().stream()
                .min(Comparator.comparing(SqsResponse::getMessageTimestamp));

        return earliestResponse.get().getMessageTimestamp();
    }
}
