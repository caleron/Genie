package de.teyzer.genie.ui.fragments;

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
import de.teyzer.genie.connect.UploadStatusListener;
import de.teyzer.genie.model.Artist;
import de.teyzer.genie.ui.custom.PlayerBar;

public class ArtistFragment extends AbstractFragment {
    public static final String FRAGMENT_TAG = "artist_fragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.music_player_bar)
    PlayerBar playerBar;
    @Bind(R.id.music_list_pager)
    ViewPager viewPager;
    @Bind(R.id.tab_layout)
    TabLayout tabLayout;

    private MusicTabPagerAdapter musicTabPagerAdapter;
    private Artist displayArtist;
    private UploadStatusListener listener;
    private MenuItem searchItem;

    public ArtistFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar, true);

        //Titel setzen
        toolbar.setTitle(displayArtist.getName());

        playerBar.setDataProvider(mListener);

        initTabs();

        return root;
    }

    private void initTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Titel"));
        tabLayout.addTab(tabLayout.newTab().setText("Alben"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        /**
         * WICHTIG - getChildFragmentManager verwenden, sonst funktioniert der Lebenszyklus
         * der Fragmente nicht richtig. Sie werden dann nicht neu angezeigt, nachdem das
         * MusicFragment im Hintergrund war.
         */
        musicTabPagerAdapter = new MusicTabPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(musicTabPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Such-Button nur anzeigen, wenn der Titel-Tab ausgewählt ist
                searchItem.setVisible(tab.getPosition() == 0);

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        playerBar.requestStatusRefresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        //Suchfeld raussuchen
        searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        //Listener setzen
        SearchListener searchListener = new SearchListener();
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);
        searchView.setOnQueryTextListener(searchListener);
    }

    public void setArguments(UploadStatusListener listener, Artist artist) {
        this.listener = listener;
        displayArtist = artist;
    }

    /**
     * Wählt Passend zum Tabindex das richtige Tabfragment aus
     */
    public class MusicTabPagerAdapter extends FragmentStatePagerAdapter {

        final MusicListFragment titleListFrag;
        final MusicListFragment albumListFrag;

        public MusicTabPagerAdapter(FragmentManager fm) {
            super(fm);
            titleListFrag = new MusicListFragment();
            titleListFrag.setTrackMode(listener, displayArtist.getAllTracks());

            albumListFrag = new MusicListFragment();
            albumListFrag.setAlbumMode(listener, displayArtist.getAlbums());
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return titleListFrag;
                case 1:
                    return albumListFrag;
            }
            return titleListFrag;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Titel";
                case 1:
                    return "Alben";
            }
            return "Errora";
        }
    }

    /**
     * Reagiert auf Events vom Suchfeld. Für die Suche werden playerBar und tabLayout versteckt.
     */
    private class SearchListener implements MenuItemCompat.OnActionExpandListener, SearchView.OnQueryTextListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            //Tabbar und playerBar verstecken
            playerBar.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            ((MusicListFragment) musicTabPagerAdapter.getItem(0)).setSearchMode(true);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            //Tabbar und playerBar wieder anzeigen
            playerBar.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            ((MusicListFragment) musicTabPagerAdapter.getItem(0)).setSearchMode(false);
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            ((MusicListFragment) musicTabPagerAdapter.getItem(0)).updateSearchString(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            ((MusicListFragment) musicTabPagerAdapter.getItem(0)).updateSearchString(newText);
            return false;
        }
    }
}
