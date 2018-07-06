package eventprocessing.threads;

import eventprocessing.models.TemporarySqsResponseStorage;

public class SqsClientThread extends Thread {

    private final String name;
    private TemporarySqsResponseStorage temporarySqsResponseStorage;

    public SqsClientThread(String name, TemporarySqsResponseStorage temporarySqsResponseStorage) {
        this.name = name;
        this.temporarySqsResponseStorage = temporarySqsResponseStorage;
    }

    public void run() {
        System.out.println("name is " + name);
    }

}
