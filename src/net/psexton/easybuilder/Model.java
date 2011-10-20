/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.psexton.easybuilder;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JTextArea;

/**
 *
 * @author PSexton
 */
public class Model implements PacketListener {
    private HashMap<String, String> buttonActions;
    private XBee xbee;
    private JTextArea console;
    
    public Model(JTextArea console) {
        this.buttonActions = new HashMap<String, String>();
        this.xbee = new XBee();
        this.console = console;
    }
    
    public void connect(String portName) {
        try {
            xbee.open(portName, 9600);
            
            console.append("Connected to " + portName + "\n"); 
        
            // Add PacketListener
            xbee.addPacketListener(this);
        } 
        catch (XBeeException ex) {
            console.append(ex.getLocalizedMessage());
        }
    }
    
    public void disconnect() {
        xbee.removePacketListener(this);
        xbee.close();
    }
    
    public void setActions(Map<String, String> buttonActions) {
        this.buttonActions.clear();
        for(Entry<String, String> entry : buttonActions.entrySet()) {
            String buttonId = entry.getKey();
            String url = entry.getValue();
            this.buttonActions.put(buttonId, url);
        }
    }
    
    public Map<String, String> getActions() {
        // Make defensive copy
        return new HashMap<String, String>(buttonActions);
    }

    @Override
    public void processResponse(XBeeResponse response) {
        // handle the response
        if (response.getApiId() == ApiId.RX_16_IO_RESPONSE || response.getApiId() == ApiId.RX_64_RESPONSE) {
            RxResponseIoSample ioSample = (RxResponseIoSample) response;
            XBeeAddress sourceAddress = ioSample.getSourceAddress();

            console.append("\n");
            console.append("Received a sample from " + sourceAddress + "\n");
            console.append("RSSI is " + ioSample.getRssi() + "\n");
            
            // Need to convert the address from the format used by the XBeeAddress class
            // Hex value of "CAFE" is encoded as "0xCA,0xFE"
            String sourceAddressString = sourceAddress.toString();
            String buttonIdByte1 = sourceAddressString.substring(2, 4);
            String buttonIdByte2 = sourceAddressString.substring(7, 9);
            String buttonId = buttonIdByte1 + buttonIdByte2;
            
            if(buttonActions.containsKey(buttonId)) {
                String url = buttonActions.get(buttonId);
                console.append("Identified button " + buttonId + ", sending request to " + url + "\n");
                sendHttpGetRequest(url);
            }
            else {
                console.append("Unidentified button " + buttonId + "\n");
            }
        }
    }
    
    private void sendHttpGetRequest(String url) {
        try {
            // We don't actually care about the response, so we don't do anything with the BufferedReader.
            // But it's needed so that Java will actually make the HTTP request.
            URLConnection connection = new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            in.close();
        } 
        catch(MalformedURLException ex) {
            console.append(ex.getLocalizedMessage());
        }
        catch(IOException ex) {
            console.append(ex.getLocalizedMessage());
        }
    }
}
