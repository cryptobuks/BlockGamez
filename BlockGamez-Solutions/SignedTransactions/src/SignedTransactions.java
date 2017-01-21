
import com.google.gson.*;
import org.apache.commons.codec.DecoderException;
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
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.HexEncoder;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException, DecoderException {} //Creating Tests

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

    public void NewTransaction(String recipientAddress, String senderAddress, String senderPrivateWIF) throws IOException, NoSuchAlgorithmException, DecoderException {
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
        String sUnspentOutputs = "https://blockchain.info/unspent?active=" + senderAddress + "&format=json";
        String sURL = sUnspentOutputs; //just a string

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
            k = k + 5; //This needs to count by 5, to properly set all previous transactions at the correct index, 5 => 5 values needed for indexing during each iteration.
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

    /** Sign and verify the transaction **/
    public String SignandVerify(String senderAddress, String recipientAddress, BigDecimal amount, String senderPrivateWIF) throws IOException, NoSuchAlgorithmException, DecoderException {
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

        scriptPubKey = OP_DUP + " " + OP_HASH160 + " " + sizeToBase16 + " " + (senderHex.substring(2, senderHex.length() - 8)) + " " + OP_EQUALVERIFY + " " + OP_CHECKSIG;
        int inputsValueSize = k;

        /** Not Efficient, please change later**/
        int y = 2;
        int g = 3;
        int scriptLength = ((senderHex.substring(2, senderHex.length() - 8)).length() / 2) + 5;

        while(inputsValueSize > y){
            inputs[y] = "scriptLength: " + String.valueOf(scriptLength); //add 1 byte for each script opcode
            inputs[g] = "scriptSig: " + scriptPubKey;
            y = y + 5;
            g = g + 5;
        }

        String hashCodeType = "01000000";
        // Create the necessary values for a transaction
        String[] transaction = {"version: 1", "in_counter: " + inputsValueSize, "inputs: " + inputs, "out_counter: " + outputs.length, "outputs: " + outputs, "lock_time: 0", "hash_code_type: " + hashCodeType};

        /** Serialization Starts Here... **/
        serialize_transaction(transaction, inputsValueSize);
        String value1 = "return something lol.";
        return value1;
    }

    public String little_endian_hex_of_n_bytes(Integer i , int n){ //This one takes interger values

        String iToBase16 = i.toString(i, 16);
        String value = "0" + iToBase16+ String.join("", Collections.nCopies(n * 2,"0")); //Also double check this number
        return value;
    }

    public String little_endian_hex_of_n_bytes(String i , int n){ //This one takes BigInteger for :previousTx values

        String newValue = "";
        int count = 2;
        String pairs;

        for(int k = 0; k <= i.length() - 1; k++){
            if(count % 2 == 0){
                pairs = i.substring(k,count);
                newValue = newValue + pairs + ",";
            }
            count++;
        }

        newValue= newValue.substring(0, newValue.length() - 1); //remove last comma
        String[] myList = newValue.split(",");
        Collections.reverse(Arrays.asList(myList));
        newValue = "";
        for(int k = 0; k<= myList.length - 1; k++){
            newValue = newValue + myList[k];
        }
        return newValue;
    }

    public String parse_script(String script){
        return script;
    }

    public String[] serialize_transaction(String[] transaction, int inputsValueSize) throws UnsupportedEncodingException, DecoderException {

        /** Creating the transaction **/
        String tx = "";
        Integer i = Integer.parseInt(transaction[0].substring(transaction[0].lastIndexOf(' ') + 1)); // grab the version number from transaction array (anything after first blank space)
        tx = tx + little_endian_hex_of_n_bytes(i,3) + "\n"; //Double check this number....
        i = Integer.parseInt(transaction[1].substring(transaction[1].lastIndexOf(' ') + 1)); // grab the version number from transaction array (anything after first blank space)
        tx = tx + little_endian_hex_of_n_bytes(i,0) + "\n";

        String[] g = inputs;
        for(int k = 0; k< inputsValueSize; k++){
            if(g[k].contains("previousTx:")){
                String value = g[k];
                String newValue = value.substring(13,value.length()-1);
                //BigInteger bi = new BigInteger(newValue, 16);
                int biLength = newValue.length();
              //  BigInteger bi2 = BigInteger.valueOf(bi.intValue());
                tx = tx + little_endian_hex_of_n_bytes(newValue, biLength) + "\n";



            }
            if(g[k].contains("index:")) {
                i = Integer.parseInt(inputs[1].substring(inputs[1].lastIndexOf(' ') + 1)); // grab the version number from transaction array (anything after first blank space)
                tx = tx + little_endian_hex_of_n_bytes(i,4) + "\n";
            }
            if(g[k].contains("scriptLength:")){

                i = Integer.parseInt(inputs[2].substring(inputs[2].lastIndexOf(' ') + 1)); // grab the version number from transaction array (anything after first blank space)
                tx = tx + little_endian_hex_of_n_bytes(i,0) + "\n";
            }
            if(g[k].contains("scriptSig:")){
                String[] HexRep = g[k].split(" ");
                if(HexRep[1].equalsIgnoreCase("OP_DUP")){
                    HexRep[1] = "76"; // Hex representation of OP_DUP script
                }
                if(HexRep[2].equalsIgnoreCase("OP_HASH160")){
                    HexRep[2] = "a9"; // Hex representaion of OP_HASH160
                }
                if(HexRep[HexRep.length - 1].equalsIgnoreCase("OP_CHECKSIG")){
                    HexRep[HexRep.length - 1] = "ac"; //Hex representation of "OP_CHECKSIG
                }
                if(HexRep[HexRep.length - 2].equalsIgnoreCase("OP_EQUALVERIFY")){
                    HexRep[HexRep.length - 2] = "88"; //Hex representation of "OP_EQUALVERIFY
                }
                String newValue = "";
                for(int x = 1; x <= HexRep.length - 1; x++){ //x = 1, we don't need the definition
                    newValue = newValue + HexRep[x] + " ";
                }
                tx = tx + newValue + "\n";
            }

            if(g[k].contains("sequence_no:")){
                String[] HexRep = g[k].split(" ");
                tx = tx + HexRep[1] + "\n";
            }
        }
        System.out.println("TX: " + tx);
        return transaction;
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
