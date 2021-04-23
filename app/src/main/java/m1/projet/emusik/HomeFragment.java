package m1.projet.emusik;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeroc.IceInternal.Ex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.URISyntaxException;
import java.net.UnknownHostException;

import io.socket.emitter.Emitter;
import m1.projet.emusik.Server.SearchResult;
import m1.projet.emusik.Server.SessionException;
import m1.projet.emusik.Server.Song;
import m1.projet.emusik.client.AsrClient;
import m1.projet.emusik.client.ClientSession;


public class HomeFragment extends Fragment implements  SearchView.OnQueryTextListener {
    private FloatingActionButton addSong;
    private Song[] songs;
    private SearchView searchView;



    private Button startButton,stopButton;



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addSong = view.findViewById(R.id.addSongFloatingButton);

        addSong.setOnClickListener(v ->{
            //NavHostFragment.findNavController(HomeFragment.this)
                  //  .navigate(R.id.action_HomeFragment_to_UploadFragment)
            Fragment fragment = new UploadFragment();
            ((MainActivity)getContext()).addFragment(R.id.UploadFragment, null);
        } );



        showList();

    }

    public void showList(){
        ClientSession session = ClientSession.getClientSession(getContext());
        songs = null;
        try{
            songs = session.getSongs();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        resetList();
    }
    public void showSongList(Song[] songs, View view){
        RecyclerView rvContacts = (RecyclerView) view.findViewById(R.id.songsList);

        SongsAdapter adapter = new SongsAdapter(songs, this);
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvContacts.getContext(), layoutManager.getOrientation());
        rvContacts.setLayoutManager(layoutManager);
        rvContacts.addItemDecoration(dividerItemDecoration);
    }
    public Song[] search(String query){
        ClientSession clientSession = ClientSession.getClientSession();
        Song[] songs = null;
        Log.println(Log.INFO, "search", query);
        try {
            SearchResult[] searchResult = clientSession.getStreamServerPrx().searchTitle(query, clientSession.getSession());
            if(searchResult!=null){
                songs = new Song[searchResult.length];
                for(int i=0;i<searchResult.length;i++)
                    songs[i] = searchResult[i].song;
            }

        } catch (SessionException e) {
            e.printStackTrace();
        }
        return songs;
    }
    private boolean search = false;
    public void resetList(){
        showSongList(songs, getView());
        search = false;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        showSongList(search(query), getView());
        search = true;
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu.findItem(R.id.action_search)!=null){
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            searchView = (SearchView) searchMenuItem.getActionView();
            searchView.setOnQueryTextListener(this);
            MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    resetList();
                    return true;
                }
            });
        }

    }
    class RowHolder  extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        TextView artisteName =null;
        TextView songName = null;
        Fragment fragment;
        Song song;

        RowHolder(View row, Fragment fragment) {
            super(row);
            artisteName = (TextView) row.findViewById(R.id.artisteName);
            songName=(TextView)row.findViewById(R.id.songName);
            Log.println(Log.INFO, "Test", "Init "+(fragment==null));
            row.setOnClickListener(this);
            row.setOnCreateContextMenuListener(this);
            this.fragment = fragment;
        }


        void bindModel(Song song) {

            if (song != null) {
                artisteName.setText(song.artist);
                songName.setText(song.title);
                this.song = song;
            }
        }

        @Override
        public void onClick(View view) {
            Log.println(Log.INFO, "Test", "Click");
            Bundle bundle = new Bundle();
            bundle.putSerializable("song", this.song);
            ((MainActivity) songName.getContext()).addFragment(R.id.PlayerFragment, bundle);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem deleteButton = menu.add(0, v.getId(), 0, "Delete");//groupId, itemId, order, title
            deleteButton.setOnMenuItemClickListener(item -> {

                new AlertDialog.Builder(v.getContext())
                        .setTitle("")
                        .setMessage("Do you really want to delete "+song.title+"?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", (dialog, whichButton) ->{
                            try {
                                if(ClientSession.getClientSession().deleteSong(song)) {
                                    Toast.makeText(v.getContext(), "Song deleted! ", Toast.LENGTH_LONG).show();
                                    showList();
                                }
                                else
                                    Toast.makeText(v.getContext() , "Error could not delete the song", Toast.LENGTH_LONG).show();
                            } catch (SessionException e) {
                                Toast.makeText(v.getContext() , "Session Expired", Toast.LENGTH_LONG).show();
                            }
                        } )
                        .setNegativeButton("No", null).show();

                return true;
            });
        }


    }
    class SongsAdapter extends RecyclerView.Adapter<RowHolder> {
        private Song[] songs;
        private Fragment fragment;
        SongsAdapter(Song[] songs, Fragment fragment){
            this.songs = songs;
            this.fragment = fragment;
        }
        @Override
        public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row, parent, false);
            return (new RowHolder(view, fragment));

        }

        @Override
        public void onBindViewHolder(RowHolder holder, int position) {
            try {
                holder.bindModel(songs[position]);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if(songs == null) return 0;
            return songs.length;
        }
    }


}


