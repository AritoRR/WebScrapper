package scrapper.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scrapper.Data;
import scrapper.OutputWriter;
import java.net.http.HttpClient;
import java.util.List;

public class NekoApi implements Api {
    private static final Logger logger = LogManager.getLogger(NekoApi.class);
    private static final String SOURCE = "https://nekos.best/api/v2/neko";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public NekoApi() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public NekoApi(HttpClient client, ObjectMapper mapper) {
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

    @Override
    public void handleApiResponse(String response) {
        try {
            JsonNode json = mapper.readTree(response);
            String imageUrl = json.path("results").get(0).path("url").asText();
            OutputWriter.write(new Data("neko-api", List.of(imageUrl)));
        } catch (NullPointerException e) {
            logger.error(e);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response: ", e);
    }
}
}
