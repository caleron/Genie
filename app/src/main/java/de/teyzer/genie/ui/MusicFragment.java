package de.teyzer.genie.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.connect.ResponseListener;
import de.teyzer.genie.connect.ServerConnect;
import de.teyzer.genie.connect.UploadStatusListener;
import de.teyzer.genie.data.DataManager;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.PlayerState;
import de.teyzer.genie.model.Track;


public class MusicFragment extends Fragment implements UploadStatusListener, ResponseListener {
    public static final String FRAGMENT_TAG = "music_control";

    public static final int REQUEST_SHOW_PROGRESS = 0;

    private static MusicFragment musicFragment;
    @Bind(R.id.music_shuffle_btn)
    ImageButton musicShuffleBtn;
    @Bind(R.id.music_play_pause_btn)
    ImageButton musicPlayPauseBtn;
    @Bind(R.id.music_repeat_btn)
    ImageButton musicRepeatBtn;
    @Bind(R.id.music_current_title_text)
    TextView musicCurrentTitleText;
    @Bind(R.id.music_current_artist_text)
    TextView musicCurrentArtistText;
    @Bind(R.id.music_current_progress_bar)
    SeekBar musicCurrentSeekBar;
    @Bind(R.id.music_list)
    RecyclerView trackListView;

    private MusicAdapter musicAdapter;
    private PlayerState playerState;

    //null, falls nicht mehr aktiv
    private PlayProgressTimer playProgressTimer;

    private UploadStatusDialogFragment uploadStatusDialogFragment = null;

    private DataProvider mListener;

    public MusicFragment() {
        musicFragment = this;
        playerState = new PlayerState(true, PlayerState.REPEAT_MODE_ALL);
    }

    public static MusicFragment getInstance() {
        return musicFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, root);

        RecyclerView.LayoutManager mListLayoutManager = new LinearLayoutManager(getActivity());
        trackListView.setLayoutManager(mListLayoutManager);

        musicAdapter = new MusicAdapter();
        trackListView.setAdapter(musicAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Playerstate neu abfragen
        requestStatusRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (playProgressTimer != null) {
            playProgressTimer.cancel();
            playProgressTimer = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SHOW_PROGRESS && resultCode == Activity.RESULT_CANCELED) {
            uploadStatusDialogFragment = null;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataProvider) {
            mListener = (DataProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DataProvider) {
            mListener = (DataProvider) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void updateStatus(String text, int progressPercent) {
        if (uploadStatusDialogFragment == null) {
            uploadStatusDialogFragment = new UploadStatusDialogFragment();
            uploadStatusDialogFragment.setTargetFragment(musicFragment, REQUEST_SHOW_PROGRESS);

            uploadStatusDialogFragment.show(getFragmentManager(), UploadStatusDialogFragment.FRAGMENT_TAG);
            uploadStatusDialogFragment.updateStatus(text, Math.max(progressPercent, 0));
        } else {
            if (progressPercent > 100) {
                uploadStatusDialogFragment.dismiss();
                uploadStatusDialogFragment = null;
            } else {
                uploadStatusDialogFragment.updateStatus(text, Math.max(progressPercent, 0));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.music_shuffle_btn, R.id.music_previous_btn, R.id.music_play_pause_btn, R.id.music_next_btn,
            R.id.music_repeat_btn})
    public void onClick(View view) {
        ServerConnect serverConnect = mListener.getServerConnect();
        switch (view.getId()) {
            case R.id.music_shuffle_btn:
                playerState.toggleShuffle();
                refreshShuffleBtnState();

                serverConnect.executeAction(Action.setShuffle(playerState.isShuffle(), this));
                break;

            case R.id.music_previous_btn:
                serverConnect.executeAction(Action.playPrevious(this));
                break;

            case R.id.music_play_pause_btn:
                playerState.togglePlaying();
                refreshPlayPauseBtnState();

                serverConnect.executeAction(Action.togglePlayPause(this));
                break;

            case R.id.music_next_btn:
                serverConnect.executeAction(Action.playNext(this));
                break;

            case R.id.music_repeat_btn:
                playerState.nextRepeatMode();
                refreshRepeatBtnState();

                serverConnect.executeAction(Action.setRepeatMode(playerState.getRepeatMode(), this));
                break;
        }
    }

    private void requestStatusRefresh() {
        ServerConnect serverConnect = mListener.getServerConnect();
        serverConnect.executeAction(Action.getStatus(this));
    }

    @Override
    public void responseReceived(Action sourceAction, final String response) {
        musicShuffleBtn.post(new Runnable() {
            @Override
            public void run() {
                if (playerState.parsePlayerState(response)) {
                    if (playProgressTimer != null) {
                        playProgressTimer.cancel();
                    }
                    playProgressTimer = null;
                    System.out.println("new song");
                }
                applyPlayerState();
            }
        });
    }

    private void applyPlayerState() {
        refreshCurrentPlayingTexts();
        refreshProgressBar();
        refreshRepeatBtnState();
        refreshShuffleBtnState();
        refreshPlayPauseBtnState();

        if (!playerState.isPlaying()) {
            if (playProgressTimer != null) {
                playProgressTimer.cancel();
            }
            playProgressTimer = null;
            System.out.println("playback paused, timer cancelled");
        } else if (playProgressTimer == null) {
            int remainingTime = playerState.getRemainingSeconds();

            if (remainingTime > 1) {
                playProgressTimer = new PlayProgressTimer(remainingTime);
                playProgressTimer.start();
                System.out.println("new timer started");
            }
        }
    }

    private void refreshCurrentPlayingTexts() {
        musicCurrentArtistText.setText(playerState.getCurrentArtist());
        musicCurrentTitleText.setText(playerState.getCurrentTitle());
    }

    private void refreshProgressBar() {
        if (musicCurrentSeekBar.getMax() != playerState.getTrackLength()) {
            musicCurrentSeekBar.setProgress(0);
            musicCurrentSeekBar.setMax(playerState.getTrackLength());
        }
        musicCurrentSeekBar.setProgress(playerState.getPlayPosition());
    }

    private void refreshPlayPauseBtnState() {
        int imageResource;
        if (playerState.isPlaying()) {
            imageResource = R.drawable.ic_pause_circle_filled_deep_orange_a200_36dp;
        } else {
            imageResource = R.drawable.ic_play_circle_filled_deep_orange_a200_36dp;
        }
        musicPlayPauseBtn.setImageResource(imageResource);
    }

    private void refreshShuffleBtnState() {
        int imageResource;

        if (playerState.isShuffle()) {
            imageResource = R.drawable.ic_shuffle_deep_orange_a200_36dp;
        } else {
            imageResource = R.drawable.ic_shuffle_grey_600_36dp;
        }

        musicShuffleBtn.setImageResource(imageResource);
    }

    private void refreshRepeatBtnState() {
        int imageResource;
        if (playerState.getRepeatMode() == PlayerState.REPEAT_MODE_NONE) {
            imageResource = R.drawable.ic_repeat_grey_600_36dp;
        } else if (playerState.getRepeatMode() == PlayerState.REPEAT_MODE_ONE) {
            imageResource = R.drawable.ic_repeat_one_deep_orange_a200_36dp;
        } else {
            imageResource = R.drawable.ic_repeat_deep_orange_a200_36dp;
        }
        musicRepeatBtn.setImageResource(imageResource);
    }

    private class PlayProgressTimer extends CountDownTimer {

        /**
         * @param timeLeft Restdauer in Sekunden
         */
        public PlayProgressTimer(long timeLeft) {
            super(timeLeft * 1000, 1000);
        }

        @Override
        public void onTick(final long millisUntilFinished) {
            if (musicCurrentSeekBar == null || playProgressTimer == null) {
                playProgressTimer = null;
                cancel();
                return;
            }
            if (playProgressTimer != this) {
                cancel();
                return;
            }

            musicCurrentSeekBar.post(new Runnable() {
                @Override
                public void run() {
                    musicCurrentSeekBar.incrementProgressBy(1);
                    /*int progress = (int) (playerState.getTrackLength() - (millisUntilFinished / 1000));
                    musicCurrentSeekBar.setProgress(progress);*/
                }
            });
        }

        @Override
        public void onFinish() {
            playProgressTimer = null;
            requestStatusRefresh();
        }
    }

    /**
     * Adapter zum Darstellen der Nahrungsmitteltypen in der Liste
     */
    private class MusicAdapter extends RecyclerView.Adapter<ViewHolder> {
        DataManager dataManager;

        /**
         * Erstellt einen neuen MusicAdapter und holt den Datenmanager von der Activity
         */
        public MusicAdapter() {
            dataManager = mListener.getDataManager();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Wird so oft ausgeführt, wie ViewHolder auf den Bildschirm passen
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_item, parent, false);
            return new ViewHolder(v, dataManager);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //Bindet einen neuen Track an einen bestehenden ViewHolder
            Track track = dataManager.getTrackAt(position);
            holder.bindTrack(track);
        }

        @Override
        public int getItemCount() {
            return dataManager.getTracks().size();
        }
    }


    /**
     * Verwaltet einen Eintrag in der RecyclerView
     */
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        TextView titleView;
        TextView artistView;
        Track track;

        DataManager dataManager;

        public ViewHolder(View itemView, DataManager dataManager) {
            super(itemView);
            this.itemView = itemView;

            titleView = (TextView) itemView.findViewById(R.id.titleText);
            artistView = (TextView) itemView.findViewById(R.id.artistText);

            this.itemView = itemView;
            this.dataManager = dataManager;

            itemView.setOnClickListener(this);
        }

        public void bindTrack(Track track) {
            this.track = track;

            titleView.setText(track.getTitle());
            artistView.setText(track.getArtist());
        }

        @Override
        public void onClick(View v) {
            File f = new File(track.getPath());
            Uri uri = Uri.fromFile(f);
            System.out.println(uri);
            mListener.getServerConnect().executeAction(Action.playFile(uri, musicFragment, musicFragment));
        }
    }
}
