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
    public static void exportData(List<String[]> data, File file) throws IOException {
        if (!file.exists())
            file.createNewFile();

        CSVWriter writer = new CSVWriter(new FileWriter(file, true));
        for (String[] s : data) {
            writer.writeNext(s);
        }
        writer.close();
    }

    /*
        Import data from *.csv file
     */
    public static List<String[]> importData(File file) throws IOException, CsvException {
        if (!file.exists())
            throw new FileNotFoundException("File not found");

        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> strings = reader.readAll();

        return strings;
    }

    public static List<String[]> toStrings(List<Map<String, Object>> maps) {
        List<String[]> strings = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            String[] record = new String[map.size()];
            Object[] keys = map.keySet().toArray();

            for (int i = 0; i < keys.length; i++)
                record[i] = keys[i].toString() + "=" + map.get(keys[i].toString());

            strings.add(record);
        }
        return strings;
    }

    public static List<Map<String, Object>> toMaps(List<String[]> strings) {
        List<Map<String, Object>> maps = new ArrayList<>();

        for (String[] str : strings) {
            Map<String, Object> map = new HashMap<>();

            for (String val: str) {
                String key = val.substring(0, val.indexOf("="));
                map.put(key, val.substring(val.indexOf("=")+1));
            }

            maps.add(map);
        }

        return maps;
    }


}
