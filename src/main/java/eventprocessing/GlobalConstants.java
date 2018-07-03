package eventprocessing;

public class GlobalConstants {
    public final static long THIS_BUCKET_RANGE = (60 * 1000) - 1; //production value: (60 * 1000) - 1
    public final static long BUCKET_UPPER_BOUND = 60 * 1000; //production value: 60
    public final static int MAX_MESSAGE_DELAY_MINS = 5; //DO NOT CHANGE -> maximum possible delay between sensor taking reading and message being recieved by this app. Not controlled by devs at this end.
    public final static long FIRST_LOOP_DURATION = MAX_MESSAGE_DELAY_MINS * BUCKET_UPPER_BOUND; //production value: MAX_MESSAGE_DELAY_MINS * BUCKET_UPPER_BOUND
}
