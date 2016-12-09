import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.*;
import java.util.Base64;


/**
 * Created by tiefenb7 on 10/29/2016.
 */
public class Generate {


    public static void main(String[] argv) throws IOException, NoSuchAlgorithmException {

        Generate test = new Generate();
        test.generatePubPriv();


    }




    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }

    public void generatePubPriv() throws NoSuchAlgorithmException, IOException {

        X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");
        ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(),
                ecp.getG(), ecp.getN(), ecp.getH(),
                ecp.getSeed());

        /**
         *
         * SET THIS STRING TO WHATEVER YOU WANT
         *
         */
        String UserSetString = "This string will generate the same bitcoin address";
        byte[] seed = UserSetString.getBytes();


        // Generate a private key and a public key
        AsymmetricCipherKeyPair keyPair;
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom(seed));
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keyGenParams);
        keyPair = generator.generateKeyPair();





        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();
        byte[] privateKeyBytes = privateKey.getD().toByteArray();

        ECFieldElement getFirst = publicKey.getQ().getX();
        ECFieldElement getSecond = publicKey.getQ().getY();
        String finalPublic = "04" + getFirst.toString() + getSecond.toString();


        System.out.println("");
        // First print our generated private key and public key
        System.out.println("Private key: " + toHex(privateKeyBytes));
        System.out.println("Public key: " + finalPublic);
        System.out.println("");

        ECPoint dd = ecp.getG().multiply(privateKey.getD());

        byte[] publickey=new byte[65];
        System.arraycopy(dd.getY().toBigInteger().toByteArray(), 0, publickey, 64-dd.getY().toBigInteger().toByteArray().length+1, dd.getY().toBigInteger().toByteArray().length);
        System.arraycopy(dd.getX().toBigInteger().toByteArray(), 0, publickey, 32-dd.getX().toBigInteger().toByteArray().length+1, dd.getX().toBigInteger().toByteArray().length);
        publickey[0]=4;

        byte[] newValue256 = SHA256hash(publickey);
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

    }

    private byte[] SHA256hash(byte[] enterKey){
        SHA256Digest digester=new SHA256Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }


    private byte[] RIPEMD160(byte[] enterKey){
        RIPEMD160Digest digester = new RIPEMD160Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }

    private byte[] AddNetworkBytes(byte[] enterKey){

        byte[] networkByte = {(byte) 0x0 };
        byte[] newByteArray = new byte[networkByte.length + enterKey.length];
        System.arraycopy(networkByte, 0, newByteArray, 0, networkByte.length);
        System.arraycopy(enterKey, 0, newByteArray, networkByte.length, enterKey.length);

        return newByteArray;
    }

    private byte[] GrabFirstFourBytes(byte[] enterKey){

        byte[] firstFourBytes = new byte[4];

        for(int i = 0; i < firstFourBytes.length; i++){
            firstFourBytes[i] = enterKey[i];
        }

        return  firstFourBytes;
    }

    private byte[] AddSevenEndOfNetworkByte(byte[] firstFour, byte[] NetworkByteArray){

        byte[] newByteArray = new byte[NetworkByteArray.length + firstFour.length];
        System.arraycopy(NetworkByteArray, 0, newByteArray, 0, NetworkByteArray.length);
        System.arraycopy(firstFour, 0, newByteArray, NetworkByteArray.length, firstFour.length);

        return newByteArray;
    }
}
