import eventprocessing.customerrors.InvalidSqsResponseException;
import eventprocessing.models.SqsResponse;
import eventprocessing.responseservices.SqsResponseService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class SqsResponseServiceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void parseResponse_throwsInvalidSqsResponseExceptionWhenJsonIsMalformed() {
        SqsResponseService sqsResponseService = new SqsResponseService();
        String faultyResponse = "{\"Type\" : \"Notification\", \"MessageId\" : \"91cc8819-c1bb-5b0d-9e2f-dbadceb050c9\", \"TopicArn\" : \"topicARN\", \"Message\" : \"{\"locationId\":\"93f1c429-c2fe-4f4a-8b62-6ea990f60b76\",\"eventId\":\"1730d98e-9087-4814-a6b9-f1c3c97cf73e\",\"value\":3.1656330341535552,\"timestamp⾁:1530531019281}\", \"Timestamp\" : \"2018-07-02T11:30:19.301Z\", \"SignatureVersion\" : \"1\", \"Signature\" : \"sig\", \"SigningCertURL\" : \"sign_url\", \"UnsubscribeURL\" : \"unsub_url\"}";
        exception.expect(InvalidSqsResponseException.class);
        sqsResponseService.parseResponse(faultyResponse);
    }

    @Test
    public void parseResponse_returnsSqsResponseObjectWhenJsonIsValid() {
        SqsResponseService sqsResponseService = new SqsResponseService();
        String validResponse = "{\"Type\" : \"Notification\", \"MessageId\" : \"messageId\", \"TopicArn\" : \"topicARN\", \"Message\" : \"{\"locationId\":\"93f1c429-c2fe-4f4a-8b62-6ea990f60b76\",\"eventId\":\"1730d98e-9087-4814-a6b9-f1c3c97cf73e\",\"value\":3.1656330341535552,\"timestamp\":1530531019281}\", \"Timestamp\" : \"2018-07-02T11:30:19.301Z\", \"SignatureVersion\" : \"1\", \"Signature\" : \"sig\", \"SigningCertURL\" : \"sign_url\", \"UnsubscribeURL\" : \"unsub_url\"}";
        SqsResponse sqsResponse = sqsResponseService.parseResponse(validResponse);
        assertEquals(sqsResponse.getMessageId(), "messageId");
    }
}
