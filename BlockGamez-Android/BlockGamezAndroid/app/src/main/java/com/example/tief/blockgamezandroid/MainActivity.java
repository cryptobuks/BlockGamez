package com.example.tief.blockgamezandroid;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.security.Security;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        try {
            Log.d("WIF ", generatePubPriv());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Address Key";
                case 1:
                    return "Transactions";
                case 2:
                    return "Merchants";
            }
            return null;
        }
    }

    public String generatePubPriv() throws NoSuchAlgorithmException, IOException {

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

    private byte[] SHA256hash(byte[] enterKey){
        SHA256Digest digester=new SHA256Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }


    private byte[] RIPEMD160(byte[] enterKey){
        RIPEMD160Digest digester = new RIPEMD160Digest();
        byte[] retValue=new byte[digester.getDigestSize()];
        digester.update(enterKey, 0, enterKey.length);
        digester.doFinal(retValue, 0);
        return retValue;
    }

    private byte[] AddNetworkBytes(byte[] enterKey){

        byte[] networkByte = {(byte) 0x0 };
        byte[] newByteArray = new byte[networkByte.length + enterKey.length];
        System.arraycopy(networkByte, 0, newByteArray, 0, networkByte.length);
        System.arraycopy(enterKey, 0, newByteArray, networkByte.length, enterKey.length);

        return newByteArray;
    }

    private byte[] GrabFirstFourBytes(byte[] enterKey){

        byte[] firstFourBytes = new byte[4];

        for(int i = 0; i < firstFourBytes.length; i++){
            firstFourBytes[i] = enterKey[i];
        }

        return  firstFourBytes;
    }

    private byte[] AddSevenEndOfNetworkByte(byte[] firstFour, byte[] NetworkByteArray){

        byte[] newByteArray = new byte[NetworkByteArray.length + firstFour.length];
        System.arraycopy(NetworkByteArray, 0, newByteArray, 0, NetworkByteArray.length);
        System.arraycopy(firstFour, 0, newByteArray, NetworkByteArray.length, firstFour.length);

        return newByteArray;
    }





}
