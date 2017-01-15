import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class SignedTransactionsTest{


    @Test
    public void testSignedTransaction_1() throws NoSuchAlgorithmException, IOException {

        String recipientAddress = "1KHxSzFpdm337XtBeyfbvbS9LZC1BfDu8K";
        String senderAddress = "1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX";
        String senderprivateKeyWIF = "76a91499bc78ba577a95a11f1a344d4d2ae55f2f857b9888ac";

        SignedTransactions createTransaction = new SignedTransactions();
        createTransaction.NewTransaction(recipientAddress, senderAddress, senderprivateKeyWIF);
//        assertEquals(expectedWIF,actualWif);
    }




}