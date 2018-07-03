package eventprocessing.models;

import eventprocessing.fileservices.CSVFileWriter;

public class ReadingAggregator {
    private CSVFileWriter csvFileWriter;

    public ReadingAggregator(CSVFileWriter csvFileWriter) {
        this.csvFileWriter = csvFileWriter;
    }

    public void process(SqsResponse sqsResponse) {}
}
