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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author PSexton
 */
public class Model implements PacketListener {
    private Map<String, String> buttonActions;
    private XBee xbee;
    private JTextArea console;
    
    public Model(JTextArea console) {
        this.buttonActions = null;
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
        
    }
    
    public void setActions(Map<String, String> buttonActions) {
        
    }
    
    public Map<String, String> getActions() {
        return buttonActions;
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

            if (sourceAddress.toString().equals("0x50,0x01")) {
                console.append("Identified button #5001\n");
                sendHttpGetRequest("http://mc.speechbanana.com/stream/04F9C751962280/broadcast"); // My RFID tag
            } else {
                console.append("Unidentified button\n");
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
