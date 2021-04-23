package m1.projet.emusik.client;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import m1.projet.emusik.AsrController;

public class AsrClient {
    Socket socket;
    String result;
    AsrController asrController;

    public AsrClient(AsrController asrController, Emitter.Listener onDataReceived){
        this.asrController = asrController;
        this.onDataReceived = onDataReceived;
        try {
            socket = IO.socket("http://192.168.1.58:10001");
            socket.on("recognize", onDataReceived);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public boolean connect(){
        Log.d("Socket", "Connecting... ");
        if(!socket.connected()) {
            socket.connect();
            Log.d("Socket", "Connecting... "+socket.connected());
            result = "";
        }
        return socket.connected();
    }
    public void send(byte[] data){
        if(socket.connected()) {
            socket.emit("stream-data", data);
        }
    }
    public String disconnect(){
        if(socket.connected()){
            socket.disconnect();
        }
        return result;
    }
    public boolean isConnected(){
        return socket.connected();
    }
    Emitter.Listener onDataReceived;
}
