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
    public void testPrivateWIFGenerate_1() throws NoSuchAlgorithmException, IOException {
        String sPrivateKey = "0C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D";
        byte[] bPrivateKey = DatatypeConverter.parseHexBinary(sPrivateKey);

        Generate test = new Generate();
        String expectedWIF = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ";
        String actualWif = test.privateToWif(bPrivateKey);
        assertEquals(expectedWIF,actualWif);
    }

    @Test
    public void testPublicWIFGenerate_1()
    {
        String sPublicKey = "04D0DE0AAEAEFAD02B8BDC8A01A1B8B11C696BD3D66A2C5F10780D95B7DF42645CD85228A6FB29940E858E7E55842AE2BD115D1ED7CC0E82D934E929C97648CB0A";
        byte[] bPrivateKey = DatatypeConverter.parseHexBinary(sPublicKey);

        Generate test = new Generate();
        String expectedWif = "1GAehh7TsJAHuUAeKZcXf5CnwuGuGgyX2S";
        String actualWif = test.publicToWif(bPrivateKey);
        assertEquals(expectedWif,actualWif);
    }

    @Test
    public void testPrivateWIFGenerate_2()throws NoSuchAlgorithmException, IOException {
        String sPrivateKey = "F4929C88370791C694440F621F228EC93D4CCD8D5255CF1DED0105CC976DE140";
        byte[] bPrivateKey = DatatypeConverter.parseHexBinary(sPrivateKey);

        Generate test = new Generate();
        String expectedWIF = "5Kfzs14jFfRcAnLDitmzo61TZwFFxMXx3htdk8xzVTu7sjn7siW";
        String actualWif = test.privateToWif(bPrivateKey);
        assertEquals(expectedWIF,actualWif);
    }

    @Test
    public void testPublicWIFGenerate_2()
    {
        String sPublicKey = "0406D851FDE3F8E78605D56C28D3F2184223FA1608B72F567275836EA798DC054D6DF314769A0CB73E508F9D76D88FDFD389D728FE3CAA905D92FF34AACC77369B";
        byte[] bPrivateKey = DatatypeConverter.parseHexBinary(sPublicKey);

        Generate test = new Generate();
        String expectedWif = "1PrDBFmTJWGGxWvf1LnnsgeUC7VsSQhPLW";
        String actualWif = test.publicToWif(bPrivateKey);
        assertEquals(expectedWif,actualWif);
    }

}