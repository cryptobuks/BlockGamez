import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * Created by tief on 3/30/17.
 */
public class EthereumTransaction {

    public static void main(String args[]) throws ExecutionException, InterruptedException {

        Web3j web3 = Web3j.build(new HttpService());  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        System.out.println(clientVersion); //List Geth Client Version


        /** Transfer Ether from One Party to Another **/


        /** ############# Create Raw ################### **/
        BigInteger nonceAmount = new BigInteger("1.0");
        BigInteger gasPrice = new BigInteger("1.0");
        BigInteger gasLimit = new BigInteger("1.0");
        String sendToAddress = "Enter Address Here!";
        BigInteger amount = Convert.toWei("1.0", Convert.Unit.ETHER).toBigInteger();

        RawTransaction createRawTransaction = RawTransaction.createEtherTransaction(
                nonceAmount,gasPrice,gasLimit,sendToAddress,amount
        );

        /** ############# Send ####################### **/

    }
}
