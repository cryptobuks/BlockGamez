import javax.xml.bind.DatatypeConverter;

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
}
