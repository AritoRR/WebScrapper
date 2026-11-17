package scrapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.FileWriter;
import java.util.StringJoiner;


public class OutputWriter {
    private static final Logger logger = LogManager.getLogger(OutputWriter.class);

    private static String type;

    private static File fileJson = new File("output.json");
    private static File fileCsv = new File("output.csv");

    private OutputWriter() {}

    public static void setType(String argsType) {type = argsType;}

    public static void write(Data output) {
        if (type.equals("json")) jsonWrite(output);
        else csvWrite(output);
    }

    static synchronized void jsonWrite(Data output) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            List<Data> existingData = new ArrayList<>();
            if (fileJson.exists() && fileJson.length() > 0) {
                existingData = mapper.readValue(fileJson,
                        mapper.getTypeFactory().constructCollectionType(List.class, Data.class));
            }
            existingData.add(output);
            mapper.writeValue(fileJson, existingData);
            logger.info(output.getData());

        } catch (NullPointerException |
                 IOException e) {
            logger.error("Ошибка записи в JSON: ", e);
        }
    }
    static synchronized void csvWrite(Data output) {
        try (FileWriter writer = new FileWriter(fileCsv, true)) {
            if (fileCsv.length() == 0) {
                writer.write("API Name,Data\n");
            }
            StringJoiner csvLine = new StringJoiner(",");
            csvLine.add("\"" + output.getApiName() + "\"");
            String dataString = String.join(";", output.getData());
            csvLine.add("\"" + dataString + "\"");
            writer.write(csvLine + "\n");
            logger.info(output.getData());

        } catch (IOException e) {
            logger.error("Ошибка записи в CSV: ", e);
        }
    }


}
