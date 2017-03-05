import org.bouncycastle.crypto.digests.RIPEMD160Digest;

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
        RIPEMD160Digest digester = new RIPEMD160Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }

    public static byte[] Add80Byte(byte[] enterKey)
    {
        byte[] eightyByte = {(byte)0x80};
        return concateByteArrays(eightyByte,enterKey);
    }

    public static byte[] AddNetworkBytes(byte[] enterKey){

        byte[] networkByte = {(byte) 0x0 };
        return concateByteArrays(networkByte,enterKey);
    }

    public static byte[] GrabFirstFourBytes(byte[] enterKey){

        byte[] firstFourBytes = new byte[4];

        for(int i = 0; i < firstFourBytes.length; i++){
            firstFourBytes[i] = enterKey[i];
        }

        return  firstFourBytes;
    }

    public static byte[] AddSevenEndOfNetworkByte(byte[] firstFour, byte[] NetworkByteArray)
    {
        return concateByteArrays(NetworkByteArray,firstFour);
    }

    public static byte[] RemoveLastFourBytes(byte[] byteArray){
        byte[] trimmedByteArray = new byte[byteArray.length-4];

        for(int i=0; i < (byteArray.length-4);i++)
        {
            trimmedByteArray[i] = byteArray[i];
        }

        return trimmedByteArray;
    }

    public static byte[] RemoveFirstByte(byte[] byteArray){
        if (byteArray.length > 1) {
            byte[] trimmedByte = new byte[byteArray.length - 1];
            for (int i = 1; i < byteArray.length; i++) {
                trimmedByte[i-1] = byteArray[i];
            }

            return trimmedByte;
        }
        return null;
    }

    public static byte[] AddChecksumEndOfKey(byte[] key,byte[] checksum)
    {
        return concateByteArrays(key,checksum);
    }

}