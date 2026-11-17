package scrapper.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.OutputWriter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static scrapper.Reflection.getStaticPrivateField;

@ExtendWith(MockitoExtension.class)
public class DogApiTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private JsonNode jsonNode;

    @Mock
    private JsonNode resultsNode;

    @InjectMocks
    private DogApi dogApi;

    @Before
    public void setUp() {
        OutputWriter.setType("csv");
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        mapper = mock(ObjectMapper.class);
        jsonNode = mock(JsonNode.class);
        resultsNode = mock(JsonNode.class);
        dogApi = new DogApi(httpClient, mapper);
    }


    @Test
    public void getHttpClientTest() {
        DogApi dogApiTest = new DogApi();

        assertNotNull(dogApiTest.getHttpClient());
    }

    @Test
    public void getLoggerTest() {
        DogApi dogApiTest = new DogApi();

        assertEquals(dogApiTest.getLogger(), getStaticPrivateField(dogApiTest.getClass(), "logger"));
    }

    @Test
    public void getSourceTest() {
        DogApi dogApiTest = new DogApi();

        assertEquals(dogApiTest.getSource(), getStaticPrivateField(dogApiTest.getClass(), "SOURCE"));
    }

    @Test
    public void handleApiResponseTest() {
        assertDoesNotThrow(() -> {
            String response = "{\"message\":\"https://images.dog.ceo/breeds/lhasa/n02098413_5382.jpg\",\"status\":\"success\"}";
            String expectedUrl = "https://images.dog.ceo/breeds/lhasa/n02098413_5382.jpg";

            when(mapper.readTree(response)).thenReturn(jsonNode);
            when(jsonNode.path("message")).thenReturn(resultsNode);
            when(resultsNode.asText()).thenReturn(expectedUrl);

            dogApi.handleApiResponse(response);

            verify(mapper).readTree(response);
            verify(jsonNode).path("message");
            verify(resultsNode).asText();
        });
    }

    @Test
    public void fetchDataTest_ShouldSendCorrectHttpRequest() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(httpResponse));

        dogApi.fetchData();

        verify(httpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void fetchDataTest_ShouldHandleExceptionsGracefully() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IllegalArgumentException("Test exception"));

        assertDoesNotThrow(() -> dogApi.fetchData());
    }

}
