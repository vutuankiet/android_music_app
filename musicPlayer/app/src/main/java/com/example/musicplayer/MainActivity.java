package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsSeekBar;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import phucdv.android.musichelper.MediaHelper;
import phucdv.android.musichelper.Song;

public class MainActivity extends AppCompatActivity {

    MusicController mMusicController;
    List<Song> mListSong;
    private ArrayList<String> mListSongView = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;
    private MediaPlayer mMediaPlayer;
    private boolean mIsLooping = false;
    private boolean mIsShuffle = false;
    private RelativeLayout relativeLayout;
    private ImageButton mBtnPlayControl;
    private ImageButton mBtnPauseControl;
    private ImageButton mBtnNextControl;
    private ImageButton mBtnPrevControl;
    private ImageButton mBtnLoopControl;
    private ImageButton mBtnShuffleControl;
    private ImageButton mBtnPlay;
    private ImageButton mBtnPause;
    private TextView mSongTitleTextview;
    private TextView mSongArtistTextview;
    private TextView mCurrentTime;
    private TextView mMaxTime;
    private SeekBar mMusicSeekBar;
    RelativeLayout mMusicControlLayout;
    // Khai báo biến mHandler và mRunnable
    private Handler mHandler;
    private Runnable mRunnable;
    private int mIndexSong;
    private int mCurrentSongIndex;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mHandler = new Handler();

        // Ánh xạ RelativeLayout
        View musicControlView = LayoutInflater.from(this).inflate(R.layout.layout_music_control, null);

        // Get reference to the music control layout and hide it initially
        mMusicControlLayout = musicControlView.findViewById(R.id.layout_music_control);
        mMusicControlLayout.setVisibility(View.INVISIBLE);

        mSongTitleTextview = musicControlView.findViewById(R.id.song_title_textview);
        mSongArtistTextview = musicControlView.findViewById(R.id.song_artist_textview);
        mMusicSeekBar = musicControlView.findViewById(R.id.music_seekbar);
        mBtnPlayControl = musicControlView.findViewById(R.id.play_song_btn_control);
        mBtnPauseControl = musicControlView.findViewById(R.id.pause_song_btn_control);
        mCurrentTime = musicControlView.findViewById(R.id.song_current_time);
        mMaxTime = musicControlView.findViewById(R.id.song_max_time);
        mBtnPlay = findViewById(R.id.play_song_btn);
        mBtnPause = findViewById(R.id.pause_song_btn);
        mBtnNextControl = musicControlView.findViewById(R.id.next_button_control);
        mBtnPrevControl = musicControlView.findViewById(R.id.prev_button_control);
        mBtnLoopControl = musicControlView.findViewById(R.id.loop_button_control);
        mBtnShuffleControl = musicControlView.findViewById(R.id.shuffle_button_control);

        // Add the music control view to the main content layout
        FrameLayout contentLayout = findViewById(R.id.layout_main_control);
        contentLayout.addView(musicControlView);


        mListSong = new ArrayList<>();
        mMusicController = new MusicController(this, new MusicController.MusicSource() {
            @Override
            public int getSize() {
                return mListSong.size();
            }

            @Override
            public Song getAtIndex(int index) {
                return mListSong.get(index);
            }
        });
        mMediaPlayer = mMusicController.getmMediaPlayer();
        doRetrieveAllSong();


        mMusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                    mMusicController.start();
                    mMusicControlLayout.setVisibility(View.VISIBLE);
                    mBtnPlayControl.setVisibility(View.INVISIBLE);
                    mBtnPauseControl.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });






////        mMusicController.playSongAt(this, 1);
    }

    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 999);
            return false;
        } else {
            doRetrieveAllSong();
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 999) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                doRetrieveAllSong();
            }
        }
    }

    private void doRetrieveAllSong() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            checkPermission();
            return;
        }
        MediaHelper.retrieveAllSong(this, new MediaHelper.OnFinishRetrieve() {
            @Override
            public void onFinish(List<Song> list) {
                // Receiver result
                // Do something here

                for (int i = 0; i < list.size(); i++) {
                    mListSongView.add(list.get(i).getTitle());
                    Log.d("TAG", "onFinish: " + list.get(i).getAlbumUri());

                }
                // Get a handle to the RecyclerView.
                mRecyclerView = findViewById(R.id.rcv_song);
// Create an adapter and supply the data to be displayed.
                mAdapter = new SongListAdapter(MainActivity.this, list);
// Connect the adapter with the RecyclerView.
                mRecyclerView.setAdapter(mAdapter);
// Give the RecyclerView a default layout manager.
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mListSong.addAll(list);


//                mMusicController.playSongAt(MainActivity.this, 1);

                // play click
                mAdapter.setOnPlayClickListener((v, pos) -> {

                    // neu no khong play cai nao thi minh lay cai hien tai
                    if (mIndexSong == list.get(pos).getId()) {
                        mMusicController.start();
                        mMusicControlLayout.setVisibility(View.VISIBLE);
                        mBtnPlayControl.setVisibility(View.INVISIBLE);
                        mBtnPauseControl.setVisibility(View.VISIBLE);
                        mRecyclerView.setClipToPadding(true);
                        mRecyclerView.setPadding(0, 0, 0, 420);
                        mRecyclerView.setPaddingRelative(0, 0, 0, 420);


                        mSongTitleTextview.setText(list.get(pos).getTitle());
                        mSongArtistTextview.setText(list.get(pos).getArtist());
                        Log.d("TAG_t", "onFinish: " + mSongTitleTextview.getText());
                        updateSeekBar();
                    } else {

                        mMusicController.playSongAt(MainActivity.this, pos);
                        mMusicControlLayout.setVisibility(View.VISIBLE);
                        mBtnPlayControl.setVisibility(View.INVISIBLE);
                        mBtnPauseControl.setVisibility(View.VISIBLE);
                        mRecyclerView.setClipToPadding(true);
                        mRecyclerView.setPadding(0, 0, 0, 420);
                        mRecyclerView.setPaddingRelative(0, 0, 0, 420);


                        mSongTitleTextview.setText(list.get(pos).getTitle());
                        mSongArtistTextview.setText(list.get(pos).getArtist());
                        Log.d("TAG_t", "onFinish: " + mSongTitleTextview.getText());
                        mAdapter.setSelectedItem(pos);
                        mAdapter.notifyItemChanged(pos);
                        updateSeekBar();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if(mIsLooping == false && mIsShuffle == false && mMediaPlayer.getCurrentPosition() > 0){
                                    Log.d("TAG_t_stop", "onFinish: ");
                                    // Tăng chỉ số của bài hát hiện tại
                                    mCurrentSongIndex = mMusicController.getCurrentIndex();
                                    mCurrentSongIndex++;

                                    // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
                                    if (mCurrentSongIndex >= mListSong.size()) {
//                                        Collections.shuffle(mListSong);
                                        mCurrentSongIndex = 0;
                                    }

                                    // Lấy bài hát tiếp theo trong danh sách và phát nó
                                    mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
                                    mSongTitleTextview.setText(list.get(mCurrentSongIndex).getTitle());
                                    mSongArtistTextview.setText(list.get(mCurrentSongIndex).getArtist());
                                    mAdapter.setSelectedItem(mCurrentSongIndex);
                                    mAdapter.notifyItemChanged(mCurrentSongIndex);
                                }
                                if(mIsLooping == true){
                                    Log.d("TAG_t_stop_loop", "onFinish: ");
                                    mMediaPlayer.setLooping(true);
                                    mIsLooping = true;
                                }
                            }
                        });



//                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                            @Override
//                            public void onCompletion(MediaPlayer mediaPlayer) {
//
//                                mCurrentSongIndex++;
//
//                                // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
//                                if (mCurrentSongIndex >= mListSong.size()) {
//                                    mCurrentSongIndex = 0;
//                                }
//
//                                // Lấy bài hát tiếp theo trong danh sách và phát nó
//                                mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
//                                mMusicControlLayout.setVisibility(View.VISIBLE);
//                                mBtnPlayControl.setVisibility(View.INVISIBLE);
//                                mBtnPauseControl.setVisibility(View.VISIBLE);
//                                mRecyclerView.setClipToPadding(true);
//                                mRecyclerView.setPadding(0, 0, 0, 420);
//                                mRecyclerView.setPaddingRelative(0, 0, 0, 420);
//
//
//                                mSongTitleTextview.setText(list.get(pos).getTitle());
//                                Log.d("TAG_t", "onFinish: " + mSongTitleTextview.getText());
//                                updateSeekBar();
//                                Toast.makeText(MainActivity.this, "Auto", Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
//                        mMusicController.playSongAt(MainActivity.this, pos);

                    }

                });

                // ko nen de event nhu nay ne
                mAdapter.setOnPauseClickListener((v, pos) -> {
                    if (mMediaPlayer.isPlaying()) {
                        Log.d("TAG_progress", "onFinish: " + mMusicSeekBar.getProgress());
                        mIndexSong = (int) list.get(pos).getId();
                    }
                    mMusicController.pause();
                    mBtnPlayControl.setVisibility(View.VISIBLE);
                    mBtnPauseControl.setVisibility(View.INVISIBLE);
                    mRecyclerView.setClipToPadding(true);
                    mRecyclerView.setPadding(0, 0, 0, 420);
                    mRecyclerView.setPaddingRelative(0, 0, 0, 420);
//                  mBtnPause.setImageResource(R.drawable.ic_pause_song);
//                    mMusicController.setLooping(mIsLooping); // Thiết lập chế độ lặp lại bài hát

                    stopUpdatingSeekBar();
                });

                mBtnPlayControl.setOnClickListener(v -> {
                    mMusicController.start();
                    mBtnPlayControl.setVisibility(View.INVISIBLE);
                    mBtnPauseControl.setVisibility(View.VISIBLE);

                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(mIsLooping == false && mIsShuffle == false && mMediaPlayer.getCurrentPosition() > 0){
                                Log.d("TAG_t_stop", "onFinish: ");
                                // Tăng chỉ số của bài hát hiện tại
                                mCurrentSongIndex = mMusicController.getCurrentIndex();
                                mCurrentSongIndex++;

                                // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
                                if (mCurrentSongIndex >= mListSong.size()) {
                                    Collections.shuffle(mListSong);
                                    mCurrentSongIndex = 0;
                                }

                                // Lấy bài hát tiếp theo trong danh sách và phát nó
                                mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
                                mSongTitleTextview.setText(list.get(mCurrentSongIndex).getTitle());
                                mSongArtistTextview.setText(list.get(mCurrentSongIndex).getArtist());
                                mAdapter.setSelectedItem(mCurrentSongIndex);
                                mAdapter.notifyItemChanged(mCurrentSongIndex);
                            }
                            if(mIsLooping == true){
                                Log.d("TAG_t_stop_loop", "onFinish: ");
                                mMediaPlayer.setLooping(true);
                                mIsLooping = true;
                            }
                        }
                    });

//            mRecyclerView.setAdapter(mAdapter);
                });
                mBtnPauseControl.setOnClickListener(v -> {
                    mMusicController.pause();
                    mBtnPlayControl.setVisibility(View.VISIBLE);
                    mBtnPauseControl.setVisibility(View.INVISIBLE);

                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(mIsLooping == false && mIsShuffle == false && mMediaPlayer.getCurrentPosition() > 0){
                                Log.d("TAG_t_stop", "onFinish: ");
                                // Tăng chỉ số của bài hát hiện tại
                                mCurrentSongIndex = mMusicController.getCurrentIndex();
                                mCurrentSongIndex++;

                                // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
                                if (mCurrentSongIndex >= mListSong.size()) {
                                    Collections.shuffle(mListSong);
                                    mCurrentSongIndex = 0;
                                }

                                // Lấy bài hát tiếp theo trong danh sách và phát nó
                                mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
                                mSongTitleTextview.setText(list.get(mCurrentSongIndex).getTitle());
                                mSongArtistTextview.setText(list.get(mCurrentSongIndex).getArtist());
                                mAdapter.setSelectedItem(mCurrentSongIndex);
                                mAdapter.notifyItemChanged(mCurrentSongIndex);
                            }
                            if(mIsLooping == true){
                                Log.d("TAG_t_stop_loop", "onFinish: ");
                                mMediaPlayer.setLooping(true);
                                mIsLooping = true;
                            }
                        }
                    });


                });
                mBtnNextControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMusicController.playNext();
                        int pos = mMusicController.getCurrentIndex();
                        mAdapter.setSelectedItem(pos);
                        mSongTitleTextview.setText(list.get(pos).getTitle());
                        mSongArtistTextview.setText(list.get(pos).getArtist());
                        mBtnPlayControl.setVisibility(View.INVISIBLE);
                        mBtnPauseControl.setVisibility(View.VISIBLE);
//                        mIsLooping = true;
//                        mMusicController.setLooping(mIsLooping); // Thiết lập chế độ lặp lại bài hát
                        // Update the music control layout with the details of the song being played

                        mAdapter.notifyItemChanged(pos);
                        updateSeekBar();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if(mIsLooping == false && mIsShuffle == false && mMediaPlayer.getCurrentPosition() > 0){
                                    Log.d("TAG_t_stop", "onFinish: ");
                                    // Tăng chỉ số của bài hát hiện tại
                                    mCurrentSongIndex = mMusicController.getCurrentIndex();
                                    mCurrentSongIndex++;

                                    // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
                                    if (mCurrentSongIndex >= mListSong.size()) {
                                        Collections.shuffle(mListSong);
                                        mCurrentSongIndex = 0;
                                    }

                                    // Lấy bài hát tiếp theo trong danh sách và phát nó
                                    mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
                                    mSongTitleTextview.setText(list.get(mCurrentSongIndex).getTitle());
                                    mSongArtistTextview.setText(list.get(mCurrentSongIndex).getArtist());
                                    mAdapter.setSelectedItem(mCurrentSongIndex);
                                    mAdapter.notifyItemChanged(mCurrentSongIndex);
                                }
                                if(mIsLooping == true){
                                    Log.d("TAG_t_stop_loop", "onFinish: ");
                                    mMediaPlayer.setLooping(true);
                                    mIsLooping = true;
                                }
                            }
                        });

                    }
                });

                mBtnPrevControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("TAG_t_prev", "onClick: ");
                        mMusicController.playPrev();
                        int pos = mMusicController.getCurrentIndex();
                        mAdapter.setSelectedItem(pos);
                        mSongTitleTextview.setText(list.get(pos).getTitle());
                        mSongArtistTextview.setText(list.get(pos).getArtist());
                        mBtnPlayControl.setVisibility(View.INVISIBLE);
                        mBtnPauseControl.setVisibility(View.VISIBLE);
//                        mIsLooping = true;
//                        mMusicController.setLooping(mIsLooping); // Thiết lập chế độ lặp lại bài hát
                        mAdapter.notifyItemChanged(pos);
                        updateSeekBar();

                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if(mIsLooping == false && mIsShuffle == false && mMediaPlayer.getCurrentPosition() > 0){
                                    Log.d("TAG_t_stop", "onFinish: ");
                                    // Tăng chỉ số của bài hát hiện tại
                                    mCurrentSongIndex = mMusicController.getCurrentIndex();
                                    mCurrentSongIndex++;

                                    // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
                                    if (mCurrentSongIndex >= mListSong.size()) {
                                        Collections.shuffle(mListSong);
                                        mCurrentSongIndex = 0;
                                    }

                                    // Lấy bài hát tiếp theo trong danh sách và phát nó
                                    mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
                                    mSongTitleTextview.setText(list.get(mCurrentSongIndex).getTitle());
                                    mSongArtistTextview.setText(list.get(mCurrentSongIndex).getArtist());
                                    mAdapter.setSelectedItem(mCurrentSongIndex);
                                    mAdapter.notifyItemChanged(mCurrentSongIndex);
                                }
                                if(mIsLooping == true){
                                    Log.d("TAG_t_stop_loop", "onFinish: ");
                                    mMediaPlayer.setLooping(true);
                                    mIsLooping = true;
                                }
                            }
                        });

                    }
                });

                mBtnLoopControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mIsLooping) {
                            mMediaPlayer.setLooping(true);
                            mIsLooping = true;
                            mBtnLoopControl.setImageResource(R.drawable.loop_purple_700);
                            // Hiển thị thông báo cho người dùng biết rằng đã thoát chế độ lặp lại bài hát
                            Toast.makeText(MainActivity.this, "Looping enabled", Toast.LENGTH_SHORT).show();
                            Log.d("TAG_t_loop_else", "onClick: ");
                        } else {
                            mMediaPlayer.setLooping(false);
                            mIsLooping = false;
                            mBtnLoopControl.setImageResource(R.drawable.loop_gray);
                            // Hiển thị thông báo cho người dùng biết rằng đã thoát chế độ lặp lại bài hát
                            Toast.makeText(MainActivity.this, "Looping disabled", Toast.LENGTH_SHORT).show();
                            Log.d("TAG_t_loop_else", "onClick: ");

                        }
                        Log.d("TAG_finish", "onFinish: ");

                    }
                });

                mBtnShuffleControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mIsShuffle) {
                            playRandomSong(true);
                            Log.d("TAG_t_Sh", "onFinish: ");

                            mIsShuffle = true;
                            mBtnShuffleControl.setImageResource(R.drawable.shuffle_purple_700);
                            // Hiển thị thông báo cho người dùng biết rằng đang ở chế độ phát ngẫu nhiên bài hát
                            Toast.makeText(MainActivity.this, "Shuffle enabled", Toast.LENGTH_SHORT).show();
                            Log.d("TAG_finish", "onFinish: ");
                        }
                        else {
                            playRandomSong(false);

                            Log.d("TAG_t_Sh_else", "onFinish: ");

                            mIsShuffle = false;
                            mBtnShuffleControl.setImageResource(R.drawable.shuffle_gray);
                            // Hiển thị thông báo cho người dùng biết rằng đã thoát chế độ phát ngẫu nhiên bài hát
                            Toast.makeText(MainActivity.this, "Shuffle disabled", Toast.LENGTH_SHORT).show();
                        }
                    }
//
                });



            }
        });
    }

    private void playRandomSong(boolean random) {
        if(random){
            // Thiết lập Listener cho sự kiện kết thúc bài hát
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Random random = new Random();
                    int randomPosition = random.nextInt(mListSong.size());
                    // Lấy bài hát tiếp theo trong danh sách và phát nó
                    mMusicController.playSongAt(MainActivity.this, randomPosition);
                    mSongTitleTextview.setText(mListSong.get(randomPosition).getTitle());
                    mSongArtistTextview.setText(mListSong.get(randomPosition).getArtist());
                    mBtnPlayControl.setVisibility(View.INVISIBLE);
                    mBtnPauseControl.setVisibility(View.VISIBLE);
                    mAdapter.setSelectedItem(randomPosition);
                    mAdapter.notifyItemChanged(randomPosition);
                    Toast.makeText(MainActivity.this, "Random", Toast.LENGTH_SHORT).show();
                    Log.d("TAG_t_auto_sh", "onFinish: " + mSongTitleTextview.getText());

                }
            });
        }else{
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d("TAG_t_stop", "onFinish: ");
                    mBtnPlayControl.setVisibility(View.VISIBLE);
                    mBtnPauseControl.setVisibility(View.INVISIBLE);
                    if(mMediaPlayer.isLooping() == true){
                        mBtnPlayControl.setVisibility(View.INVISIBLE);
                        mBtnPauseControl.setVisibility(View.VISIBLE);
                        Log.d("TAG_t_loop", "onFinish: ");
                    }
                }
            });
        }

//            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    mCurrentSongIndex++;
//
//                    // Nếu đã phát hết danh sách bài hát thì trộn lại và phát bài hát đầu tiên
//                    if (mCurrentSongIndex >= mListSong.size()) {
//                        mCurrentSongIndex = 0;
//                    }
//
//                    // Lấy bài hát tiếp theo trong danh sách và phát nó
//                    mMusicController.playSongAt(MainActivity.this, mCurrentSongIndex);
//                    mSongTitleTextview.setText(mListSong.get(mCurrentSongIndex).getTitle());
//                    mBtnPlayControl.setVisibility(View.INVISIBLE);
//                    mBtnPauseControl.setVisibility(View.VISIBLE);
//                    mAdapter.setSelectedItem(mCurrentSongIndex);
//                    mAdapter.notifyItemChanged(mCurrentSongIndex);
//                    Toast.makeText(MainActivity.this, "Auto", Toast.LENGTH_SHORT).show();
//
//                }
//            });

    }

    public static String formatTime(int timeInSeconds) {
        int minutes = (timeInSeconds / 1000) / 60;
        int seconds = (timeInSeconds / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    //     Phương thức cập nhật seekbar
    private void updateSeekBar() {
//        Log.d("TAG_Duration", "onFinish: " + mMusicController.getmMediaPlayer().getDuration());
        mMusicSeekBar.setMax(mMusicController.getmMediaPlayer().getDuration());
        int currPos = mMediaPlayer.getCurrentPosition();
        mMusicSeekBar.setProgress(currPos);
        mCurrentTime.setText(formatTime(currPos));
        mMaxTime.setText(formatTime(mMusicSeekBar.getMax()));

//        Log.d("TAG_currPos", "onFinish: " + currPos);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    private void stopUpdatingSeekBar() {
        mHandler.removeCallbacks(MainActivity.this::updateSeekBar);
    }
}