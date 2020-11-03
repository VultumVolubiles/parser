import com.gargoylesoftware.htmlunit.WebClient;
import com.opencsv.exceptions.CsvException;
import ustils.CSVHelper;
import ustils.parsers.AliExpressParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {

    public static void main(String[] args) {
        String url = "https://flashdeals.aliexpress.com/en.htm?";
        File storage = new File("storage");
        File file = storage.toPath().resolve("test.csv").toFile();
        if (!storage.exists()) {
            storage.mkdirs();
        }

        AliExpressParser aliExpressParser = new AliExpressParser(new WebClient());
        List<Map<String, Object>> requestsData = new ArrayList<>();
        List<Map<String, Object>> parsingData = new ArrayList<>();

        try {
            requestsData = aliExpressParser.parseFlashDeals(url, 100);

            // Checking that the data received through the request is equal to the data received by parsing the page
            parsingData = aliExpressParser.parseFlashDeals(url);
            for (int i = 0; i < parsingData.size(); i++) {
                Map<String, Object> reqData = requestsData.get(i);
                Map<String, Object> parsData = parsingData.get(i);

                for (String key : parsData.keySet())
                    System.out.println(key + " equal: " + parsData.get(key).equals(reqData.get(key)));

                System.out.println("--------------------");
            }
        } catch (Exception e) {
            System.out.println("Can't parse flash deals page");
        }
        // Checking

        try {
            List<String[]> strings = CSVHelper.toStrings(requestsData);
            CSVHelper.exportData(strings, file);
            System.out.println("Writing to csv successful");
        } catch (IOException e) {
            System.out.println("Failed to write data to file");
        }

        // Checking
        try {
            List<String[]> strings = CSVHelper.importData(file);
            System.out.println("Data equals before and after reading: "
                    + requestsData.toString().equals(CSVHelper.toMaps(strings).toString())); // work if file created in this run
        } catch (IOException | CsvException e) {
            System.out.println("Failed to read data from file");
        }
    }
}
