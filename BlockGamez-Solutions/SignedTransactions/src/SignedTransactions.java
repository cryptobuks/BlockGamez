
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by Tief on 12/17/2016.
 */
public class SignedTransactions {


    public static void main(String args[]) throws IOException {

        SignedTransactions createTransaction = new SignedTransactions();
        /**
         * recipientAddress -> The private key we are sending a transaction to (In WIF Format)
         * senderAddress -> The WIF address that is sending the bitcoin
         * amount -> The amount of Bitcoin being sent (BigDecimal format, no floats for money).
         * transactionFee -> the fee for doing the job (BigDecimal)
         */

        String recipientAddress = "1KHxSzFpdm337XtBeyfbvbS9LZC1BfDu8K";
        String senderAddress = "1KHxSzFpdm337XtBeyfbvbS9LZC1BfDu8K";
        BigDecimal amount = new BigDecimal("0.01");
        BigDecimal transactionFee = new BigDecimal("0.0005");
        createTransaction.NewTransaction(recipientAddress, senderAddress, amount, transactionFee);
    }

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


    public void NewTransaction(String recipientAddrress, String senderAddress, BigDecimal amount, BigDecimal transactionFee) throws IOException {

        System.out.println("About to send " + amount + " Bitcoin to address " + recipientAddrress + " from address " + senderAddress + " with a transaction fee of: " + transactionFee + "\n");
        /** URLs to blockchain's JSON Values **/
        String sFinalBalance = "https://blockchain.info/address/" + senderAddress + "?format=json"; //just a string
        String sUnspentOutputs = "https://blockchain.info/unspent?active=" + senderAddress + "&format=json";

        BigDecimal finalBalanceBD = null;
        BigDecimal finalUnspentOutputs = null;

        try{

            finalBalanceBD = new BigDecimal(parseUrlInJson("final_balance", sFinalBalance)).divide(SATOSHI_PER_BITCOIN());
            finalUnspentOutputs = new BigDecimal(parseUrlInJson("unspent_outputs",sUnspentOutputs));
            }
        catch(IOException e){
            System.out.println("IO EXCEPTION>>>> " + e.toString());
        } finally {
            System.out.println("Final Balance of Sender: " + finalBalanceBD);
            System.out.println("Final Unspent Outputs: " + finalUnspentOutputs);
        }

        int res = finalBalanceBD.compareTo(amount.add(transactionFee));
        if(res == -1){

            /** ######################## THROW EXCEPTION IF FUNDS ARE NOT AVAILABLE, COMMENTING THIS OUT FOR TESTING ##########################**/
            //throw new IllegalArgumentException("Insufficient funds in Wallet");
            /** ###############################################################################################################################**/
        }
        // Need to check for pending payments, will do so later when testing...
        System.out.println(UnspentTransactions());
        SignandVerify(senderAddress,recipientAddrress);
    }

    /** # Need to check wallet if there is a result of one or more incoming payments. **/
    public String UnspentTransactions(){

        String value = "Need to check for pending payments -> Skipping...";
        return value;

    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }


    /** Sign and verify the transaction **/
    public String SignandVerify(String senderAddress, String recipientAddress){

        byte[] senderHexByte = Base58.decode(senderAddress);
        byte[] recipientHexByte = Base58.decode(recipientAddress);
        String senderHex = toHex(senderHexByte);
        String recipientHex = toHex(recipientHexByte);
        


        /**
        w2 = Bitcoin.decode_base58(@secret_wif)
        w3 = w2[0..-9]
        @secret = w3[2..-1]

        @keypair = Bitcoin.open_key(@secret)
        raise "Invalid keypair" unless @keypair.check_key

        step_2 = (Digest::SHA2.new << [@keypair.public_key_hex].pack("H*")).to_s
        step_3 = (Digest::RMD160.new << [step_2].pack("H*")).to_s
        step_4 = "00" + step_3
        step_5 = (Digest::SHA2.new << [step_4].pack("H*")).to_s
        step_6 = (Digest::SHA2.new << [step_5].pack("H*")).to_s
        step_7 = step_7 = step_6[0..7]
        step_8 = step_4 + step_7
        step_9 = Bitcoin.encode_base58(step_8)

        raise "Public key does not match private key" if @sender != step_9

        puts "Public key matches private key, so we can sign the transaction..."
        **/

        String value = "Need to sign and verify the transaction -> Skipping...";
        return value;
    }

}


