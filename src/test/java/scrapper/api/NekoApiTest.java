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
public class NekoApiTest {
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

    @Mock
    private JsonNode firstResultNode;

    @Mock
    private JsonNode urlNode;

    @InjectMocks
    private NekoApi nekoApi;

    @Before
    public void setUp() {
        OutputWriter.setType("csv");
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        mapper = mock(ObjectMapper.class);
        jsonNode = mock(JsonNode.class);
        resultsNode = mock(JsonNode.class);
        firstResultNode = mock(JsonNode.class);
        urlNode = mock(JsonNode.class);
        nekoApi = new NekoApi(httpClient, mapper);
    }

    @Test
    public void getHttpClientTest() {
        NekoApi nekoApiTest = new NekoApi();

        assertNotNull(nekoApiTest.getHttpClient());
    }

    @Test
    public void getLoggerTest() {
        NekoApi nekoApiTest = new NekoApi();

        assertEquals(nekoApiTest.getLogger(), getStaticPrivateField(nekoApiTest.getClass(), "logger"));
    }

    @Test
    public void getSourceTest() {
        NekoApi nekoApiTest = new NekoApi();

        assertEquals(nekoApiTest.getSource(), getStaticPrivateField(nekoApiTest.getClass(), "SOURCE"));
    }

    @Test
    public void handleApiResponseTest() {
        assertDoesNotThrow(() -> {
            String response = "{\"results\":[{\"artist_name\":\"木塘ムタン\",\"artist_href\":\"https://www.pixiv.net/en/users/28603589\",\"source_url\":\"https://www.pixiv.net/en/artworks/86993400\",\"url\":\"https://nekos.best/api/v2/neko/cac6bb9a-d297-4b7e-97ce-122263961310.png\"}]}";
            String expectedUrl = "https://nekos.best/api/v2/neko/cac6bb9a-d297-4b7e-97ce-122263961310.png";

            when(mapper.readTree(response)).thenReturn(jsonNode);
            when(jsonNode.path("results")).thenReturn(resultsNode);
            when(resultsNode.get(0)).thenReturn(firstResultNode);
            when(firstResultNode.path("url")).thenReturn(urlNode);
            when(urlNode.asText()).thenReturn(expectedUrl);

            nekoApi.handleApiResponse(response);

            verify(mapper).readTree(response);
            verify(jsonNode).path("results");
            verify(resultsNode).get(0);
            verify(firstResultNode).path("url");
            verify(urlNode).asText();
        });
    }

    @Test
    public void fetchData_ShouldSendCorrectHttpRequest() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(httpResponse));

        nekoApi.fetchData();

        verify(httpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void fetchData_ShouldHandleExceptionsGracefully() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IllegalArgumentException("Test exception"));

        assertDoesNotThrow(() -> nekoApi.fetchData());
    }
}
