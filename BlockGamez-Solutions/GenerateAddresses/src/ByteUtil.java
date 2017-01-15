import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;

/**
 * Created by arjun on 12/30/16.
 */
public class ByteUtil {
    public static String byteArrayToHexString(byte[] data) {
        return DatatypeConverter.printHexBinary(data);
    }

    public static byte[] stringToByteArray(String byteString)
    {
        return DatatypeConverter.parseHexBinary(byteString);
    }

    public static byte[] concateByteArrays(byte[] frontByteArray,byte[] backByteArray)
    {
        byte[] newByteArray = new byte[frontByteArray.length+backByteArray.length];
        System.arraycopy(frontByteArray,0,newByteArray,0,frontByteArray.length);
        System.arraycopy(backByteArray,0,newByteArray,frontByteArray.length,backByteArray.length);
        return newByteArray;
    }

    public static byte[] SHA256hash(byte[] enterKey)
    {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(enterKey);
            return md.digest();
        } catch (NoSuchAlgorithmException ex)
        {
            System.out.println("ERROR: Unable to SHA-hash byte array");
        }
        byte [] bKey = {(byte) 0x0};
        return bKey;

    }

    public static byte[] RIPEMD160(byte[] enterKey)
    {
        try{
            MessageDigest md = MessageDigest.getInstance("RIPEMD160");
            md.update(enterKey);
            return md.digest();
        } catch (NoSuchAlgorithmException ex)
        {
            System.out.println("ERROR: Unable to RIPEMD160 byte array");
        }
        byte [] bKey = {(byte) 0x0};
        return bKey;
    }
}
