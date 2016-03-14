package de.teyzer.genie.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.connect.ResponseListener;
import de.teyzer.genie.connect.ServerConnect;
import de.teyzer.genie.connect.UploadStatusListener;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.PlayerState;


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
    @Bind(R.id.music_list_pager)
    ViewPager musicListPager;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.music_player_bar)
    View playerBar;

    private PlayerState playerState;

    //null, falls nicht mehr aktiv
    private PlayProgressTimer playProgressTimer;

    private UploadStatusDialogFragment uploadStatusDialogFragment = null;

    private DataProvider mListener;
    private MusicTabPagerAdapter musicTabPagerAdapter;

    public MusicFragment() {
        musicFragment = this;
        playerState = new PlayerState(true, PlayerState.REPEAT_MODE_ALL);
    }

    public static MusicFragment getInstance() {
        return musicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar);

        initTabs();

        return root;
    }

    private void initTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Titel"));
        tabLayout.addTab(tabLayout.newTab().setText("Künstler"));
        tabLayout.addTab(tabLayout.newTab().setText("Alben"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        /**
         * WICHTIG - getChildFragmentManager verwenden, sonst funktioniert der Lebenszyklus
         * der Fragmente nicht richtig. Sie werden dann nicht neu angezeigt, nachdem das
         * MusicFragment im Hintergrund war.
         */
        musicTabPagerAdapter = new MusicTabPagerAdapter(getChildFragmentManager());
        musicListPager.setAdapter(musicTabPagerAdapter);
        musicListPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                musicListPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public MusicListFragment getCurrentTabFragment() {
        return (MusicListFragment) musicTabPagerAdapter.getItem(tabLayout.getSelectedTabPosition());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Wird einmal beim laden der Activity ausgeführt
        inflater.inflate(R.menu.fragment_music_menu, menu);

        //Suchfeld raussuchen
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        //Listener setzen
        SearchListener searchListener = new SearchListener();
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);
        searchView.setOnQueryTextListener(searchListener);

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

        //Timer zurücksetzen
        if (playProgressTimer != null) {
            playProgressTimer.cancel();
            playProgressTimer = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Zum upload abbrechen, noch nicht implementiert
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

    /**
     * Updated den Upload-Status
     *
     * @param text            Statustext
     * @param progressPercent Prozentualer Forschritt, über 100 wenn fertiggestellt.
     */
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

    /**
     * Wird beim Klick auf einen der Wiedergabesteuerungs-Buttons ausgelöst
     *
     * @param view Der Button
     */
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

    /**
     * Fordert einen neuen Serverstatus an
     */
    private void requestStatusRefresh() {
        ServerConnect serverConnect = mListener.getServerConnect();
        serverConnect.executeAction(Action.getStatus(this));
    }

    /**
     * Wird ausgelöst, wenn eine Status-Antwort vom Server gekommen ist
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
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
     * Wählt Passend zum Tabindex das richtige Tabfragment aus
     */
    public class MusicTabPagerAdapter extends FragmentStatePagerAdapter {

        MusicListFragment titleListFrag;
        MusicListFragment artistListFrag;
        MusicListFragment albumListFrag;

        public MusicTabPagerAdapter(FragmentManager fm) {
            super(fm);
            titleListFrag = new MusicListFragment();
            titleListFrag.setArguments(musicFragment, MusicListFragment.MODE_TITLE);

            artistListFrag = new MusicListFragment();
            artistListFrag.setArguments(musicFragment, MusicListFragment.MODE_ARTIST);

            albumListFrag = new MusicListFragment();
            albumListFrag.setArguments(musicFragment, MusicListFragment.MODE_ALBUM);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return titleListFrag;
                case 1:
                    return artistListFrag;
                case 2:
                    return albumListFrag;
            }
            return titleListFrag;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Titel";
                case 1:
                    return "Künstler";
                case 2:
                    return "Alben";
            }
            return "Errora";
        }
    }

    /**
     * Reagiert auf Events vom Suchfeld. Für die Suche werden playerBar und tabLayout versteckt.
     */
    public class SearchListener implements MenuItemCompat.OnActionExpandListener, SearchView.OnQueryTextListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            //Tabbar und playerBar verstecken
            playerBar.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            getCurrentTabFragment().setSearchMode(true);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            //Tabbar und playerBar wieder anzeigen
            playerBar.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            getCurrentTabFragment().setSearchMode(false);
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            getCurrentTabFragment().updateSearchString(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            getCurrentTabFragment().updateSearchString(newText);
            return false;
        }
    }
}
