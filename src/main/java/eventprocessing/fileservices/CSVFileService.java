package eventprocessing.fileservices;

import eventprocessing.analysis.Analyser;
import eventprocessing.models.Bucket;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CSVFileService {
    private final String outputCsvFile;

    public CSVFileService(String outputCsvFile) {
        this.outputCsvFile = outputCsvFile;
    }

    public void writeBucketDataToFile(List<Bucket> bucketToWriteToFile) throws IOException {
        Analyser analyser = new Analyser();

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputCsvFile));
             CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT
                     .withHeader("Start Time", "End Time", "Number of Responses", "Average Value"));
        ) {
            bucketToWriteToFile.forEach(bucket -> {
                String startTime = bucket.getTimeRange().getMinimum().toString();
                String endTime = bucket.getTimeRange().getMaximum().toString();
                String numberOfResponses = String.valueOf(bucket.getResponses().size());
                String averageValue = String.valueOf(analyser.getAverageValue(bucket));

                try {
                    csvPrinter.printRecord(startTime, endTime, numberOfResponses, averageValue);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            csvPrinter.flush();
        }
    }
}
