package eventprocessing.fileservices;

import eventprocessing.models.Bucket;
import eventprocessing.models.Sensor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CSVFileService {
    private final String bucketCsvFile;
    private final String sensorCsvFile;
    private final int ESTIMATED_LINE_LENGTH = 70;

    private static final Logger logger = LogManager.getLogger("S3Client");

    public CSVFileService(String bucketCsvFile, String sensorCsvFile) {
        this.bucketCsvFile = bucketCsvFile;
        this.sensorCsvFile = sensorCsvFile;
    }

    public void write(Bucket bucket) {
        logger.info("Writing bucket with TimeRange " + bucket.getTimeRange() + "and " + bucket.getReadings().size() + " responses.");
        CSVFormat csvFormat = getBucketCsvFormat();
        StringBuffer stringBuffer = new StringBuffer(ESTIMATED_LINE_LENGTH * 2);
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuffer, csvFormat)) {
            writeBucketToStream(csvPrinter, bucket);
            writeToFile(stringBuffer, bucketCsvFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void writeSensorData(Sensor sensor) {
        logger.info("Writing sensor data for location ID " + sensor.getLocationId());
        CSVFormat csvFormat = getSensorCsvFormat();
        StringBuffer stringBuffer = new StringBuffer(ESTIMATED_LINE_LENGTH * 2);
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuffer, csvFormat)) {
            writeSensorToStream(csvPrinter, sensor);
            writeToFile(stringBuffer, sensorCsvFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    private void writeBucketToStream(CSVPrinter csvPrinter, Bucket bucket) {
        String startTime = bucket.getTimeRange().getMinimum().toString();
        String endTime = bucket.getTimeRange().getMaximum().toString();
        String numberOfResponses = String.valueOf(bucket.getReadings().size());
        String averageValue = Double.isNaN(bucket.getAverageValue()) ?
                "No Readings In TimeRange" :
                String.valueOf(bucket.getAverageValue());
        try {
            csvPrinter.printRecord(startTime, endTime, numberOfResponses, averageValue);
        } catch (IOException e) {
            logger.error(e.getStackTrace());
        }
    }

    private void writeSensorToStream(CSVPrinter csvPrinter, Sensor sensor) {
        String locationId = sensor.getLocationId();
        String numberOfResponses = String.valueOf(sensor.getNumberOfReadings());
        String averageValue = Double.isNaN(sensor.getAverageValue()) ?
                "No Values in Sensor" :
                String.valueOf(sensor.getAverageValue());
        try {
            csvPrinter.printRecord(locationId, numberOfResponses, averageValue);
        } catch (IOException e) {
            logger.error(e.getStackTrace());
        }
    }

    private void writeToFile(StringBuffer stringBuffer, String fileToWriteTo) throws IOException {
        int charCount = stringBuffer.length();
        char[] dst =  new char[charCount];
        stringBuffer.getChars(0, charCount, dst, 0);

        FileWriter fileWriter = new FileWriter(fileToWriteTo, true);
        fileWriter.append(new String(dst));
        fileWriter.flush();
    }

    private CSVFormat getBucketCsvFormat() {
        CSVFormat csvFormat;
        if (!fileExists(bucketCsvFile)) {
            csvFormat = CSVFormat.DEFAULT.withHeader("Start Time", "End Time", "Number of Responses", "Average Value");
        } else {
            csvFormat = CSVFormat.DEFAULT;
        }
        return csvFormat;
    }

    private CSVFormat getSensorCsvFormat() {
        CSVFormat csvFormat;
        if (!fileExists(sensorCsvFile)) {
            csvFormat = CSVFormat.DEFAULT.withHeader("Location ID", "Number of Responses", "Average Value");
        } else {
            csvFormat = CSVFormat.DEFAULT;
        }
        return csvFormat;
    }

    private boolean fileExists(String fileName) {
        return Files.exists(Paths.get(fileName));
    }
}
