
import com.google.gson.*;
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
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * Created by Tief on 12/17/2016.
 */
public class SignedTransactions {

    /**
     * recipientAddress -> The private key we are sending a transaction to (In WIF Format)
     * senderAddress -> The WIF address that is sending the bitcoin
     * amount -> The amount of Bitcoin being sent (BigDecimal format, no floats for money).
     * transactionFee -> the fee for doing the job (BigDecimal)
     */
    BigDecimal amount = new BigDecimal("0.01");
    BigDecimal transactionFee = new BigDecimal("0.0005");
    BigDecimal change = new BigDecimal("0");
    String[] inputs;
    int k = 0;
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException {} //Creating Tests

    //Add the SATOSHI Constant (100,000,000 Satoshi = 1 BTC)
    public BigDecimal SATOSHI_PER_BITCOIN(){
        BigDecimal SATOSHI_PER_BITCOIN = new BigDecimal("100000000");
        return SATOSHI_PER_BITCOIN;
    }

    public String parseUrlInJson(String grabJsonValue, String sURL) throws IOException {
        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        String grabbedJsonValue = rootobj.get(grabJsonValue).getAsString();
        return grabbedJsonValue;
    }

    public void NewTransaction(String recipientAddress, String senderAddress, String senderPrivateWIF) throws IOException, NoSuchAlgorithmException {
        System.out.println("About to send " + amount + " Bitcoin to address " + recipientAddress + " from address " + senderAddress + " with a transaction fee of: " + transactionFee + "\n");
        /** URLs to blockchain's JSON Values **/
        String sFinalBalance = "https://blockchain.info/address/" + senderAddress + "?format=json"; //just a string
        BigDecimal finalBalanceBD = null;
        try{
            finalBalanceBD = new BigDecimal(parseUrlInJson("final_balance", sFinalBalance)).divide(SATOSHI_PER_BITCOIN());
            }
        catch(IOException e){
            System.out.println("IO EXCEPTION>>>> " + e.toString());
        } finally {
            System.out.println("Current Balance of Sender: " + finalBalanceBD);
        }

        int res = finalBalanceBD.compareTo(amount.add(transactionFee));
        if(res == -1){

            /** ######################## THROW EXCEPTION IF FUNDS ARE NOT AVAILABLE, COMMENTING THIS OUT FOR TESTING ##########################**/
            throw new IllegalArgumentException("Insufficient funds in Wallet");
            /** ###############################################################################################################################**/
        }
        // Need to check for pending payments, will do so later when testing...
        UnspentTransactions( transactionFee, senderAddress); // finalUnspentOutputs,
        SignandVerify(senderAddress,recipientAddress, amount, senderPrivateWIF);
    }

    /** # Need to check wallet if there is a result of one or more incoming payments. **/
    public String[] UnspentTransactions( BigDecimal transactionFee, String senderAddress) throws IOException {  //BigDecimal jsonGrabbedUnspentOutputs,
        String[] holdValues = new String[1000];
        String sUnspentOutputs = "https://blockchain.info/unspent?active=" + senderAddress + "&format=json";
        String sURL = sUnspentOutputs; //just a string

        System.out.println(sUnspentOutputs);

        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        /** This code is Not As Efficient As I Would Like, will go through code-review later... **/
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject();
        JsonArray unspentoutputs = rootobj.get("unspent_outputs").getAsJsonArray();
        Integer sizeOfUnspentOutputs = unspentoutputs.size();
        inputs = new String[sizeOfUnspentOutputs]; // create string array to hold all values of unspent outputs
        BigDecimal input_total = new BigDecimal("0");

        for(int i = 0; i <= sizeOfUnspentOutputs - 1; i++){

            JsonElement getFirstJsonString = unspentoutputs.get(i);
            JsonObject getJsonValue = getFirstJsonString.getAsJsonObject();
            JsonElement previousTx = getJsonValue.get("tx_hash_big_endian"); inputs[k] = "previousTx: " + previousTx;
            JsonElement index = getJsonValue.get("tx_output_n"); inputs[k+1] = "index: " + index;
            JsonElement value = getJsonValue.get("value"); inputs[k+2] = "scriptLength: " + null;
            String scriptSig = null; inputs[k+3] = "scriptSig: " + scriptSig; //make this blank, will sign later
            String sequenceNO = "ffffffff"; inputs[k+4] = "sequence_no: " + sequenceNO;
            k = k + 5; //This needs to count by 4, to properly set all previous transactions at the correct index, 5 => 5 values needed for indexing during each iteration.
            amount = new BigDecimal(value.toString()).divide(SATOSHI_PER_BITCOIN());
            input_total = input_total.add(amount);
            if((input_total.compareTo(amount.add(transactionFee)) == 0) || (input_total.compareTo(amount.add(transactionFee))) == 1){
                break;
            }
        }
        change = input_total.subtract(transactionFee).subtract(amount);
        if((input_total.compareTo(amount.add(transactionFee)) == -1) || (change.compareTo(BigDecimal.ZERO) == 0)){

            /** ############################### THROW EXCEPTION IF input_total is less than the overall amount or if remaining change is negative ######### **/
            throw new IllegalArgumentException("Throwing Exception, Check BigDecimal Values in UnspentTransactions");
            /** ########################################################################################################################################### **/
        }
        return inputs;
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }

    public static String toHexString(byte[] array){
        return ByteUtil.byteArrayToHexString(array);
    }

    public static byte[] toByteArray(String byteString)
    {
        return ByteUtil.stringToByteArray(byteString);
    }

    /** Sign and verify the transaction **/
    public String SignandVerify(String senderAddress, String recipientAddress, BigDecimal amount, String senderPrivateWIF) throws IOException, NoSuchAlgorithmException {
        /** Payment Scripts: https://en.bitcoin.it/wiki/Script **/
        String OP_DUP = "OP_DUP"; //Duplicates the top stack item.
        String OP_HASH160 = "OP_HASH160"; //The input is hashed twice: first with SHA-256 and then with RIPEMD-160.
        String OP_EQUALVERIFY = "OP_EQUALVERIFY"; //Same as OP_EQUAL, but runs OP_VERIFY afterward.
        String OP_CHECKSIG = "OP_CHECKSIG"; //The entire transaction's outputs, inputs, and script (from the most recently-executed OP_CODESEPARATOR to the end) are hashed. The signature used by OP_CHECKSIG must be a valid signature for this hash and public key. If it is, 1 is returned, 0 otherwise.

        /**
         * We have to use all of the bitcoins from the sender address as the input for the transaction
         * We then subtract the amount and transaction fee and the remaining change goes back to the
         * sender as a second output.
          */
        byte[] senderHexByte = Base58.decode(senderAddress);
        byte[] recipientHexByte = Base58.decode(recipientAddress);
        String senderHex = toHex(senderHexByte);
        String recipientHex = toHex(recipientHexByte);
        Integer size;
        String sizeToBase16, scriptPubKey;
        String[] outputs = new String[2];


        size = (((senderHex.substring(2, senderHex.length() - 8)).length()) / 2);
        sizeToBase16 = size.toString(size, 16);
        scriptPubKey = OP_DUP + " " + OP_HASH160 + " " + sizeToBase16 + " " + (senderHex.substring(2, senderHex.length() - 8)) + " " + OP_EQUALVERIFY + " " + OP_CHECKSIG;
        outputs[0] = "value: " + amount + "," + scriptPubKey;


        // the amount to transfer, we are leaving out the leading zeros and the 4 byte checksum.
        if(change.compareTo(BigDecimal.ZERO) > 0){
           // value = amount;
            size = (((recipientHex.substring(2, recipientHex.length() - 8)).length()) / 2);
            sizeToBase16 = size.toString(size, 16);
            scriptPubKey = OP_DUP + " " + OP_HASH160 + " " + sizeToBase16 + " " + (recipientHex.substring(2, recipientHex.length() - 8)) + " " + OP_EQUALVERIFY + " " + OP_CHECKSIG;
            outputs[1] = "value: " + change + "," + scriptPubKey;
        }

       // generatePubPriv("sfsdf");
//        X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");
//        ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(),
//                ecp.getG(), ecp.getN(), ecp.getH(),
//                ecp.getSeed());
//        BigInteger d = new BigInteger(toByteArray(senderPrivateWIF));
//        ECPoint q = domainParams.getG().multiply(d);
//        ECPublicKeyParameters publicParams = new ECPublicKeyParameters(q, domainParams);
//        byte[] finalPublic = publicParams.getQ().getEncoded();
//
//        System.out.println("HERE IT IS: " + toHexString(finalPublic));

        scriptPubKey = OP_DUP + " " + OP_HASH160 + " " + sizeToBase16 + " " + (senderHex.substring(2, senderHex.length() - 8)) + " " + OP_EQUALVERIFY + " " + OP_CHECKSIG;
        int inputsValueSize = k;


        String value1 = "Need to sign and verify the transaction -> Skipping...";
        return value1;
    }


    private byte[] SHA256hash(byte[] enterKey){
        return ByteUtil.SHA256hash(enterKey);
    }


    private byte[] RIPEMD160(byte[] enterKey){
        RIPEMD160Digest digester = new RIPEMD160Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }



}
