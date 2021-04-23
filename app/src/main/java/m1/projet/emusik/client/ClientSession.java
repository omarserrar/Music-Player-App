package m1.projet.emusik.client;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

import m1.projet.emusik.PlayerFragment;
import m1.projet.emusik.Server.SessionException;
import m1.projet.emusik.Server.Song;
import m1.projet.emusik.Server.StreamServerPrx;

public class ClientSession {
    private static ClientSession CLIENT = null;

    private String session;
    private StreamServerPrx streamServerPrx;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private boolean stopped = true;

    private PlayerFragment playerFragment;

    ClientSession(Context context){
        connectStreamServer(context);
    }
    private void connectStreamServer(Context context){
        try{
            libVLC = new LibVLC(context);
            mediaPlayer = new MediaPlayer(libVLC);
            com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(new String[]{"1"});

            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SongPlayer:default -h 192.168.1.58 -p 10000");

            streamServerPrx = StreamServerPrx.checkedCast(base);
            if(streamServerPrx == null)
            {
                throw new Error("Invalid proxy");
            }

            session = streamServerPrx.initClient("0");

            assert session != null;

            Log.println(Log.INFO, "Client", "Session ON: "+session);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ClientSession getClientSession(Context context){
        if(CLIENT != null)
            return CLIENT;
        CLIENT = new ClientSession(context);
        return CLIENT;
    }

    public static ClientSession getClientSession(){
        return CLIENT;
    }


    public String getSession() {
        return session;
    }

    public Song[] getSongs() throws SessionException {
        return streamServerPrx.getSongsList(session);
    }
    public String playSong(Song song){
        try {
            String audioUrl = streamServerPrx.playSong(song, session);
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.play(Uri.parse(audioUrl));
            if(playerFragment != null) playerFragment.play();
            return  audioUrl;
        } catch (SessionException e) {
            e.printStackTrace();
            return "Error";
        }
    }
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void pause() throws SessionException {
        if(isPlaying()) {
            streamServerPrx.pause(session);
            mediaPlayer.pause();
            if(playerFragment != null) playerFragment.pause();
        }

    }
    public void resume() throws SessionException {
        if(!isPlaying()){
            streamServerPrx.resume(session);
            mediaPlayer.play();
            if(playerFragment != null) playerFragment.play();
        }
    }
    public void stop() throws SessionException {
        if(isPlaying()){
            streamServerPrx.stop(session);
            mediaPlayer.stop();
            if(playerFragment != null) playerFragment.stop();
        }

    }
    public LibVLC getLibVLC() {
        return libVLC;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public StreamServerPrx getStreamServerPrx(){
        return streamServerPrx;
    }

    public PlayerFragment getPlayerFragment() {
        return playerFragment;
    }

    public void setPlayerFragment(PlayerFragment playerFragment) {
        this.playerFragment = playerFragment;
    }
    public boolean deleteSong(Song song) throws SessionException {
        streamServerPrx.deleteSong(song, session);
        return true;
    }

}
