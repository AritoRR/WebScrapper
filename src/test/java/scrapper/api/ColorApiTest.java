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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static scrapper.Reflection.getStaticPrivateField;

@ExtendWith(MockitoExtension.class)
public class ColorApiTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private JsonNode jsonNode;

    @Mock
    private JsonNode resultNode;

    @InjectMocks
    private ColorApi colorApi;

    @Before
    public void setUp() {
        OutputWriter.setType("csv");
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        mapper = mock(ObjectMapper.class);
        jsonNode = mock(JsonNode.class);
        resultNode = mock(JsonNode.class);
        colorApi = new ColorApi(httpClient, mapper);
    }

    @Test
    public void getHttpClientTest() {
        ColorApi colorApiTest = new ColorApi();
        assertNotNull(colorApiTest.getHttpClient());
    }

    @Test
    public void getLoggerTest() {
        ColorApi colorApiTest = new ColorApi();
        assertEquals(colorApiTest.getLogger(), getStaticPrivateField(colorApiTest.getClass(), "logger"));
    }

    @Test
    public void getSourceTest() {
        ColorApi colorApiTest = new ColorApi();
        assertEquals(colorApiTest.getSource(), getStaticPrivateField(colorApiTest.getClass(), "SOURCE"));
    }

    @Test
    public void rgbToHexTest() {
        int r = 150;
        int g = 68;
        int b = 68;
        String expectedHex = "964444";
        ColorApi colorApiTest = new ColorApi();

        assertEquals(expectedHex, colorApiTest.rgbToHex(r, g, b));
    }

    @Test
    public void makeColorTest() {
        int[][] colorEntries = {{90,37,58},{218,81,57},{205,202,165},{192,210,171},{52,90,90}};
        ArrayList<String> expectedColor = new ArrayList<>();
        expectedColor.add("https://www.colorhexa.com/5a253a");
        expectedColor.add("https://www.colorhexa.com/da5139");
        expectedColor.add("https://www.colorhexa.com/cdcaa5");
        expectedColor.add("https://www.colorhexa.com/c0d2ab");
        expectedColor.add("https://www.colorhexa.com/345a5a");
        ColorApi colorApiTest = new ColorApi();

        assertEquals(expectedColor, colorApiTest.makeColor(colorEntries));
    }

    @Test
    public void handleApiResponseTest() {
        assertDoesNotThrow(() -> {
            String response = "{\"result\":[[255,0,0],[0,255,0],[0,0,255]]}";
            int[][] expectedColors = {{255, 0, 0}, {0, 255, 0}, {0, 0, 255}};

            when(mapper.readTree(response)).thenReturn(jsonNode);
            when(jsonNode.path("result")).thenReturn(resultNode);
            when(resultNode.toString()).thenReturn("[[255,0,0],[0,255,0],[0,0,255]]");
            when(mapper.readValue("[[255,0,0],[0,255,0],[0,0,255]]", int[][].class))
                    .thenReturn(expectedColors);

            colorApi.handleApiResponse(response);

            verify(mapper).readTree(response);
            verify(jsonNode).path("result");
            verify(mapper).readValue("[[255,0,0],[0,255,0],[0,0,255]]", int[][].class);
        });
    }

    @Test
    public void fetchDataTest_ShouldSendCorrectHttpRequest() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(httpResponse));

        colorApi.fetchData();

        verify(httpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void fetchDataTest_ShouldHandleExceptionsGracefully() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IllegalArgumentException("Test exception"));

        assertDoesNotThrow(() -> colorApi.fetchData());
    }

}
