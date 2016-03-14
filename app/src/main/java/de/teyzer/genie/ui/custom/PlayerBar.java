package de.teyzer.genie.ui.custom;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.connect.ResponseListener;
import de.teyzer.genie.connect.ServerConnect;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.PlayerState;

public class PlayerBar extends RelativeLayout implements ResponseListener {

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

    private PlayerState playerState;
    //null, falls nicht mehr aktiv
    private PlayProgressTimer playProgressTimer;
    private DataProvider dataProvider;

    public PlayerBar(Context context) {
        super(context);
        init(context);
    }

    public PlayerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.bar_player, this, true);

        ButterKnife.bind(this);

        playerState = new PlayerState(true, PlayerState.REPEAT_MODE_ALL);
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Fordert einen neuen Serverstatus an
     */
    public void requestStatusRefresh() {
        if (dataProvider != null) {
            ServerConnect serverConnect = dataProvider.getServerConnect();
            serverConnect.executeAction(Action.getStatus(this));
        }
    }

    /**
     * Wird ausgelöst, wenn eine Status-Antwort vom Server gekommen ist
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
    @Override
    public void responseReceived(Action sourceAction, final String response) {
        post(new Runnable() {
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroyTimer();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestStatusRefresh();
    }

    public void destroyTimer() {
        //Timer zurücksetzen
        if (playProgressTimer != null) {
            playProgressTimer.cancel();
            playProgressTimer = null;
        }
    }


    /**
     * Wird beim Klick auf einen der Wiedergabesteuerungs-Buttons ausgelöst
     *
     * @param view Der Button
     */
    @OnClick({R.id.music_shuffle_btn, R.id.music_previous_btn, R.id.music_play_pause_btn, R.id.music_next_btn,
            R.id.music_repeat_btn})
    public void onClick(View view) {
        ServerConnect serverConnect = dataProvider.getServerConnect();
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


    /**
     * Wendet den Playerstatus des Servers an
     */
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

    /**
     * Setzt Titel und Interpret
     */
    private void refreshCurrentPlayingTexts() {
        musicCurrentArtistText.setText(playerState.getCurrentArtist());
        musicCurrentTitleText.setText(playerState.getCurrentTitle());
    }

    /**
     * Setzt aktuellen und maximalen Wert der musicCurrentSeekBar
     */
    private void refreshProgressBar() {
        if (musicCurrentSeekBar.getMax() != playerState.getTrackLength()) {
            musicCurrentSeekBar.setProgress(0);
            musicCurrentSeekBar.setMax(playerState.getTrackLength());
        }
        musicCurrentSeekBar.setProgress(playerState.getPlayPosition());
    }

    /**
     * Passt das Icon des Play/Pause-Buttons an
     */
    private void refreshPlayPauseBtnState() {
        int imageResource;
        if (playerState.isPlaying()) {
            imageResource = R.drawable.ic_pause_circle_filled_deep_orange_a200_36dp;
        } else {
            imageResource = R.drawable.ic_play_circle_filled_deep_orange_a200_36dp;
        }
        musicPlayPauseBtn.setImageResource(imageResource);
    }

    /**
     * Passt das Icon des Shuffle-Buttons an
     */
    private void refreshShuffleBtnState() {
        int imageResource;

        if (playerState.isShuffle()) {
            imageResource = R.drawable.ic_shuffle_deep_orange_a200_36dp;
        } else {
            imageResource = R.drawable.ic_shuffle_grey_600_36dp;
        }

        musicShuffleBtn.setImageResource(imageResource);
    }

    /**
     * Passt das Icon des Wiederholungsmodus-Buttons an
     */
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
                }
            });
        }

        @Override
        public void onFinish() {
            playProgressTimer = null;
            requestStatusRefresh();
        }
    }

}
