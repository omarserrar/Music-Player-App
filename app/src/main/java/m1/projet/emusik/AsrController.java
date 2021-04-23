package m1.projet.emusik;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.zeroc.Ice.InvocationFuture;
import com.zeroc.Ice.Util;
import com.zeroc.IceInternal.Ex;

import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import at.markushi.ui.CircleButton;
import m1.projet.emusik.client.AsrClient;
import m1.projet.emusik.client.ClientSession;
import m1.projet.emusik.client.TalClient;

public class AsrController {
    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)*5;
    public byte[] buffer = new byte[minBufSize];;
    public MainActivity activity;
    CircleButton record, stop, valid;
    public TextView resultText, commandText;
    RecordTask recordTask;
    boolean recording = false;

    private String lastResult = null;
    private AsrClient asrClient;

    public AsrController(CircleButton record, CircleButton stop, CircleButton valid, TextView resultText, TextView commandText) {
        this.record = record;
        this.stop = stop;
        this.valid = valid;
        this.resultText = resultText;
        this.commandText = commandText;

    }

    void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);

        recordTask = new RecordTask();
        recordTask.execute();
    }

    public void printTalResult(String command, String arg){
        stopRecording();
        Log.d("t ", command+" "+arg);
        if(arg != null && ! arg.equals("null")){
            resultText.setText(arg);
        }
        else{
            resultText.setText("");
        }
        if(command != null && ! command.equals("null")){
            commandText.setText(command);
            commandText.setVisibility(View.VISIBLE);
        }

    }

    void valid(){
        if(lastResult != null)
            new TalClient(ClientSession.getClientSession(), this).query(lastResult);
    }
    void releaseMic(){
        asrClient.disconnect();
        recordTask.cancel(true);
        if(recorder!=null)
            recorder.release();
    }
    void stopRecording(){
        releaseMic();
        lastResult = null;
        if(resultText != null){
            resultText.setText("");

        }
        if(record != null && stop != null && valid != null){
            stop.setVisibility(View.GONE);
            valid.setVisibility(View.GONE);
            record.setVisibility(View.VISIBLE);
            commandText.setVisibility(View.GONE);
        }

    }

    public void onResult(JSONObject result){
        releaseMic();
        if(resultText != null && result !=null){
            try{
                lastResult = result.getString("text");
                resultText.setText(lastResult);
            }
            catch (Exception e){
                resultText.setText("An error occurred, please try again later");
            }
        }
        if(record != null && stop != null && valid != null){
            stop.setVisibility(View.VISIBLE);
            valid.setVisibility(View.VISIBLE);
            record.setVisibility(View.VISIBLE);
        }
    }

    AudioRecord recorder;
    class RecordTask extends AsyncTask<Void, Void, Void> {
        JSONObject result;
        boolean stop = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultText.setText("");
            AsrController.this.stop.setVisibility(View.VISIBLE);
            AsrController.this.record.setVisibility(View.GONE);
            commandText.setVisibility(View.GONE);

            asrClient = new AsrClient(AsrController.this, args -> {
                stop = true;
                try{
                    result = (JSONObject) args[0];
                }
                catch(Exception e){
                    result = null;
                }

            });
            asrClient.connect();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onResult(result);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            stopRecording();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stopRecording();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                recorder.startRecording();

                while(!isCancelled() && !stop) {
                    int size = recorder.read(buffer, 0, buffer.length);
                    Log.d("Recorder", ""+size);
                    asrClient.send(buffer);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
