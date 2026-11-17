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


public class DogApi implements Api {
    private static final Logger logger = LogManager.getLogger(DogApi.class);
    private static final String SOURCE = "https://dog.ceo/api/breeds/image/random";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public DogApi() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public DogApi(HttpClient client, ObjectMapper mapper) {
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
            String imageUrl = json.path("message").asText();
            OutputWriter.write(new Data("dog-api", List.of(imageUrl)));
        } catch (NullPointerException e) {
            logger.error(e);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response: ", e);
        }
    }

}
