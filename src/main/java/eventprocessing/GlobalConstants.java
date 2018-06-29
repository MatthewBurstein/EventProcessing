package eventprocessing;

public class GlobalConstants {
    public static int MULTIPLES_OF_MESSAGES = 10; //default: 10
    public static long THIS_BUCKET_RANGE = (60 * 1000) - 1; //default: (60 * 1000) - 1
    public static long BUCKET_UPPER_BOUND = 60 * 1000; //default: 60
    public static int MAX_MESSAGE_DELAY_MINS = 5; //DO NOT CHANGE
    public static long FIRST_LOOP_DURATION = MAX_MESSAGE_DELAY_MINS * BUCKET_UPPER_BOUND; //default: MAX_MESSAGE_DELAY_MINS * BUCKET_UPPER_BOUND
}
