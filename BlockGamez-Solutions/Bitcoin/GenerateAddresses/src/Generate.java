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
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;





/**
 * Created by tiefenb7 on 10/29/2016.
 *
 * http://gobittest.appspot.com/Address
 */
public class Generate {

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String first = "first", second = "second", third = "third", fourth = "forth", fifth = "fifth", sixth = "sixth";
    //private X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");

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

    public AsymmetricCipherKeyPair generateKeyPair(String seedValue) throws NoSuchAlgorithmException, IOException
    {

        // Use secure hash algorithm to convert into byte array to be set as Secure Random seed

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] seed = digest.digest(seedValue.getBytes("UTF-8"));



        // Use secp256k1 named curve to generate ECDSA points for Bitcoin addresses
        X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");
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

    private  KeyPair getKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDsA", "SC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    public String privateToWif(byte[] privateKey)
    {
        WalletGenerateInterface bitCoin = new BitCoinWalletGenerate();
        return bitCoin.privateToWif(privateKey);
    }

    public String publicToWif(byte[] publicKey)
    {
        WalletGenerateInterface bitCoin = new BitCoinWalletGenerate();
        return bitCoin.publicToWif(publicKey);
    }

    public void generatePubPriv(String seedValue) throws NoSuchAlgorithmException, IOException {

        AsymmetricCipherKeyPair keyPair = generateKeyPair(seedValue);

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
        System.out.println("Private key: " + ByteUtil.byteArrayToHexString(privateKeyBytes));
        System.out.println("Public key: " + finalPublic);
        System.out.println("");

        byte[] publicKeyBytes = ByteUtil.stringToByteArray(finalPublic);

        privateToWif(privateKeyBytes);
        publicToWif(publicKeyBytes);

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

}
