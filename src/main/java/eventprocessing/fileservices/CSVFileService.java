package eventprocessing.fileservices;

import eventprocessing.analysis.Analyser;
import eventprocessing.models.Bucket;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CSVFileService {
    private final String outputCsvFile;
    private final Analyser analyser;

    public CSVFileService(String outputCsvFile) {
        this.outputCsvFile = outputCsvFile;
        this.analyser = new Analyser();
    }

    public void writeBucketDataToFile(Bucket bucketToWriteToFile) throws IOException {
        CSVFormat csvFormat = getCsvFormat();
        StringBuffer stringBuffer = new StringBuffer(1000);
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuffer, csvFormat)) {
            writeBucketToStream(csvPrinter, bucketToWriteToFile);
            writeToFile(stringBuffer);
        }
    }

    public void writeMultipleBucketDataToFile(List<Bucket> bucketToWriteToFile) throws IOException {
        CSVFormat csvFormat = getCsvFormat();
        StringBuffer stringBuffer = new StringBuffer(1000);
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuffer, csvFormat)) {
            bucketToWriteToFile.forEach(bucket -> writeBucketToStream(csvPrinter, bucket));
            writeToFile(stringBuffer);
        }
    }

    private void writeBucketToStream(CSVPrinter csvPrinter, Bucket bucket) {
        String startTime = bucket.getTimeRange().getMinimum().toString();
        String endTime = bucket.getTimeRange().getMaximum().toString();
        String numberOfResponses = String.valueOf(bucket.getResponses().size());
        String averageValue = String.valueOf(analyser.getAverageValue(bucket));
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
