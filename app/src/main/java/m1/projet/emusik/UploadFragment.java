package m1.projet.emusik;


import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.zeroc.Ice.InvocationFuture;
import com.zeroc.Ice.Util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import m1.projet.emusik.Server.Song;
import m1.projet.emusik.client.ClientSession;

public class UploadFragment extends Fragment {


    private Button uploadSongButton;
    private Button addSongButton;

    private EditText titleEditText;
    private EditText artisteEditText;

    private TextView uploadedSongTitle;

    private TextView enterTitleError;
    private TextView enterArtisteError;
    private TextView songAlreadyExistError;
    private TextView noSongUploadedError;

    private View uploadingZone;
    private ImageView uploadSuccess;
    private View uploadProgressBar;
    private TextView uploadingMessage;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.upload_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uploadSongButton = view.findViewById(R.id.uploadSongButton);
        addSongButton = view.findViewById(R.id.addSongButton);

        titleEditText = view.findViewById(R.id.titleEditText);
        artisteEditText = view.findViewById(R.id.artisteEditText);

        uploadedSongTitle = view.findViewById(R.id.uploadedSongTitle);

        enterTitleError = view.findViewById(R.id.enterTitleError);
        enterArtisteError = view.findViewById(R.id.enterArtisteError);
        songAlreadyExistError = view.findViewById(R.id.songAlreadyExistError);
        noSongUploadedError = view.findViewById(R.id.noSongUploadedError);

        uploadingMessage = view.findViewById(R.id.uploadingMessage);
        uploadingZone = view.findViewById(R.id.uploadingZone);
        uploadSuccess = view.findViewById(R.id.uploadSuccess);
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar);

        uploadingZone.setVisibility(View.GONE);
        uploadSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");


                startActivityForResult(intent, 1);
            }
        });

        addSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickUpload();
            }
        });
    }

    private Uri selectedSongUri;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != 0 && data != null){
            selectedSongUri = data.getData();
            uploadedSongTitle.setText(getFileName(selectedSongUri));
        }

    }

    public String getFileName(Uri uri) {
        String filename = "";
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return filename;
    }
    public void sendFile(Uri fileUri){

    }
    public void onClickUpload(){
        boolean error=false;
        String title = titleEditText.getText().toString().trim();
        String artiste = artisteEditText.getText().toString().trim();

        if(title.isEmpty()){    // Verifie si le titre est vide
            error = true;
            enterTitleError.setVisibility(View.VISIBLE);
        } else enterTitleError.setVisibility(View.GONE);

        if(artiste.isEmpty()){ // Verifie si l'artiste est vide
            error = true;
            enterArtisteError.setVisibility(View.VISIBLE);
        }else enterArtisteError.setVisibility(View.GONE);

        if(selectedSongUri == null){ // Verifie si l'utilisateur a choisi un fichier
            error = true;
            noSongUploadedError.setVisibility(View.VISIBLE);
        } else noSongUploadedError.setVisibility(View.GONE);

        if(!error){ // Si 0 Erreur Cherche si la musique existe
            ClientSession clientSession = ClientSession.getClientSession();
            Song song = clientSession.getStreamServerPrx().getSong(title, artiste, clientSession.getSession());

            if(song.id != 0){  // Si la musique existe deja afficher erreur
                error = true;
                songAlreadyExistError.setVisibility(View.VISIBLE);
            } else songAlreadyExistError.setVisibility(View.GONE);
        }

        if(!error){ // Si 0 Erreur Upload file

            new UploadTask(selectedSongUri, title, artiste).execute();
        }
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
    }
   /* public void readFile(Uri fileUri){
        try {
            long l = getContext().getContentResolver().openFileDescriptor(fileUri,"r").getStatSize();
            InputStreamReader inputStream = new InputStreamReader(getContext().getContentResolver().openInputStream(fileUri));


            int length = 0;
            String line = "";
            while((line = br.readLine())!= null){
                byte[] bytes = line.getBytes();
                length += bytes.length;
            }

            Log.println(Log.INFO, "up", l+" "+length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    class UploadTask extends AsyncTask<Void, Void, Boolean> {

        Uri fileUri;
        String title, artist;
        UploadTask(Uri fileUri, String title, String artist){
            this.fileUri = fileUri;
            this.title = title;
            this.artist =artist;
        }
        protected void onPreExecute() {
            uploadSuccess.setVisibility(View.GONE);
            uploadProgressBar.setVisibility(View.VISIBLE);

            uploadingZone.setVisibility(View.VISIBLE);
            String message = "Uploading "+title+" - "+artist;
            uploadingMessage.setText(message);

            titleEditText.setEnabled(false);
            artisteEditText.setEnabled(false);

            addSongButton.setEnabled(false);
            uploadSongButton.setEnabled(false);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            try{

                ClientSession clientSession = ClientSession.getClientSession();
                long l = getContext().getContentResolver().openFileDescriptor(fileUri,"r").getStatSize();
                FileDescriptor fileDescriptor = getContext().getContentResolver().openFileDescriptor(fileUri, "r").getFileDescriptor();
                InputStream br = getContext().getContentResolver().openInputStream(fileUri);
                char[] myBuffer = new char[60656];
                int offset = 0;
                int length = 0;
                LinkedList<InvocationFuture<Boolean>> results = new LinkedList<InvocationFuture<Boolean>>();
                int numRequests = 5;
                Log.println(Log.INFO, "Send", "Begin");
                byte[] bytes = new byte[65565];
                while((length = br.read(bytes))>0)
                {

                    // Send up to numRequests + 1 chunks asynchronously.
                    CompletableFuture<Boolean> f = clientSession.getStreamServerPrx().transferAsync(title, artist, "omar", offset, bytes, 0, clientSession.getSession());
                    offset += length;

                    // Wait until this request has been passed to the transport.
                    InvocationFuture<Boolean> i = Util.getInvocationFuture(f);
                    i.waitForSent();
                    results.add(i);

                    // Once there are more than numRequests, wait for the least
                    // recent one to complete.
                    while(results.size() > numRequests)
                    {
                        i = results.getFirst();
                        results.removeFirst();
                        i.join();
                    }
                }
                CompletableFuture<Boolean> f = clientSession.getStreamServerPrx().transferAsync(title, artist, "omar", offset, new byte[]{}, 1, clientSession.getSession());
                InvocationFuture<Boolean> i = Util.getInvocationFuture(f);
                i.waitForSent();
                results.add(i);
                Log.println(Log.INFO, "Send", "Two");
                // Wait for any remaining requests to complete.
                while(results.size() > 0)
                {
                    i = results.getFirst();
                    results.removeFirst();
                    i.join();
                }
                Log.println(Log.INFO, "Send", "End");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            String message = title+" - "+artist+" Uploaded";
            uploadSuccess.setVisibility(View.VISIBLE);
            uploadProgressBar.setVisibility(View.GONE);

            uploadingMessage.setText(message);
            titleEditText.setEnabled(true);
            artisteEditText.setEnabled(true);

            addSongButton.setEnabled(true);
            uploadSongButton.setEnabled(true);

            titleEditText.setText("");
            artisteEditText.setText("");

            selectedSongUri = null;
            uploadedSongTitle.setText("");
        }

    }
}

