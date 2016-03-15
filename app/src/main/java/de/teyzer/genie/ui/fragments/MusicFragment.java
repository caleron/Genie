package de.teyzer.genie.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.connect.ResponseListener;
import de.teyzer.genie.connect.UploadStatusListener;
import de.teyzer.genie.ui.custom.PlayerBar;
import de.teyzer.genie.ui.dialogs.UploadStatusDialogFragment;


public class MusicFragment extends AbstractFragment implements UploadStatusListener, ResponseListener {
    public static final String FRAGMENT_TAG = "music_control";
    public static final int REQUEST_SHOW_PROGRESS = 0;

    private static MusicFragment musicFragment;

    @Bind(R.id.music_list_pager)
    ViewPager musicListPager;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.music_player_bar)
    PlayerBar playerBar;

    private UploadStatusDialogFragment uploadStatusDialogFragment = null;
    private MusicTabPagerAdapter musicTabPagerAdapter;

    public MusicFragment() {
        musicFragment = this;
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

        playerBar.setDataProvider(mListener);

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
        inflater.inflate(R.menu.search_menu, menu);

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
        //Playerstate neu abfragen TODO testen obs ohne funktioniert wg PlayerBar.onAttachedToWindow
        //playerBar.requestStatusRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        //playerBar.destroyTimer();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Zum upload abbrechen, noch nicht implementiert
        if (requestCode == REQUEST_SHOW_PROGRESS && resultCode == Activity.RESULT_CANCELED) {
            uploadStatusDialogFragment = null;
        }
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

    /**
     * Wird ausgelöst, wenn eine Status-Antwort vom Server gekommen ist
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
    @Override
    public void responseReceived(Action sourceAction, String response) {
        playerBar.responseReceived(sourceAction, response);
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
            titleListFrag.setTrackMode(musicFragment, mListener.getDataManager().getTracks());

            artistListFrag = new MusicListFragment();
            artistListFrag.setArtistMode(musicFragment, mListener.getDataManager().getArtists());

            albumListFrag = new MusicListFragment();
            albumListFrag.setAlbumMode(musicFragment, mListener.getDataManager().getAlbums());
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
