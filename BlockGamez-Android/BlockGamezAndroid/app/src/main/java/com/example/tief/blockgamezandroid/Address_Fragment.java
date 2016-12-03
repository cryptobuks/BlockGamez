package com.example.tief.blockgamezandroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.math.ec.ECFieldElement;
import org.spongycastle.math.ec.ECPoint;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Address_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Address_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Address_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Address_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Address_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Address_Fragment newInstance(String param1, String param2) {
        Address_Fragment fragment = new Address_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_transactions_,
                container, false);
        TextView test = (TextView)rootView.findViewById(R.id.test);
        try {
            test.setText(generatePubPriv());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return rootView;
    }


    public static String generatePubPriv() throws NoSuchAlgorithmException, IOException {

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

        ECPoint dd = ecp.getG().multiply(privateKey.getD());

        byte[] publickey=new byte[65];
        System.arraycopy(dd.getY().toBigInteger().toByteArray(), 0, publickey, 64-dd.getY().toBigInteger().toByteArray().length+1, dd.getY().toBigInteger().toByteArray().length);
        System.arraycopy(dd.getX().toBigInteger().toByteArray(), 0, publickey, 32-dd.getX().toBigInteger().toByteArray().length+1, dd.getX().toBigInteger().toByteArray().length);
        publickey[0]=4;

        byte[] newValue256 = SHA256hash(publickey);
        System.out.println("SHA256 " + toHex(newValue256));
        byte[] newValue160 = RIPEMD160(newValue256);
        System.out.println("RIPEMD160 " + toHex(newValue160));
        byte[] newValueNetwork = AddNetworkBytes(newValue160);
        System.out.println("ADDBYTES " + toHex(newValueNetwork));

        byte[] re_SHA256_First = SHA256hash(newValueNetwork);
        System.out.println("SHA256Again " + toHex(re_SHA256_First));
        byte[] re_SHA256_Second = SHA256hash(re_SHA256_First);
        System.out.println("SHA256AgainSecondTime " + toHex(re_SHA256_Second));

        byte[] grabFourBytes = GrabFirstFourBytes(re_SHA256_Second);
        System.out.println("GrabFirstFour " + toHex(grabFourBytes));

        byte[] AddSeven = AddSevenEndOfNetworkByte(grabFourBytes, newValueNetwork);
        System.out.println("AddFourBytesToNetwork " + toHex(AddSeven));

        String WIF = Base58.encode(AddSeven);
        System.out.println("Bitcoin Address " + WIF);

        return WIF;

    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }

    private static byte[] SHA256hash(byte[] enterKey){
        SHA256Digest digester=new SHA256Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }


    private static byte[] RIPEMD160(byte[] enterKey){
        RIPEMD160Digest digester = new RIPEMD160Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }

    private static byte[] AddNetworkBytes(byte[] enterKey){

        byte[] networkByte = {(byte) 0x0 };
        byte[] newByteArray = new byte[networkByte.length + enterKey.length];
        System.arraycopy(networkByte, 0, newByteArray, 0, networkByte.length);
        System.arraycopy(enterKey, 0, newByteArray, networkByte.length, enterKey.length);

        return newByteArray;
    }

    private static byte[] GrabFirstFourBytes(byte[] enterKey){

        byte[] firstFourBytes = new byte[4];

        for(int i = 0; i < firstFourBytes.length; i++){
            firstFourBytes[i] = enterKey[i];
        }

        return  firstFourBytes;
    }

    private static byte[] AddSevenEndOfNetworkByte(byte[] firstFour, byte[] NetworkByteArray){

        byte[] newByteArray = new byte[NetworkByteArray.length + firstFour.length];
        System.arraycopy(NetworkByteArray, 0, newByteArray, 0, NetworkByteArray.length);
        System.arraycopy(firstFour, 0, newByteArray, NetworkByteArray.length, firstFour.length);

        return newByteArray;
    }





    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
