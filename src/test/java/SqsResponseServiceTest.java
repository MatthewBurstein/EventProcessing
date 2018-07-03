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
//        String validResponse = "{\"Type\" : \"Notification\", \"MessageId\" : \"messageId\", \"TopicArn\" : \"topicARN\", \"Message\" : \"{\"locationId\":\"93f1c429-c2fe-4f4a-8b62-6ea990f60b76\",\"eventId\":\"1730d98e-9087-4814-a6b9-f1c3c97cf73e\",\"value\":3.1656330341535552,\"timestamp\":1530531019281}\", \"Timestamp\" : \"2018-07-02T11:30:19.301Z\", \"SignatureVersion\" : \"1\", \"Signature\" : \"sig\", \"SigningCertURL\" : \"sign_url\", \"UnsubscribeURL\" : \"unsub_url\"}";
        String validResponse = "{\"Type\" : \"Notification\",\"MessageId\" : \"b9352c2e-2df9-52df-99c0-7f9b420b6c00\",\"TopicArn\" : \"arn:aws:sns:eu-west-1:552908040772:EventProcessing-SuzanneJune2018-snsTopicSensorDataPart2-15OODW444U5LO\",\"Message\" : \"{\"locationId\":\"04c8c045-edb8-4044-b942-21b92ff780c5\",\"eventId\":\"1c826854-1397-4dc3-adb1-0225a9ff69e5\",\"value\":3.5274867458968866,\"timestamp\":1530621362110}\",\"Timestamp\" : \"2018-07-03T12:36:02.100Z\",\"SignatureVersion\" : \"1\",\"Signature\" : \"YDugXgcEwwSXwiqWLOZppfTyAsh5SoFcuV6kFejqUkMvZ1fZ/EIS0x5hO2yxUGZkC9i1Qt8jvJ4oZ9+Tf5ybxLhjY0VjZA8ULaMU+eRtTWIk02ZO0NiypSeXwxwyH1y3umdaPbnWMMcWVqbeeYS8oy/zHS2pwwhvguoaG4OiF4j2KvSHKsiklEbABJ+pAE4We10q6mX5YFz/EeS8JtkM/N+hU9GIDyD9LU6vIWiMH4Q5TU/eq5EZM/Davzkkg/9ziWDW29AuNlNRpOHB417l66IevdRh3+jvscdpW6m27cgBXmR2kgbHE1EakB/ShDrdEbqTde5Nwoq9Yrbo3btYJA==\",\"SigningCertURL\" : \"https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-eaea6120e66ea12e88dcd8bcbddca752.pem\",\"UnsubscribeURL\" : \"https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:552908040772:EventProcessing-SuzanneJune2018-snsTopicSensorDataPart2-15OODW444U5LO:ca64c382-5163-4192-a475-b19e2b3ce919\"}";
        SqsResponse sqsResponse = sqsResponseService.parseResponse(validResponse);
        assertEquals(sqsResponse.getMessageId(), "messageId");
    }
}
