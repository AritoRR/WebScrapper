package scrapper.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scrapper.Data;
import scrapper.OutputWriter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class ColorApi implements Api {

    private static final Logger logger = LogManager.getLogger(ColorApi.class);
    private static final String SOURCE = "http://colormind.io/api/";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ColorApi() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public ColorApi(HttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public HttpClient getHttpClient() {
        return client;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    public String rgbToHex(int r, int g, int b) {
        return String.format("%02X%02X%02X",
                r,
                g,
                b
        ).toLowerCase();
    }

    public List<String> makeColor(int[][] colorEntries) {
        ArrayList<String> output = new ArrayList<>();
        for (int[] colorEntry : colorEntries) {
            output.add("https://www.colorhexa.com/" + rgbToHex(colorEntry[0], colorEntry[1], colorEntry[2]));
        }
        return output;
    }

    @Override
    public void handleApiResponse(String response) {
        try {
            JsonNode json = mapper.readTree(response).path("result");
            int[][] colorRgb = mapper.readValue(json.toString(), int[][].class);
            OutputWriter.write(new Data("colormind-api", makeColor(colorRgb)));
        } catch (NullPointerException |
                 IllegalArgumentException e) {
            logger.error(e);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response: ", e);
        }
    }

    @Override
    public void fetchData() {
        try {
            String requestBody = "{\"model\":\"default\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SOURCE))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            sendAsyncRequest(request);

        } catch (IllegalArgumentException |
                 CancellationException |
                 NullPointerException |
                 SecurityException e){
            logger.error(e);
        }

    }
}
