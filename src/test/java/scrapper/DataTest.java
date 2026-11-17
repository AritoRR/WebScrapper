package scrapper;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTest {

    private Data data;

    @Before
    public void setUp() {
        data = new Data();
    }

    @Test
    public void getSetApiNameTest() {
        data.setApiName("test");
        assertEquals("test", data.getApiName());
    }

    @Test
    public void getSetDataTest() {
        data.setData(List.of("test"));
        assertEquals(List.of("test"), data.getData());
    }
}
