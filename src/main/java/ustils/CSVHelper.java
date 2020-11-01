package ustils;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

/*
    Class for working with csv files
 */
public class CSVHelper {

    /*
        Export data to *.csv file
     */
    public static void exportData(List<String[]> data, String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists())
            file.createNewFile();

        CSVWriter writer = new CSVWriter(new FileWriter(filename));
        writer.writeAll(data);
        writer.close();
    }

    /*
        Import data from *.csv file
     */
    public static List<String[]> importData(String filename) throws IOException, CsvException {
        File file = new File(filename);
        if (!file.exists())
            throw new FileNotFoundException("File not found");

        CSVReader reader = new CSVReader(new FileReader(filename));
        List<String[]> strings = reader.readAll();

        return strings;
    }

    public static List<String[]> toStrings(List<Map<String, Object>> maps) {
        List<String[]> strings = new ArrayList<>();
        for (Map<String, Object> map : maps)
            strings.add(new String[] {map.toString()});

        return strings;
    }

    public static List<Map<String, Object>> toMaps(List<String[]> strings) {
        List<Map<String, Object>> maps = new ArrayList<>();

        for (String[] str : strings) {
            Map<String, Object> map = new HashMap<>();

            if (str.length > 0) {
                String[] values = StringUtils.substringBetween(str[0], "{", "}").split(", ");
                for (String val: values) {
                    String key = val.substring(0, val.indexOf("="));
                    map.put(key, val.substring(val.indexOf("=")+1));
                }
            }

            maps.add(map);
        }

        return maps;
    }


}
