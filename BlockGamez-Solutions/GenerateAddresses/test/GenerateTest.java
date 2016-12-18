import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by arjun on 12/17/16.
 */
class GenerateTest {

    @Test
    public void testWIFGenerate() throws NoSuchAlgorithmException, IOException {
        String userStringSet = "HelloWorld!!!!!";
        String expectedWIF = "1HkDUvRAAaGvgW6X1inVAtv4prLvCiMWk2";
        Generate test = new Generate();
        String actualWif = test.generatePubPriv(userStringSet);
        assertEquals(expectedWIF,actualWif);
    }

}