package de.teyzer.genie.ui.custom;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
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
import de.teyzer.genie.connect.ServerStatus;
import de.teyzer.genie.connect.StatusChangedListener;
import de.teyzer.genie.data.DataProvider;

public class PlayerBar extends RelativeLayout implements StatusChangedListener, SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.music_shuffle_btn)
    ImageButton musicShuffleBtn;
    @Bind(R.id.music_play_pause_btn)
    ImageButton musicPlayPauseBtn;
    @Bind(R.id.music_repeat_btn)
    ImageButton musicRepeatBtn;
    @Bind(R.id.music_current_title_text)
    MarqueeTextView musicCurrentTitleText;
    //@Bind(R.id.music_current_artist_text)
    //TextView musicCurrentArtistText;
    @Bind(R.id.music_current_progress_bar)
    SeekBar musicCurrentSeekBar;

    //null, falls nicht mehr aktiv
    private PlayProgressTimer playProgressTimer;
    private ServerStatus serverStatus;

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

        musicCurrentSeekBar.setOnSeekBarChangeListener(this);
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.serverStatus = dataProvider.getServerStatus();
        serverStatus.addStatusChangedListener(this);
        refreshCurrentPlayingTexts();
    }

    /**
     * Fordert einen neuen Serverstatus an
     */
    public void requestStatusRefresh() {
        if (serverStatus != null && isShown()) {
            serverStatus.requestNewStatus();
        }
    }

    @Override
    public void serverStatusChanged(final boolean newSong) {
        post(new Runnable() {
            @Override
            public void run() {
                if (newSong) {
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

        switch (view.getId()) {
            case R.id.music_shuffle_btn:
                serverStatus.toggleShuffle();
                refreshShuffleBtnState();
                break;

            case R.id.music_previous_btn:
                serverStatus.playPrevious();
                break;

            case R.id.music_play_pause_btn:
                serverStatus.togglePlaying();
                refreshPlayPauseBtnState();

                break;

            case R.id.music_next_btn:
                serverStatus.playNext();
                break;

            case R.id.music_repeat_btn:
                serverStatus.nextRepeatMode();
                refreshRepeatBtnState();
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

        if (!serverStatus.isPlaying()) {
            if (playProgressTimer != null) {
                playProgressTimer.cancel();
            }
            playProgressTimer = null;
            System.out.println("playback paused, timer cancelled");
        } else if (playProgressTimer == null) {
            int remainingTime = serverStatus.getRemainingSeconds();

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
        //musicCurrentArtistText.setText(serverStatus.getCurrentArtist());
        String title = serverStatus.getCurrentTitle();
        String artist = serverStatus.getCurrentArtist();
        SpannableString text = new SpannableString(title + " - " + artist);

        if (title.length() > 0) {
            text.setSpan(new TextAppearanceSpan(getContext(), R.style.AppTheme_PrimaryTextDarkBack),
                    0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        text.setSpan(new TextAppearanceSpan(getContext(), R.style.AppTheme_SecondaryTextDarkBack),
                title.length(), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        musicCurrentTitleText.setText(text, TextView.BufferType.SPANNABLE);
    }

    /**
     * Setzt aktuellen und maximalen Wert der musicCurrentSeekBar
     */
    private void refreshProgressBar() {
        if (musicCurrentSeekBar.getMax() != serverStatus.getTrackLength()) {
            musicCurrentSeekBar.setProgress(0);
            musicCurrentSeekBar.setMax(serverStatus.getTrackLength());
        }
        musicCurrentSeekBar.setProgress(serverStatus.getPlayPosition());
    }

    /**
     * Passt das Icon des Play/Pause-Buttons an
     */
    private void refreshPlayPauseBtnState() {
        int imageResource;
        if (serverStatus.isPlaying()) {
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

        if (serverStatus.isShuffle()) {
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
        if (serverStatus.getRepeatMode() == ServerStatus.REPEAT_MODE_NONE) {
            imageResource = R.drawable.ic_repeat_grey_600_36dp;
        } else if (serverStatus.getRepeatMode() == ServerStatus.REPEAT_MODE_ONE) {
            imageResource = R.drawable.ic_repeat_one_deep_orange_a200_36dp;
        } else {
            imageResource = R.drawable.ic_repeat_deep_orange_a200_36dp;
        }
        musicRepeatBtn.setImageResource(imageResource);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //uninteressant
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //auch egal
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //neue Position an Server senden
        if (serverStatus != null) {
            int position = seekBar.getProgress();

            if (playProgressTimer != null) {
                playProgressTimer.cancel();
                playProgressTimer = null;
            }
            serverStatus.playFromPosition(position);
        }
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
