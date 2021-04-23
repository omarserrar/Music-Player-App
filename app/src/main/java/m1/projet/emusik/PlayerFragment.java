package m1.projet.emusik;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.videolan.libvlc.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import m1.projet.emusik.Server.SessionException;
import m1.projet.emusik.Server.Song;
import m1.projet.emusik.client.ClientSession;

public class PlayerFragment extends Fragment implements View.OnClickListener {
    private Button buttonPlayPause;
    public EditText editTextSongURL;
    private boolean stopped = false;
    private Song song;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonPlayPause = (Button) view.findViewById(R.id.playPause);
        buttonPlayPause.setOnClickListener(this);

        ClientSession.getClientSession(view.getContext()).setPlayerFragment(this);

        Bundle bundle = this.getArguments();
        TextView songTitle = view.findViewById(R.id.songTitle);
        TextView songArtiste = view.findViewById(R.id.songArtiste);

        if(bundle != null && bundle.containsKey("song") && bundle.getSerializable("song") != null){
            song = (Song) bundle.getSerializable("song");
            Log.println(Log.INFO, "Song", "Song "+(song.title));
            songTitle.setText(song.title);
            songArtiste.setText(song.artist);
            String audioUrl = ClientSession.getClientSession(view.getContext()).playSong(song);
        }
        else{
            Log.println(Log.INFO, "Null Test", "Bundle "+(bundle==null));
        }

    }
    public void play(){

        buttonPlayPause.setBackgroundResource(R.drawable.pause);
    }
    public void pause(){
        buttonPlayPause.setBackgroundResource(R.drawable.play);
    }
    public void stop(){
        stopped = true;
        buttonPlayPause.setBackgroundResource(R.drawable.play);
    }

    @Override
    public void onClick(View v) {
        try{
            if(stopped){
                ClientSession.getClientSession(v.getContext()).playSong(song);
                stopped = false;
            }
            else if(ClientSession.getClientSession().isPlaying()){
                ClientSession.getClientSession().pause();
            }
            else {
                ClientSession.getClientSession().resume();
            }
        }
        catch (SessionException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
    }


}