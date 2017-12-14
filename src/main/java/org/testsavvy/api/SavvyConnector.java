package org.testsavvy.api;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by ferrynico on 14/12/2017.
 */
public class SavvyConnector {
    private static final Logger journal = Logger.getLogger(SavvyConnector.class.getName());
    private final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private final String endpoint;
    private JSONParser p;

    public SavvyConnector(String endpoint){
        System.setProperty("jsse.enableSNIExtension", "false");
        this.endpoint=endpoint;
        p =new JSONParser();
    }

    public String getLocations(){
        return callSavvy("/v1/locations/");
    }

    public JSONArray getLocationAsJSON(){
        String tmp=callSavvy("/v1/locations/");
        return convertAsJSONArray(tmp);
    }

    public String getMachines(String location){
        return callSavvy("/v1/locations/"+location+"/machines");
    }

    public JSONArray getMachinesAsJSON(String location){
        String tmp=getMachines(location);
        return convertAsJSONArray(tmp);
    }

    public String getGroups(String location, String machines){
        return callSavvy("/v1/locations/"+location+"/machines/"+machines+"/groups");
    }
    public JSONArray getGroupsAsJSON(String location, String machines){
        String tmp=getGroups(location,machines);
        return convertAsJSONArray(tmp);
    }

    public String getIndicators(String location, String machines, String group){
        return callSavvy("/v1/locations/"+location+"/machines/"+machines+"/groups/"+group+"/indicators");
    }

    public JSONArray getIndicatorsAsJSON(String location, String machines, String group){
        String tmp=getIndicators(location, machines, group);
        return convertAsJSONArray(tmp);
    }

    public InputStream connectStream(String machine){
        return getSavvyStream("/v1/stream?track="+machine);
    }

    private JSONArray convertAsJSONArray(String tmp){
        JSONArray result = null;
        try {
            result=(JSONArray) p.parse(tmp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] signBytes =mac.doFinal(data.getBytes("UTF-8"));
        String signature = Base64.encode(signBytes);
        return signature;
    }

    private HttpURLConnection prepareConnection(String loc) {
        URL url = null;
        try {
            url = new URL(this.endpoint + loc);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            long epoch = System.currentTimeMillis();
            connection.setRequestProperty("X-M2C-Sequence", epoch + "");
            connection.setDoOutput(true);

            String Request = "GET" + "\n"
                    + "text/plain; charset=UTF-8" + "\n"
                    + epoch + "\n"
                    + loc;

            String Authorization = "M2C" + " " + "xptbQCSLWL" + ":"
                    + calculateRFC2104HMAC(Request, "iY6PtqLBBbCgFaC69EAc");

            connection.setRequestProperty("Authorization", Authorization);

            return connection;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getSavvyStream(String loc){
        HttpURLConnection connection = prepareConnection(loc);
        InputStream st = null;
        try {
            st = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return st;
    }


    private String callSavvy(String loc) {
        HttpURLConnection connection = prepareConnection(loc);
        StringBuffer buffer = new StringBuffer();
        InputStream st = null;
        try {
            st = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(st));
            String inputLine;
            int i = 0;
            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

}
