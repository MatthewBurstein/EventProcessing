package eventprocessing;

public class GlobalConstants {
    public long THIS_BUCKET_RANGE;
    public long BUCKET_UPPER_BOUND;
    public int MAX_MESSAGE_DELAY_MINS;
    public long MAX_MESSAGE_DELAY_MILLIS;

    public GlobalConstants(int sampleDurationSeconds, int maxMessageDelayMinutes) {
        this.BUCKET_UPPER_BOUND = sampleDurationSeconds * 1000; //production value: 60
        this.THIS_BUCKET_RANGE = BUCKET_UPPER_BOUND - 1; //production value: (60 * 1000) - 1
        this.MAX_MESSAGE_DELAY_MINS = maxMessageDelayMinutes; //DO NOT CHANGE -> maximum possible delay between sensor taking reading and message being received by this app. Not controlled by devs at this end.
        this.MAX_MESSAGE_DELAY_MILLIS = MAX_MESSAGE_DELAY_MINS * BUCKET_UPPER_BOUND; //production value: MAX_MESSAGE_DELAY_MINS * BUCKET_UPPER_BOUND


    }
}
