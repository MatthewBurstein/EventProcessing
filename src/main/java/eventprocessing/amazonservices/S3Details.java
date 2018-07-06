package eventprocessing.amazonservices;

class S3Details {
    static final String s3BucketLocation = "eventprocessing-suzannejune2018-locationss3bucket-1jlchplwcxr2n";
//    static final String s3Key = "locations.json";
    static final String s3Key = "locations-part2.json"; //High data flow s3Key
//    static final String arnTopic = "arn:aws:sns:eu-west-1:552908040772:EventProcessing-SuzanneJune2018-snsTopicSensorDataPart1-1GKYP5V52Q07X";
    static final String arnTopic = "arn:aws:sns:eu-west-1:552908040772:EventProcessing-SuzanneJune2018-snsTopicSensorDataPart2-15OODW444U5LO"; //High data flow ARN
    static final String awsRegion = "eu-west-1";
    static final String queueName = "EventProcessing" + String.valueOf(System.currentTimeMillis());
}
