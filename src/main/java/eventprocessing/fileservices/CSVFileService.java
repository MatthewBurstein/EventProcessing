package eventprocessing.fileservices;

import eventprocessing.Main;
import eventprocessing.models.Bucket;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CSVFileService {
    private final String outputCsvFile;
    private final int ESTIMATED_LINE_LENGTH = 70;

    static Logger logger = LogManager.getLogger(Main.class);

    public CSVFileService(String outputCsvFile) {
        this.outputCsvFile = outputCsvFile;
    }

    public void write(Bucket bucketToWriteToFile) {
        logger.info("Writing bucket with TimeRange " + bucketToWriteToFile.getTimeRange() + "and " + bucketToWriteToFile.getSqsResponses().size() + " responses.");
        CSVFormat csvFormat = getCsvFormat();
        StringBuffer stringBuffer = new StringBuffer(ESTIMATED_LINE_LENGTH * 2);
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuffer, csvFormat)) {
            writeBucketToStream(csvPrinter, bucketToWriteToFile);
            writeToFile(stringBuffer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeMultipleBucketDataToFile(List<Bucket> bucketsToWriteToFile) throws IOException {
        CSVFormat csvFormat = getCsvFormat();
        int estimatedCharacterCount = ESTIMATED_LINE_LENGTH * (bucketsToWriteToFile.size() + 1);
        StringBuffer stringBuffer = new StringBuffer(estimatedCharacterCount);
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuffer, csvFormat)) {
            bucketsToWriteToFile.forEach(bucket -> writeBucketToStream(csvPrinter, bucket));
            writeToFile(stringBuffer);
        }
    }

    public void deleteOutputFile() throws IOException {
        Files.deleteIfExists(Paths.get(outputCsvFile));
    }

    private void writeBucketToStream(CSVPrinter csvPrinter, Bucket bucket) {
        String startTime = bucket.getTimeRange().getMinimum().toString();
        String endTime = bucket.getTimeRange().getMaximum().toString();
        String numberOfResponses = String.valueOf(bucket.getSqsResponses().size());
        String averageValue = String.valueOf(bucket.getAverageValue());
        try {
            csvPrinter.printRecord(startTime, endTime, numberOfResponses, averageValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(StringBuffer stringBuffer) throws IOException {
        int charCount = stringBuffer.length();
        char[] dst =  new char[charCount];
        stringBuffer.getChars(0, charCount, dst, 0);

        FileWriter fileWriter = new FileWriter(outputCsvFile, true);
        fileWriter.append(new String(dst));
        fileWriter.flush();
    }

    private CSVFormat getCsvFormat() {
        CSVFormat csvFormat;
        if (!fileExists()) {
            csvFormat = CSVFormat.DEFAULT.withHeader("Start Time", "End Time", "Number of Responses", "Average Value");
        } else {
            csvFormat = CSVFormat.DEFAULT;
        }
        return csvFormat;
    }

    private boolean fileExists() {
        return Files.exists(Paths.get(outputCsvFile));
    }
}
