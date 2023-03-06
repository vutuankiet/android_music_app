package com.example.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import phucdv.android.musichelper.Song;

public class MusicController {
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private int mCurrentIndex;
    private boolean mIsPreparing;
    private boolean mIsPaused;
    private boolean mIsCompleted;
    private MusicSource mMusicSource;
    private OnCompletionListener mListener;

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
    }

    public interface OnCompletionListener {
        void onCompletion();
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mListener = listener;
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (mListener != null) {
                mListener.onCompletion();
            }
            if (mMediaPlayer.getCurrentPosition() > 0) {
                mMediaPlayer.reset();
                playNext();
            }
        }
    };

    public interface MusicSource {
        int getSize();
        Song getAtIndex(int index);
    }

    public MusicController(Context context, MusicSource musicSource) {
        mContext = context;
        mMusicSource = musicSource;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mIsPreparing = false;
            }
        });

        mCurrentIndex = -1;
        mIsPreparing = false;
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mIsPaused = true;
            mIsCompleted = true;
        }
    }

    public void setLooping(boolean isLooping) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(isLooping);
        }
    }

    public MediaPlayer getmMediaPlayer(){
        return this.mMediaPlayer;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public boolean isPreparing(){
        return mIsPreparing;
    }

    public void playNext() {
        if(mMusicSource.getSize() != 0) {
            if (mCurrentIndex < mMusicSource.getSize() - 1) {
                mCurrentIndex++;
            } else {
                mCurrentIndex = 0;
            }
            playSongAt(mContext, mCurrentIndex);
        }
    }

    public void playPrev() {
        if(mMusicSource.getSize() != 0) {
            if (mCurrentIndex > 0) {
                mCurrentIndex--;
            } else {
                mCurrentIndex = mMusicSource.getSize() - 1;
            }
            playSongAt(mContext, mCurrentIndex);
        }
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void playSongAt(Context context, int index) {
        mMediaPlayer.reset();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mMusicSource.getAtIndex(index).getId());
        Log.d("TAG_trackUri", "playSongAt: "+trackUri);
        Log.d("TAG_context", "playSongAt: "+context);
        try {
            mMediaPlayer.setDataSource(context, trackUri);
            mCurrentIndex = index;
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error starting data source", e);
        }
        mMediaPlayer.prepareAsync();
        mIsPreparing = true;
    }
}
