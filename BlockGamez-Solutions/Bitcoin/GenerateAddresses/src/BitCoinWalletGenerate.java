/**
 * Created by arjun on 1/14/17.
 */
public class BitCoinWalletGenerate implements WalletGenerateInterface {

    @Override
    public String privateToWif(byte[] privateKey) {
        byte[] newValue80Front = ByteUtil.Add80Byte(privateKey);
        //System.out.println("Add Byte 80 " + ByteUtil.byteArrayToHexString(newValue80Front));

        byte[] newValue256 = ByteUtil.SHA256hash(newValue80Front);
        //System.out.println("SHA256 " + ByteUtil.byteArrayToHexString(newValue256));

        byte[] newValue256Again = ByteUtil.SHA256hash(newValue256);
        //System.out.println("SHA256 Again " + ByteUtil.byteArrayToHexString(newValue256Again));

        byte[] grabFourBytes = ByteUtil.GrabFirstFourBytes(newValue256Again);
        //System.out.println("GrabFirstFour " + ByteUtil.byteArrayToHexString(grabFourBytes));

        byte[] keyAndCheckSum = ByteUtil.AddChecksumEndOfKey(newValue80Front,grabFourBytes);
        //System.out.println("Add Checksum " + ByteUtil.byteArrayToHexString(keyAndCheckSum));

        String WIF = Base58.encode(keyAndCheckSum);
        System.out.println("Private Key WIF: " + WIF+"\n");

        return WIF;
    }

    @Override
    public String publicToWif(byte[] publicKey) {
        byte[] newValue256 = ByteUtil.SHA256hash(publicKey);
        //System.out.println("SHA256 " + ByteUtil.byteArrayToHexString(newValue256));

        byte[] newValue160 = ByteUtil.RIPEMD160(newValue256);
        //System.out.println("RIPEMD160 " + ByteUtil.byteArrayToHexString(newValue160));

        byte[] newValueNetwork = ByteUtil.AddNetworkBytes(newValue160);
        //System.out.println("ADDBYTES " + ByteUtil.byteArrayToHexString(newValueNetwork));

        byte[] re_SHA256_First = ByteUtil.SHA256hash(newValueNetwork);
        //System.out.println("SHA256Again " + ByteUtil.byteArrayToHexString(re_SHA256_First));

        byte[] re_SHA256_Second = ByteUtil.SHA256hash(re_SHA256_First);
        //System.out.println("SHA256AgainSecondTime " + ByteUtil.byteArrayToHexString(re_SHA256_Second));

        byte[] grabFourBytes = ByteUtil.GrabFirstFourBytes(re_SHA256_Second);
        //System.out.println("GrabFirstFour " + ByteUtil.byteArrayToHexString(grabFourBytes));

        byte[] AddSeven = ByteUtil.AddSevenEndOfNetworkByte(grabFourBytes, newValueNetwork);
        //System.out.println("AddFourBytesToNetwork " + ByteUtil.byteArrayToHexString(AddSeven));

        String WIF = Base58.encode(AddSeven);
        System.out.println("Public Key WIF " + WIF + "\n");
        return WIF;
    }

    @Override
    public String WifToPrivate(String privateWIF) {
        byte[] privateByte = Base58.decode(privateWIF);
        byte[] trimmedByte = ByteUtil.RemoveLastFourBytes(privateByte);
        byte[] privateKey =  ByteUtil.RemoveFirstByte(trimmedByte);
        return ByteUtil.byteArrayToHexString(privateKey);
    }

    @Override
    public String WifToPublic(String publicWIF) {
        byte[] publicByte = Base58.decode(publicWIF);

        return ByteUtil.byteArrayToHexString(publicByte);
    }
}
