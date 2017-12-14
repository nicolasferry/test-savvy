package org.testsavvy.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by ferrynico on 14/12/2017.
 */
public class main {

    public static void main(String[] args){
        SavvyConnector connector=new SavvyConnector("https://api-soraluce.savvyds.com");
        JSONArray allLocations=connector.getLocationAsJSON();
        JSONObject firstLocation= (JSONObject) allLocations.get(0);

        JSONArray allMachines=connector.getMachinesAsJSON(firstLocation.get("locationId").toString());
        JSONObject firstMachine= (JSONObject) allMachines.get(0);

        System.out.println(connector.getGroups(firstLocation.get("locationId").toString(),firstMachine.get("machineId").toString()));

        InputStream s=connector.connectStream(firstMachine.get("machineId").toString());

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s));
            String inputLine;
            int i = 0;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }


    }

}
