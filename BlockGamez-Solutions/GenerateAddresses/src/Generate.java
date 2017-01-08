import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;


import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.xml.bind.DatatypeConverter;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import org.junit.*;




/**
 * Created by tiefenb7 on 10/29/2016.
 *
 * http://gobittest.appspot.com/Address
 */
public class Generate {

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String first = "first", second = "second", third = "third", fourth = "forth", fifth = "fifth", sixth = "sixth";
    private X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");

    public static void main(String[] argv) throws IOException, NoSuchAlgorithmException {}




    public static String toHex(byte[] data) {
        return ByteUtil.byteArrayToHexString(data);
    }

    public static String toHexString(byte[] array){
        return ByteUtil.byteArrayToHexString(array);
    }

    public static byte[] toByteArray(String byteString)
    {
        return ByteUtil.stringToByteArray(byteString);
    }


    public String UserInputFullSet() throws IOException
    {
        /**
         *
         * Ask user for inputs, to put into hash
         */

        // ######################### Comment out if you want to set UserSetString manually ##########################
        UserInput(first);
        UserInput(second);
        UserInput(third);
        UserInput(fourth);
        UserInput(fifth);
        UserInput(sixth);
        // ###########################################################################################################

        return first + second + third + fourth + fifth + sixth;
    }

    public AsymmetricCipherKeyPair generateKeyPair(String stringValue) throws NoSuchAlgorithmException, IOException
    {

        // Use secure hash algorithm to convert into byte array to be set as Secure Random seed

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] seed = digest.digest(stringValue.getBytes("UTF-8"));



        // Use secp256k1 named curve to generate ECDSA points for Bitcoin addresses
        //X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");
        ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(),
                ecp.getG(), ecp.getN(), ecp.getH(),
                ecp.getSeed());

        // Generate a private key and a public key based on our Secure seed
        AsymmetricCipherKeyPair keyPair;
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom(seed));
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keyGenParams);
        keyPair = generator.generateKeyPair();
        return keyPair;
    }

    public String privateToWif(byte[] privateKey)
    {
        byte[] newValue80Front = Add80Byte(privateKey);
        System.out.println("Add Byte 80 " + toHex(newValue80Front));

        byte[] newValue256 = SHA256hash(newValue80Front);
        System.out.println("SHA256 " + toHex(newValue256));

        byte[] newValue256Again = SHA256hash(newValue256);
        System.out.println("SHA256 Again " + toHex(newValue256Again));

        byte[] grabFourBytes = GrabFirstFourBytes(newValue256Again);
        System.out.println("GrabFirstFour " + toHex(grabFourBytes));

        byte[] keyAndCheckSum = AddChecksumEndOfKey(newValue80Front,grabFourBytes);
        System.out.println("Add Checksum " + toHex(keyAndCheckSum));

        String WIF = Base58.encode(keyAndCheckSum);
        System.out.println("Private Key WIF " + WIF);
        System.out.println("");

        return WIF;
    }

    public String publicToWif(byte[] publicKey)
    {
        byte[] newValue256 = SHA256hash(publicKey);
        System.out.println("SHA256 " + toHex(newValue256));

        byte[] newValue160 = RIPEMD160(newValue256);
        System.out.println("RIPEMD160 " + toHex(newValue160));

        byte[] newValueNetwork = AddNetworkBytes(newValue160);
        System.out.println("ADDBYTES " + toHex(newValueNetwork));

        byte[] re_SHA256_First = SHA256hash(newValueNetwork);
        System.out.println("SHA256Again " + toHex(re_SHA256_First));

        byte[] re_SHA256_Second = SHA256hash(re_SHA256_First);
        System.out.println("SHA256AgainSecondTime " + toHex(re_SHA256_Second));

        byte[] grabFourBytes = GrabFirstFourBytes(re_SHA256_Second);
        System.out.println("GrabFirstFour " + toHex(grabFourBytes));

        byte[] AddSeven = AddSevenEndOfNetworkByte(grabFourBytes, newValueNetwork);
        System.out.println("AddFourBytesToNetwork " + toHex(AddSeven));

        String WIF = Base58.encode(AddSeven);
        System.out.println("Bitcoin Address " + WIF);
        return WIF;
    }

    public void generatePubPriv(String UserSetString) throws NoSuchAlgorithmException, IOException {

        AsymmetricCipherKeyPair keyPair = generateKeyPair(UserSetString);

        //Grab the pub and priv from keypair generated above
        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();
        byte[] privateKeyBytes = privateKey.getD().toByteArray();

        ECFieldElement getFirst = publicKey.getQ().getXCoord();
        ECFieldElement getSecond = publicKey.getQ().getYCoord();

        // Add 04 in-front of public ECDSA Key
        String finalPublic = "04" + getFirst.toString() + getSecond.toString();


        System.out.println("");
        // First print our generated private key and public key
        System.out.println("Private key: " + toHexString(privateKeyBytes));
        System.out.println("Public key: " + finalPublic);
        System.out.println("");

        /*
        ECPoint dd = ecp.getG().multiply(privateKey.getD());

        byte[] publickey=new byte[65];
        System.arraycopy(dd.getY().toBigInteger().toByteArray(), 0, publickey, 64-dd.getY().toBigInteger().toByteArray().length+1, dd.getY().toBigInteger().toByteArray().length);
        System.arraycopy(dd.getX().toBigInteger().toByteArray(), 0, publickey, 32-dd.getX().toBigInteger().toByteArray().length+1, dd.getX().toBigInteger().toByteArray().length);
        publickey[0]=4;*/

        byte[] public_Key = toByteArray(finalPublic);

        privateToWif(privateKeyBytes);
        publicToWif(public_Key);

    }

    public void UserInput(String value) throws IOException {


        String inputted= JOptionPane.showInputDialog("Please input " + value + " random string");


        switch (value){

            case "first":
                first = inputted;
                return;
            case "second":
                second = inputted;
                return;
            case "third":
                third = inputted;
                return;
            case "forth":
                fourth = inputted;
                return;
            case "fifth":
                fifth = inputted;
                return;
            case "sixth":
                sixth = inputted;
                return;
        }

    }

    private byte[] SHA256hash(byte[] enterKey){
        /*SHA256Digest digester=new SHA256Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;*/
        return ByteUtil.SHA256hash(enterKey);
    }


    private byte[] RIPEMD160(byte[] enterKey){
        RIPEMD160Digest digester = new RIPEMD160Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }

    private byte[] concateByteArrays(byte[] frontByteArray,byte[] backByteArray)
    {
        return ByteUtil.concateByteArrays(frontByteArray,backByteArray);
    }

    private byte[] Add80Byte(byte[] enterKey)
    {
        byte[] eightyByte = {(byte)0x80};
        return concateByteArrays(eightyByte,enterKey);
    }

    private byte[] AddNetworkBytes(byte[] enterKey){

        byte[] networkByte = {(byte) 0x0 };
        return concateByteArrays(networkByte,enterKey);
    }

    private byte[] GrabFirstFourBytes(byte[] enterKey){

        byte[] firstFourBytes = new byte[4];

        for(int i = 0; i < firstFourBytes.length; i++){
            firstFourBytes[i] = enterKey[i];
        }

        return  firstFourBytes;
    }

    private byte[] AddSevenEndOfNetworkByte(byte[] firstFour, byte[] NetworkByteArray)
    {
        return concateByteArrays(NetworkByteArray,firstFour);
    }

    private byte[] AddChecksumEndOfKey(byte[] key,byte[] checksum)
    {
        return concateByteArrays(key,checksum);
    }
}
