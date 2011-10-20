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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        // Convert the keys to the format used by the XBeeAddress class
        // hex value of "CAFE" is encoded as "0xCA,0xFE"
        this.buttonActions.clear();
        for(Entry<String, String> entry : buttonActions.entrySet()) {
            String buttonId = entry.getKey();
            String url = entry.getValue();
            
            String buttonIdByte1 = buttonId.substring(0, 2);
            String buttonIdByte2 = buttonId.substring(2, 4);
            buttonId = "0x" + buttonIdByte1 + ",0x" + buttonIdByte2;
            
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

            String sourceAddressString = sourceAddress.toString();
            if(buttonActions.containsKey(sourceAddressString)) {
                String url = buttonActions.get(sourceAddressString);
                console.append("Sending request to " + url + "\n");
                sendHttpGetRequest(url);
            }
            else {
                console.append("Unknown button\n");
            }
        }
    }
    
    private void sendHttpGetRequest(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            in.close();
        } 
        catch(MalformedURLException ex) {
            Logger.getLogger(EasyBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException ex) {
            Logger.getLogger(EasyBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
