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
        List<Map<String, Object>> data = new ArrayList<>();

        try {
            data = aliExpressParser.parseFlashDeals(url, 100);
        } catch (IOException e) {
            System.out.println("Can't parse flash deals page");
        }

        try {
            List<String[]> strings = CSVHelper.toStrings(data);
            CSVHelper.exportData(strings, file);
            System.out.println("Writing to csv successful");
        } catch (IOException e) {
            System.out.println("Failed to write data to file");
        }

        // Checking
        try {
            List<String[]> strings = CSVHelper.importData(file);
            System.out.println("Data equals before and after reading: "
                    + data.toString().equals(CSVHelper.toMaps(strings).toString())); // work if file created in this run
        } catch (IOException | CsvException e) {
            System.out.println("Failed to read data from file");
        }


    }
}
