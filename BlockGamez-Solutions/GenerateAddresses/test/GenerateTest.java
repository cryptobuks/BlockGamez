import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by arjun on 12/17/16.
 */
class GenerateTest {

    @Test
    public void testPrivateWIFGenerate() throws NoSuchAlgorithmException, IOException {
        String sPrivateKey = "0C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D";
        byte[] bPrivateKey = DatatypeConverter.parseHexBinary(sPrivateKey);

        Generate test = new Generate();
        String expectedWIF = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ";
        String actualWif = test.privateToWif(bPrivateKey);
        assertEquals(expectedWIF,actualWif);
    }

    @Test
    public void testPublicWIFGenerate()
    {
        String sPublicKey = "04D0DE0AAEAEFAD02B8BDC8A01A1B8B11C696BD3D66A2C5F10780D95B7DF42645CD85228A6FB29940E858E7E55842AE2BD115D1ED7CC0E82D934E929C97648CB0A";
        byte[] bPrivateKey = DatatypeConverter.parseHexBinary(sPublicKey);

        Generate test = new Generate();
        String expectedWif = "1GAehh7TsJAHuUAeKZcXf5CnwuGuGgyX2S";
        String actualWif = test.publicToWif(bPrivateKey);
        assertEquals(expectedWif,actualWif);
    }

}