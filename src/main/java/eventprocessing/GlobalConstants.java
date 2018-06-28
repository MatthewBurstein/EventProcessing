package eventprocessing;

public class GlobalConstants {
    public static int MULTIPLES_OF_MESSAGES = 10;
    public static long THIS_BUCKET_RANGE = 59;
    public static long BUCKET_UPPER_BOUND = 60;
    public static int MAX_MESSAGE_DELAY_MINS = 5;
    public static long FIRST_LOOP_DURATION = MAX_MESSAGE_DELAY_MINS * 1000 * BUCKET_UPPER_BOUND;
}
