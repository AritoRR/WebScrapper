package scrapper;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static scrapper.Reflection.getStaticPrivateField;
import static scrapper.Reflection.invokeStaticPrivateMethod;
import static scrapper.Reflection.setStaticPrivateField;

public class MainTest {

    private ExecutorService executor;
    private CountDownLatch latch;

    @Before
    public void setUp() {
        setStaticPrivateField(Main.class, "maxThreads", null);
        setStaticPrivateField(Main.class, "timeOut", null);
        setStaticPrivateField(Main.class, "services", null);
        OutputWriter.setType("json");
        executor = mock(ExecutorService.class);
        latch = mock(CountDownLatch.class);
    }

    @Test
    public void extractingArgsTest_WithIncompleteArguments() {
        String[] args = {"5", "1000", "json"};

        boolean result = (Boolean) invokeStaticPrivateMethod(Main.class, "extractingArgs",
                new Class<?>[]{String[].class},
                new Object[]{args});

        assertFalse(result);
    }

    @Test
    public void extractingArgsTest_WithInvalidMaxThreads() {
        String[] args = {"a", "1", "dog-api", "json"};

        boolean result = (Boolean) invokeStaticPrivateMethod(Main.class, "extractingArgs",
                new Class<?>[]{String[].class},
                new Object[]{args});

        assertFalse(result);
    }

    @Test
    public void extractingArgsTest_WithInvalidTimeOut() {
        String[] args = {"1", "-1", "dog-api", "json"};
        boolean result = (Boolean) invokeStaticPrivateMethod(Main.class, "extractingArgs",
                new Class<?>[]{String[].class},
                new Object[]{args});
        assertFalse(result);
    }

    @Test
    public void extractingArgsTest_WithInvalidType() {
        String[] args = {"1", "1", "dog-api", "Invalid"};

        boolean result = (Boolean) invokeStaticPrivateMethod(Main.class, "extractingArgs",
                new Class<?>[]{String[].class},
                new Object[]{args});

        assertFalse(result);
    }

    @Test
    public void extractingArgsTest_WithCorrectArgs() {
        String[] args = {"1", "1", "dog-api", "csv"};

        boolean result = (Boolean) invokeStaticPrivateMethod(Main.class, "extractingArgs",
                new Class<?>[]{String[].class},
                new Object[]{args});

        assertTrue(result);
    }

    @Test
    public void scrapeTest_WithInvalidService() {
        String[] args = {"Invalid"};
        setStaticPrivateField(Main.class, "services", args);

        invokeStaticPrivateMethod(Main.class, "scrape",
                new Class<?>[]{ExecutorService.class, CountDownLatch.class},
                new Object[]{executor, latch});

        verify(executor, times(0)).execute(any());
    }

    @Test
    public void scrapeTest_WithValidService() {
        String[] args = {"dog-api", "neko-api", "colormind-api"};
        setStaticPrivateField(Main.class, "services", args);

        invokeStaticPrivateMethod(Main.class, "scrape",
                new Class<?>[]{ExecutorService.class, CountDownLatch.class},
                new Object[]{executor, latch});

        verify(executor, times(3)).execute(any());
    }

    @Test
    public void mainTest_WithValidService() {
        String[] args = {"1", "1", "dog-api", "csv"};

        invokeStaticPrivateMethod(Main.class, "main",
                new Class<?>[]{String[].class},
                new Object[]{args});

        assertEquals(1, getStaticPrivateField(Main.class, "maxThreads"));
        assertEquals(1, getStaticPrivateField(Main.class, "timeOut"));
        assertEquals("csv", getStaticPrivateField(OutputWriter.class, "type"));
    }
}
