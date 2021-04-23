package m1.projet.emusik.client;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import m1.projet.emusik.AsrController;
import m1.projet.emusik.HomeFragment;
import m1.projet.emusik.MainActivity;
import m1.projet.emusik.PlayerFragment;
import m1.projet.emusik.R;
import m1.projet.emusik.Server.SearchResult;
import m1.projet.emusik.Server.SessionException;
import m1.projet.emusik.Server.Song;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TalClient {
    final static String url = "http://192.168.1.58:10002/query";
    private  ClientSession clientSession;
    private AsrController asrController;
    public TalClient(ClientSession clientSession, AsrController asrController){
        this.clientSession = clientSession;
        this.asrController = asrController;
    }
    public void query(String query){
        new RequestTask().execute(query);
    }

    public void queryProcess(String command, String arg) throws SessionException {
        if(command != null)
            switch (command){
                case "play": {
                    if(arg!= null && ! arg.trim().isEmpty()){
                        SearchResult[] searchResult = clientSession.getStreamServerPrx().searchTitle(arg, clientSession.getSession());
                        if(searchResult != null && searchResult.length > 0){
                            Song song = searchResult[0].song;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("song", song);
                            Log.d("Song", "Playing "+song.title);

                            MainActivity mainActivity = ((MainActivity)asrController.resultText.getContext());
                            mainActivity.addFragment(R.id.PlayerFragment, bundle);
                        }
                    }
                    break;
                }
                case "pause": {
                    ClientSession.getClientSession().pause();
                    break;
                }
                case "stop": {
                    ClientSession.getClientSession().stop();
                    break;
                }
                case "resume": {
                    ClientSession.getClientSession().resume();
                    break;
                }
            }
    }
    class RequestTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder().add("query", args[0]).build();
            Request request = new Request.Builder()
                    .url(TalClient.url)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {

                return new JSONObject(response.body().string());

            } catch (IOException | JSONException e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if(result != null && result.has("command") && result.has("arg")){
                try {
                    String command = result.getString("command");
                    String arg = result.getString("arg");
                    queryProcess(command, arg);
                    asrController.printTalResult(command, arg);
                } catch (JSONException | SessionException e) {
                    asrController.printTalResult("Error", "");
                    e.printStackTrace();
                }
            }

        }


    }
}

