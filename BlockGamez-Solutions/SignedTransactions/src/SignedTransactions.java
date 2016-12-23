
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        String grabbedJsonValue = rootobj.get(grabJsonValue).getAsString(); //just grab the zipcode

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

        BigDecimal[] val = new BigDecimal[10];
        Arrays.fill(val, BigDecimal.ZERO);

    }

}


