import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.util.encoders.Hex;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by tiefenb7 on 2/28/17.
 */
public class transaction {

    /**
     * recipientAddress -> The private key we are sending a transaction to (In WIF Format)
     * senderAddress -> The WIF address that is sending the bitcoin
     * amount -> The amount of Bitcoin being sent (BigDecimal format, no floats for money).
     * transactionFee -> the fee for doing the job (BigDecimal)
     */
    static BigDecimal amount = new BigDecimal("0.00001");
    BigDecimal transactionFee = new BigDecimal("0.00005");
    static BigDecimal change = new BigDecimal("0");
    int inputCount = 0; int outputCount = 0;
    String senderHexString = "";
    String recipientHexString = "";

    public static void main(String args[]) throws IOException {

        String recipientAddress = "";
        String senderAddress = "";
        String senderprivateKeyWIF = "";

        transaction createTransaction = new transaction();
        String[] createTX = createTransaction.ConstructTransaction(createTransaction.Inputs(recipientAddress,senderAddress,senderprivateKeyWIF),createTransaction.Outputs(senderAddress, recipientAddress, amount, change));
        createTransaction.SerializeTransaction(createTX);
        createTransaction.Verify(senderprivateKeyWIF);

    }

    //Add the SATOSHI Constant (100,000,000 Satoshi = 1 BTC)
    public BigDecimal SATOSHI_PER_BITCOIN() {
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

    public String[] Inputs(String recipientAddress, String senderAddress, String senderPrivateWIF) throws IOException {
        System.out.println("About to send " + amount + " Bitcoin to address " + recipientAddress + " from address " + senderAddress + " with a transaction fee of: " + transactionFee + "\n");
        /** URLs to blockchain's JSON Values **/
        String sFinalBalance = "https://blockchain.info/address/" + senderAddress + "?format=json"; //just a string
        BigDecimal finalBalanceBD = null;
        try {
            finalBalanceBD = new BigDecimal(parseUrlInJson("final_balance", sFinalBalance)).divide(SATOSHI_PER_BITCOIN());
        } catch (IOException e) {
            System.out.println("IO EXCEPTION>>>> " + e.toString());
        } finally {
            System.out.println("Current Balance of Sender: " + finalBalanceBD);
        }

        int res = finalBalanceBD.compareTo(amount.add(transactionFee));
        if (res == -1) {

            /** ######################## THROW EXCEPTION IF FUNDS ARE NOT AVAILABLE, COMMENTING THIS OUT FOR TESTING ##########################**/
            throw new IllegalArgumentException("Insufficient funds in Wallet");
            /** ###############################################################################################################################**/
        }


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
        BigDecimal input_total = new BigDecimal("0");
        String[] inputs = new String[1000];

        /**
         * We use a for loop here... We need to find the transactions that when added together
         * will give the amount that you want to send. Remember: wallets have multiple addresses
         * holding different amounts of bitcoin that are associated with it.
         */

        inputCount = 0;
        int k = 0;
        for(int i = 0; i <= sizeOfUnspentOutputs - 1; i++)
        {
            JsonElement getFirstJsonString = unspentoutputs.get(i);
            JsonObject getJsonValue = getFirstJsonString.getAsJsonObject();
            JsonElement reversedTxHash = getJsonValue.get("tx_hash_big_endian"); inputs[k] = "previousTx:" + String.valueOf(reversedTxHash) + "#"; //Reversed Previous TX Hash
            JsonElement index = getJsonValue.get("tx_output_n"); inputs[k+1] = "index:" + String.valueOf(index) + "#";
            k = k + 2;
            BigDecimal amount = new BigDecimal(String.valueOf(getJsonValue.get("value"))).divide(SATOSHI_PER_BITCOIN());
            input_total = input_total.add(amount);
            int res2;
            res2 = input_total.compareTo(amount.add(transactionFee));
            inputCount = inputCount + 1; //Everytime we see this, we keep track of amount of outputs

            if((res2 == 0) || (res2 == 1)){ break;} //we have the amount we need for this transaction

        }
        change = input_total.subtract(transactionFee).subtract(amount);

        int res3;
        res3 = input_total.compareTo(amount.add(transactionFee));
        if (res3 == -1) {

            /** ######################## THROW EXCEPTION IF YOU DON'T HAVE ENOUGH, COMMENTING THIS OUT FOR TESTING ##########################**/
            throw new IllegalArgumentException("Unable to process transaction");
            /** ###############################################################################################################################**/
        }
        if(change.compareTo(BigDecimal.ZERO) == -1){ //Your in the negative, recheck your values
            throw new IllegalArgumentException("Unable to process transaction");

        }
        return inputs;
    }

    public String[] Outputs(String senderAddress, String recipientAddress, BigDecimal amount, BigDecimal change)
    {

        //Going to cheat here and use Library for Base58 conversion.....
        byte[] senderHex = Base58.decode(senderAddress);
        byte[] recipientHex = Base58.decode(recipientAddress);
        senderHexString = toHex(senderHex);
        recipientHexString = toHex(recipientHex);

        String OP_DUP = "76";
        String OP_EQUALVERIFY = "88";
        String OP_HASH160 = "a9";
        String OP_CHECKSIG = "ac";
        Integer size = (recipientHexString.substring(2,recipientHexString.length()-8).length()/2);
        String sizeToBase16 = size.toString(size, 16);
        String[] outputs = new String[100];
        outputs[0] = "value:" + String.valueOf(amount) + "#"; //value
        outputs[1] = "scriptPubKey:" + OP_DUP + " " + OP_HASH160 + " " + sizeToBase16 + " " + recipientHexString.substring(2,recipientHexString.length()-8) + " " + OP_EQUALVERIFY + " " + OP_CHECKSIG + "#@";

        Integer size2 = (senderHexString.substring(2,senderHexString.length()-8).length()/2);
        String secoundSizeToBase16 = size2.toString(size2,16);
        outputCount++;

        if(change.compareTo(BigDecimal.ZERO) == 1)
        {
            outputs[2] = "value:" + String.valueOf(change) + "#";
            outputs[3] = "scriptPubKey:" + OP_DUP + " " + OP_HASH160 + " " + secoundSizeToBase16 + " " + senderHexString.substring(2,senderHexString.length()-8) + " " + OP_EQUALVERIFY + " " + OP_CHECKSIG + "#";
            outputCount++;
        }
        return outputs;
    }

    public String[] ConstructTransaction(String[] inputs, String[] outputs)
    {
        String inputsCollect = "";
        String scriptSig = outputs[3];
        scriptSig = scriptSig.substring(13);

        List<String> list = new ArrayList<String>(Arrays.asList(inputs));
        list.removeAll(Collections.singleton(null));
        String[] removeAllNulls =  list.toArray(new String[list.size()]);
        Integer size = 0;
        int count = 1;
        for(int i = 0; i <= removeAllNulls.length - 1; i++){
            inputsCollect = inputsCollect + removeAllNulls[i];
            if(count % 2 == 0)
            {
                size = senderHexString.substring(2,senderHexString.length()-8).length()/2 + 5;
                inputsCollect = inputsCollect + "scriptLength:" + size.toString() + "#";
                inputsCollect = inputsCollect + "scriptSig:" + scriptSig + "#";
                inputsCollect = inputsCollect + "sequence_no:ffffffff#@";
            }
            count++;
        }

        String outputsCollect = "";
        List<String> list2 = new ArrayList<String>(Arrays.asList(outputs));
        list2.removeAll(Collections.singleton(null));
        String[] removeAllNulls2 =  list2.toArray(new String[list2.size()]);

        for(int i = 0; i <= removeAllNulls2.length - 1; i++) {
            outputsCollect = outputsCollect + removeAllNulls2[i];



        }

        String[] tx = new String[10];

        // BUILD
        tx[0] = "version:1";
        tx[1] = "in_counter:" + String.valueOf(inputCount);
        tx[2]  = "inputs:" + inputsCollect;
        tx[3]  = "out_counter:" + String.valueOf(outputCount);
        tx[4]  = "outputs:" + outputsCollect;
        tx[5]  = "locktime:0";
        tx[6]  = "hash_code_type:01000000";

        return tx;
    }

    public String SerializeTransaction(String[] transaction)
    {
        String tx = "";
        String value = transaction[0];
        String newValue = value.substring(8);
        BigInteger version = new BigInteger(newValue,16);
        tx = tx + LittleEndianHex(version,4) + "\n";
        value = transaction[1];
        newValue = value.substring(11);
        version = new BigInteger(newValue,16);
        tx = tx + LittleEndianHex(version,1) + "\n";
        String[] parseInputs = transaction[2].split("@");
        String removeInputsSTR = parseInputs[0];
        removeInputsSTR = removeInputsSTR.substring(7);
        parseInputs[0] = removeInputsSTR;

        for(int i =0; i <= parseInputs.length -1; i++)
        {
            String[] inputs = parseInputs[i].split("#");

            for(int k = 0; k <= inputs.length-1; k++)
            {

                if(inputs[k].contains("previousTx:"))
                {

                    value = inputs[k];
                    newValue = value.substring(11);
                    String removeFirstAndLast = newValue.substring(1,newValue.length()-1);
                    version = new BigInteger(removeFirstAndLast,16);
                    tx = tx + LittleEndianHex(version,removeFirstAndLast.length()/2) + "\n";
                }
                if(inputs[k].contains("index:"))
                {

                    value = inputs[k];
                    newValue = value.substring(6);
                    version = new BigInteger(newValue,16);
                    tx = tx + LittleEndianHex(version,4) + "\n";
                }
                if(inputs[k].contains("scriptLength:"))
                {

                    value = inputs[k];
                    newValue = value.substring(13);
                    int intversion = Integer.parseInt(newValue);
                    tx = tx + LittleEndianTOHexString(intversion,1) + "\n";
                }
                if(inputs[k].contains("scriptSig:"))
                {

                    value = inputs[k];
                    newValue = value.substring(10);
                    tx = tx + newValue + "\n";
                }
                if(inputs[k].contains("sequence_no:"))
                {

                    value = inputs[k];
                    newValue = value.substring(12);
                    tx = tx + newValue + "\n";
                }
            }

        }

        if(transaction[3].contains("out_counter:"))
        {

            value = transaction[3];
            newValue = value.substring(12);
            version = new BigInteger(newValue,16);
            tx = tx + LittleEndianHex(version,1) + "\n";
        }

        String[] parseOutputs = transaction[4].split("@");
        String removeOutputsSTR = parseOutputs[0];
        removeOutputsSTR = removeOutputsSTR.substring(8);
        parseOutputs[0] = removeOutputsSTR;

        for(int i =0; i <= parseOutputs.length -1; i++)
        {
            String[] outputs = parseOutputs[i].split("#");

            for(int k = 0; k <= outputs.length-1; k++)
            {
                if(outputs[k].contains("value:"))
                {

                    value = outputs[k];
                    newValue = value.substring(6);
                    BigDecimal multiply = new BigDecimal(newValue).multiply(SATOSHI_PER_BITCOIN());
                    int intversion = multiply.intValue();
                    tx = tx + LittleEndianTOHexString(intversion,8) + "\n";
                }

                if(outputs[k].contains("scriptPubKey"))
                {
                    value = outputs[k];
                    newValue = value.substring(13);
                    StringBuilder str = new StringBuilder(newValue);
                    String test = str.toString();
                    test = test.replaceAll("\\s+","");
                    int intversion = test.length()/2;
                    tx = tx + LittleEndianTOHexString(intversion,1) + "\n";
                    tx = tx + test + "\n";
                }
            }
        }

        value = transaction[5];
        newValue = value.substring(9);
        version = new BigInteger(newValue,16);
        tx = tx + LittleEndianHex(version,4) + "\n";

        value = transaction[6];
        newValue = value.substring(15);
        tx = tx + newValue;
        System.out.println(tx);

        return tx;
    }

    public String LittleEndianTOHexString(int value, int n)
    {
        String toHexString = Integer.toHexString(value);
        String rightJustify = "";
        for(int i = 1; i <= ((n*2) - toHexString.length()); i++)
        {
            rightJustify = rightJustify + "0";
        }
        rightJustify = rightJustify + toHexString;

        String reverse = "";
        int count = 1;
        String temp = "";
        for(int k = rightJustify.length()-1; k >= 0; k--)
        {
            temp = temp + rightJustify.charAt(k);
            if(count == 2) {
                String index0 = String.valueOf(temp.charAt(0));
                String index1 = String.valueOf(temp.charAt(1));

                reverse = reverse + index1 + index0;
                temp = "";
                count = 0;
            }
            count++;
        }

        return reverse;
    }

    public String LittleEndianHex (BigInteger value, int n)
    {

        String toS = new BigInteger(String.valueOf(value)).toString(16);
        String rightJustify = "";
        for(int i = 1; i <= ((n*2) - toS.length()); i++)
        {
            rightJustify = rightJustify + "0";
        }
        rightJustify = rightJustify + toS;

        String reverse = "";
        int count = 1;
        String temp = "";
        for(int k = rightJustify.length()-1; k >= 0; k--)
        {
            temp = temp + rightJustify.charAt(k);
            if(count == 2) {
                String index0 = String.valueOf(temp.charAt(0));
                String index1 = String.valueOf(temp.charAt(1));

                reverse = reverse + index1 + index0;
                temp = "";
                count = 0;
            }
            count++;
        }
        return reverse;
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }
}