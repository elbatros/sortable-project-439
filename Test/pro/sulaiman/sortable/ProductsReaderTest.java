package pro.sulaiman.sortable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProductsReaderTest {
    private IoUtils.ProductsReader reader;
    @Before
    public void setUp() throws Exception {
        reader = IoUtils.getProductsReader("products.txt");
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void hasNextSubsequently() throws Exception {
        for (int i = 0; i < 5; i++) {
            reader.hasNext();
        }
    }

    @Test
    public void nextSubsequently() throws Exception {
        while(reader.next() != null) {
        }
    }

    @Test
    public void hasNextNext() throws Exception {
        while(reader.hasNext()) {
            assertNotNull(reader.next());
        }
    }

}