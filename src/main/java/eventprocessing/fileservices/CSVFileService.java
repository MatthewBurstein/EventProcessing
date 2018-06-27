package eventprocessing.fileservices;

import eventprocessing.analysis.Analyser;
import eventprocessing.models.ResponseList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CSVFileService {
    private static final String outputCsvFile = "ResponseData.csv";

    public void writeToFile(ResponseList bucketToWriteToFile) throws IOException {
        Analyser analyser = new Analyser();

        try (
                BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputCsvFile));
                CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT
                .withHeader("Start Time", "End Time", "Number of Responses", "Average Value"));
                ) {
            String startTime = bucketToWriteToFile.getTimeRange().getMinimum().toString();
            String endTime = bucketToWriteToFile.getTimeRange().getMaximum().toString();
            String numberOfResponses = String.valueOf(bucketToWriteToFile.getResponses().size());
            String averageValue = String.valueOf(analyser.getAverageValue(bucketToWriteToFile));

            csvPrinter.printRecord(startTime, endTime, numberOfResponses, averageValue);

            csvPrinter.flush();
        }
    }
}
