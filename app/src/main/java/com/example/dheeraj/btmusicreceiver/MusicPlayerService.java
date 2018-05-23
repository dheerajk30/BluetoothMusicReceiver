package com.example.dheeraj.btmusicreceiver;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.dheeraj.btmusicreceiver.models.Song;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dheeraj on 17/5/18.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;

    private int songPosn;

    private final IBinder musicBind = new MusicBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }


    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }



    public void playSong(){
        player.reset();



        Song playSong = songs.get(songPosn);

        long currSong = playSong.get_id();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);



        Log.d("mainmenu", "playSong: the exception has occurred in playing"+trackUri);
        try {
            player.setDataSource(getApplicationContext(),trackUri);
        } catch (IOException e) {
            Log.d("mainmenu", "playSong: the exception has occurred in playing"+e.getMessage());
            e.printStackTrace();
        }
        player.prepareAsync();
    }



    public void playpause(){
        if(player.isPlaying()){
            player.pause();
/*
            Toast.makeText(getApplicationContext(), "Song Paused!", Toast.LENGTH_SHORT).show();
*/
        }
        else{

/*
            Toast.makeText(getApplicationContext(), "Song Playing!", Toast.LENGTH_SHORT).show();
*/
            playSong();
        }
    }

    public void pauseSong(){

    }

    public void playprevsong() {
        if (songPosn == 0) {
/*
            Toast.makeText(getApplicationContext(), "This is already the first song.", Toast.LENGTH_SHORT).show();
*/
        } else {

/*
            Toast.makeText(getApplicationContext(), "Playing previous song!", Toast.LENGTH_SHORT).show();
*/
            songPosn-=1;
            playSong();
        }
    }

    public void playnextsong() {
        if (songPosn == songs.size()-1) {
/*
            Toast.makeText(getApplicationContext(), "This is already the last song.", Toast.LENGTH_SHORT).show();
*/
        } else {

      /*      Toast.makeText(getApplicationContext(), "Playing next song!", Toast.LENGTH_SHORT).show();
      */      songPosn+=1;
            playSong();
        }
    }



    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        songPosn=0;
        player=new MediaPlayer();
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }


    public void setSong(int songPosn){
        this.songPosn=songPosn;
    }

    public class MusicBinder extends Binder{
        MusicPlayerService fetchService(){
            return MusicPlayerService.this;
        }
    }

}
