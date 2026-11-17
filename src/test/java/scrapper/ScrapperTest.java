package scrapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.api.Api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ScrapperTest {

    @Mock
    private ExecutorService executor;

    @Mock
    private Api api;

    private CountDownLatch latch;
    private Integer timeOut;
    private AtomicBoolean running;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        latch = new CountDownLatch(3);
        timeOut = 1;
        running = new AtomicBoolean(true);
    }

    @Test
    public void runTest_ShouldCallFetchDataAndScheduleNextExecution() {
        Scrapper scrapper = new Scrapper(executor, latch, timeOut, api, running);

        scrapper.run();

        verify(api, times(1)).fetchData();
        assertEquals(2, latch.getCount());
        verify(executor, times(1)).execute(any(Scrapper.class));
    }

    @Test
    public void runTest_ShouldCallFetchDataButNotScheduleNextExecutionWhenNotRunning() {
        running.set(false);
        Scrapper scrapper = new Scrapper(executor, latch, timeOut, api, running);

        scrapper.run();

        verify(api, times(1)).fetchData();
        assertEquals(2, latch.getCount());
        verify(executor, never()).execute(any(Scrapper.class));
    }

    @Test
    public void runTest_ShouldCallFetchDataButNotScheduleNextExecutionWhenLatchIsZero() {
        while (latch.getCount() > 0) {
            latch.countDown();
        }

        Scrapper scrapper = new Scrapper(executor, latch, timeOut, api, running);

        scrapper.run();

        verify(api, times(1)).fetchData();
        assertEquals(0, latch.getCount());
        verify(executor, never()).execute(any(Scrapper.class));
    }
}
