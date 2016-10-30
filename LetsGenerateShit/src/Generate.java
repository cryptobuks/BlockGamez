import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.util.encoders.Hex;
import java.io.IOException;
import java.security.*;


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
        String suo = sha256(finalPublic);

        System.out.println("SHA-256 Hash on public key... ");
        System.out.println(suo);
        System.out.println("");
        System.out.println("RIPMD160 on SHA256 Hash... ");
        toRIPEMD160(suo);
        System.out.println("");
    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static void toRIPEMD160(String in) throws IOException {
        byte[] r = in.getBytes("US-ASCII");
        RIPEMD160Digest d = new RIPEMD160Digest();
        d.update (r, 0, r.length);
        byte[] o = new byte[d.getDigestSize()];
        d.doFinal (o, 0);
        Hex.encode (o, System.out);
        System.out.println();
    }
}
