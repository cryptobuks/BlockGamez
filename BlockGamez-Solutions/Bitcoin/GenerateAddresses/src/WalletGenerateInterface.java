/**
 * Created by arjun on 1/14/17.
 */
public interface WalletGenerateInterface {

    public String privateToWif(byte[] privateKey);

    public String publicToWif(byte[] publicKey);

    public String WifToPrivate(String privateWIF);

    public String WifToPublic(String publicWIF);
}
