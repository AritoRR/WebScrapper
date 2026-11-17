package scrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static scrapper.Reflection.getStaticPrivateField;
import static scrapper.Reflection.setStaticPrivateField;

class OutputWriterTest {

    private final Data dataTest = new Data("test-api", List.of("test-data"));

    @TempDir
    private Path tempDir;

    private File testJsonFile;
    private File testCsvFile;

    private List<Data> readJsonData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (!testJsonFile.exists() || testJsonFile.length() == 0) {
                return List.of();
            }
            return mapper.readValue(testJsonFile,
                    mapper.getTypeFactory().constructCollectionType(List.class, Data.class));
        } catch (IOException e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @BeforeEach
    void setUp() {
        testJsonFile = new File(tempDir.toFile() + "output.json");
        testCsvFile = new File(tempDir.toFile() + "output.csv");

        setStaticPrivateField(OutputWriter.class, "fileJson", testJsonFile);
        setStaticPrivateField(OutputWriter.class, "fileCsv", testCsvFile);

        OutputWriter.setType("json");
    }

    @AfterEach
    void tearDown() {
        setStaticPrivateField(OutputWriter.class,"fileJson", new File("output.json"));
        setStaticPrivateField(OutputWriter.class,"fileCsv", new File("output.csv"));
    }

    @Test
    void setTypeTest() {
        OutputWriter.setType("csv");
        assertEquals("csv", getStaticPrivateField(OutputWriter.class, "type"));
    }

    @Test
    void jsonWriteTest_CallsOutputWriter() {
            OutputWriter.setType("json");
            OutputWriter.write(dataTest);
            assertTrue(testJsonFile.exists());
            assertTrue(testJsonFile.length() > 0);
            assertFalse(testCsvFile.exists());
    }

    @Test
    void jsonWriteTest_WithNonExistentFileShouldCreateNewFile() {
        Data testData = new Data("test-api", List.of("data1", "data2"));

        OutputWriter.write(testData);

        assertTrue(testJsonFile.exists());
        assertEquals(1, readJsonData().size());
    }

    @Test
    void jsonWriteTest_WithExistingDataShouldAppendData() {
        assertDoesNotThrow(() -> {
            ObjectMapper mapper = new ObjectMapper();
            List<Data> initialData = List.of(
                    new Data("api1", List.of("value1")),
                    new Data("api2", List.of("value2"))
            );
            mapper.writeValue(testJsonFile, initialData);

            Data newData = new Data("api3", List.of("value3"));

            OutputWriter.write(newData);

            List<Data> resultData = readJsonData();
            assertEquals(3, resultData.size());
            assertEquals("api3", resultData.get(2).getApiName());
        });
    }

    @Test
    void csvWriteTest_CallsOutputWriter() {
        OutputWriter.setType("csv");
        OutputWriter.write(dataTest);
        assertTrue(testCsvFile.exists());
        assertTrue(testCsvFile.length() > 0);
        assertFalse(testJsonFile.exists());
    }
}
