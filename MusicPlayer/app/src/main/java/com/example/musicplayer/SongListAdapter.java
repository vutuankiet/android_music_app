package com.example.musicplayer;

import android.content.Context;
import android.content.SyncContext;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import java.util.ArrayList;
import java.util.List;

import phucdv.android.musichelper.Song;

public class SongListAdapter extends
        RecyclerView.Adapter<SongListAdapter.SongViewHolder>{
    private final List<Song> mSongList;
    private LayoutInflater mInflater;
    private Animation currentAnimation;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private int selectedItem = -1;
    private Context mContext;
    private List<Integer> selectedItems = new ArrayList<>();


    boolean isPlaying = false; // biến lưu trạng thái hiện tại của Button
//    private MediaPlayer mMediaPlayer; // Khai báo MediaPlayer
//
    private OnPlayClickListener mOnPlayClickListener;
    private OnPauseClickListener mOnPauseClickListener;
    private OnNextClickListener mOnNextClickListener;

    public void setOnPlayClickListener(OnPlayClickListener listener){
        mOnPlayClickListener = listener;
    }

    public void setOnPauseClickListener(OnPauseClickListener listener){
        mOnPauseClickListener = listener;
    }

    public void setOnNextClickListener(OnNextClickListener listener){
        mOnNextClickListener = listener;
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        public final TextView songTitleView;
        public ImageButton playSongBtn;
        public ImageButton pauseSongBtn;
        public ImageView songImage;
        public TextView songArtist;

        final SongListAdapter mAdapter;

        private ConstraintLayout mConstraintLayout;

        public SongViewHolder(View itemView, SongListAdapter adapter) {
            super(itemView);
            songTitleView = itemView.findViewById(R.id.txt_song);
            playSongBtn = itemView.findViewById(R.id.play_song_btn);
            pauseSongBtn = itemView.findViewById(R.id.pause_song_btn);
            mConstraintLayout = itemView.findViewById(R.id.song);
            songImage = itemView.findViewById(R.id.song_image);
            songArtist = itemView.findViewById(R.id.txt_artist);


            this.mAdapter = adapter;

        }

    }

    public SongListAdapter(Context mContext,
                           List<Song> songList) {
        mInflater = LayoutInflater.from(mContext);
        this.mSongList = songList;
        this.mContext = mContext;
//        mMediaPlayer = new MediaPlayer(); // Khởi tạo MediaPlayer
    }


    @NonNull
    @Override
    public SongListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.song_list,
                parent, false);
        return new SongViewHolder(mItemView, this);
    }

    public void setSelectedItem(int position) {
        int previousSelectedItem = selectedItem;
        selectedItem = position;
        notifyItemChanged(previousSelectedItem);
        notifyItemChanged(selectedItem);
    }
    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.SongViewHolder holder, int position) {
        // Lấy thông tin bài hát từ list tại vị trí position
        final Song song = mSongList.get(position);
        String mCurrentText = song.getTitle();
        String mCurrentArtist = song.getArtist();
        int mSongIndex = position;
        holder.songTitleView.setText(mCurrentText);
        holder.songArtist.setText(mCurrentArtist);

        Log.d("TAG_image_uri", "onBindViewHolder: ");
//        try{
//            Uri mCurrentImage = mSongList.get(position).getAlbumUri();
//            Log.d("TAG_image_uri_1", "onBindViewHolder: "+mCurrentImage);
//
//            holder.songImage.setImageURI(mCurrentImage);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }

        Glide.with(mContext)
                .load(song.getAlbumUri())
                .error(R.drawable.music)
                .into(holder.songImage);
        Log.d("TAG_check_1", "onBindViewHolder: " + position);
        Log.d("TAG_check_1.1", "onBindViewHolder: " + selectedItem);
        if (position == selectedItem) {


            // Bắt đầu animation cho item được click
            holder.songTitleView.setTranslationX(0);

            holder.mConstraintLayout.setBackgroundColor(0xFF3700B3);
            holder.playSongBtn.setBackgroundColor(0xFF3700B3);
            holder.pauseSongBtn.setBackgroundColor(0xFF3700B3);
            // Ẩn Button "play" và hiển thị Button "pause"
            holder.playSongBtn.setVisibility(View.INVISIBLE);
            holder.pauseSongBtn.setVisibility(View.INVISIBLE);
            // Thực hiện animation trôi chữ
            Animation animation = AnimationUtils.loadAnimation(holder.songTitleView.getContext(), R.anim.text_slide);
            if (animation.hasStarted() && !animation.hasEnded()) {
                return;
            }
            animation.setAnimationListener(new Animation.AnimationListener() {
                // Nếu đang thực hiện animation thì không làm gì cả
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Lắng nghe sự kiện kết thúc animation và thực hiện lại animation từ đầu
                    holder.songTitleView.setTranslationX(0);
                    holder.songTitleView.startAnimation(animation);
                    holder.mConstraintLayout.setBackgroundColor(0xFF3700B3);
                    holder.playSongBtn.setBackgroundColor(0xFF3700B3);
                    holder.pauseSongBtn.setBackgroundColor(0xFF3700B3);
                    holder.playSongBtn.setVisibility(View.VISIBLE);
                    holder.pauseSongBtn.setVisibility(View.INVISIBLE);

//                        holder.playSongBtn.setBackgroundResource(R.drawable.ic_pause_song); // thay đổi hình ảnh của Button
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            // Lưu animation hiện tại cho item được click
            currentAnimation = animation;

            // Thiết lập animation
//                animation.setDuration((long) textWidth * 5);
            animation.setRepeatCount(Animation.INFINITE);
            holder.songTitleView.startAnimation(animation);
        }
        else {
            Log.d("TAG_check_2", "onBindViewHolder: ");
            holder.songTitleView.setTranslationX(0);
//                        holder.songTitleView.startAnimation(animation);
            holder.mConstraintLayout.setBackgroundColor(0xFFBB86FC);
            holder.playSongBtn.setBackgroundColor(0xFFBB86FC);
            holder.pauseSongBtn.setBackgroundColor(0xFFBB86FC);
            holder.songImage.setBackgroundColor(0xFFBB86FC);
            holder.playSongBtn.setVisibility(View.VISIBLE);
            holder.pauseSongBtn.setVisibility(View.INVISIBLE);
        }

        holder.playSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedItem(mSongIndex);
                if(mOnPlayClickListener != null){
                    mOnPlayClickListener.onClick(v, mSongIndex);
                }
                Log.d("TAG_pf", "onClick: "+isPlaying);

                isPlaying = !isPlaying;
                if (isPlaying) {
                    Log.d("TAG_pt", "onClick: "+isPlaying);

                    // Ẩn Button "play" và hiển thị Button "pause"
//                    holder.playSongBtn.setVisibility(View.INVISIBLE);
//                    holder.pauseSongBtn.setVisibility(View.VISIBLE);

                    // set maxLines to a large number to display the full text
                    holder.songTitleView.setMaxLines(Integer.MAX_VALUE);

                    // Dừng animation của các item khác
                    if (currentAnimation != null) {
                        currentAnimation.cancel();
                    }

                    isPlaying = false;

                    Log.d("TAG_p", "onClick: "+mSongIndex);
//                    mMusicController.playSongAt(SongListAdapter.class, 1);
                } else {

                    // reset maxLines to the original value
                    holder.songTitleView.setMaxLines(3);
                    // bắt đầu phát nhạc
                    holder.songTitleView.clearAnimation();
                }
//                // Save selected position
//                selectedPosition = holder.getAdapterPosition();
//
//                // Notify adapter of item change
//                notifyDataSetChanged();

            }
        });

        holder.pauseSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnPauseClickListener != null){
                    mOnPauseClickListener.onClick(v, mSongIndex);
                }

                // Thực hiện animation trôi chữ
                Animation animation = AnimationUtils.loadAnimation(holder.songTitleView.getContext(), R.anim.text_slide);
                if (animation.hasStarted() && !animation.hasEnded()) {
                    return;
                }
                animation.setAnimationListener(new Animation.AnimationListener() {
                    // Nếu đang thực hiện animation thì không làm gì cả
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Lắng nghe sự kiện kết thúc animation và thực hiện lại animation từ đầu
                        holder.songTitleView.setTranslationX(0);
//                        holder.songTitleView.startAnimation(animation);
                        holder.mConstraintLayout.setBackgroundColor(0xFF3700B3);
                        holder.playSongBtn.setBackgroundColor(0xFF3700B3);
                        holder.pauseSongBtn.setBackgroundColor(0xFF3700B3);
                        holder.songImage.setBackgroundColor(0xFF3700B3);
                        holder.playSongBtn.setVisibility(View.INVISIBLE);
                        holder.pauseSongBtn.setVisibility(View.VISIBLE);
//                        holder.playSongBtn.setBackgroundResource(R.drawable.ic_pause_song); // thay đổi hình ảnh của Button
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                isPlaying = false;
                if (isPlaying) {

                    // Ẩn Button "play" và hiển thị Button "pause"
//                    holder.pauseSongBtn.setVisibility(View.INVISIBLE);
//                    holder.playSongBtn.setVisibility(View.VISIBLE);

                    // set maxLines to a large number to display the full text
                    holder.songTitleView.setMaxLines(Integer.MAX_VALUE);
                    // Dừng animation của các item khác
                    if (currentAnimation != null) {
                        currentAnimation.cancel();
                    }

                    // Ẩn Button "play" và hiển thị Button "pause"
                    holder.pauseSongBtn.setVisibility(View.INVISIBLE);
                    holder.playSongBtn.setVisibility(View.VISIBLE);
                    // Bắt đầu animation cho item được click
                    holder.songTitleView.setTranslationX(0);
                    holder.mConstraintLayout.setBackgroundColor(0xFF3700B3);
                    holder.playSongBtn.setBackgroundColor(0xFF3700B3);
                    holder.pauseSongBtn.setBackgroundColor(0xFF3700B3);

                    // Lưu animation hiện tại cho item được click
                    currentAnimation = animation;

                    // Thiết lập animation
//                animation.setDuration((long) textWidth * 5);
                    animation.setRepeatCount(Animation.INFINITE);
                    holder.songTitleView.startAnimation(animation);
                    isPlaying = true;
                } else {

                    // reset maxLines to the original value
                    holder.songTitleView.setMaxLines(3);
                    // bắt đầu phát nhạc
                    holder.songTitleView.clearAnimation();
                }




            }
        });


    }

    public interface OnPlayClickListener {
        public void onClick(View view, int pos);
    }

    public interface OnPauseClickListener {
        public void onClick(View view, int pos);
    }

    public interface OnNextClickListener {
        public void onClick(View view, int pos);
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    @Override
    public void onViewDetachedFromWindow(SongViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (selectedPosition == holder.getAdapterPosition()) {
            Log.d("TAG", "onViewDetachedFromWindow: ");
            holder.mConstraintLayout.setBackgroundColor(0xFF3700B3);
            holder.playSongBtn.setBackgroundColor(0xFF3700B3);
            holder.pauseSongBtn.setBackgroundColor(0xFF3700B3);
            // Ẩn Button "play" và hiển thị Button "pause"
            holder.playSongBtn.setVisibility(View.INVISIBLE);
            holder.pauseSongBtn.setVisibility(View.VISIBLE);
        } else {
        }
    }

}