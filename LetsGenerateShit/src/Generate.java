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


    // Tues - Brian flies in around 5pm
    // Stay overnight on Tues.
    // Wed - 1/2 day - go down to perrryburg then drive up


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

        // Generate a private key and a public key
        AsymmetricCipherKeyPair keyPair;
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
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

        System.out.println("HELLO " + toHex(SHA256hash(publickey)));

        byte[] newValue = SHA256hash(publickey);

        System.out.println("HELLO160 " + toHex(RIPEMD160(newValue)));




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
       // byte[] version = new byte[]{0x00};
       // return concateByteArray(version,retValue);
        return retValue;
    }
}
