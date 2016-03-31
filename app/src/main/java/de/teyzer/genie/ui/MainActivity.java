package de.teyzer.genie.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.connect.ServerConnect;
import de.teyzer.genie.connect.ServerStatus;
import de.teyzer.genie.connect.UploadStatusListener;
import de.teyzer.genie.data.DataManager;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.data.MediaScanner;
import de.teyzer.genie.data.Prefs;
import de.teyzer.genie.ui.fragments.FoodListFragment;
import de.teyzer.genie.ui.fragments.LightFragment;
import de.teyzer.genie.ui.fragments.MusicFragment;
import de.teyzer.genie.ui.fragments.SettingsFragment;
import de.teyzer.genie.ui.fragments.ToolsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DataProvider, UploadStatusListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private DataManager dataManager;
    private ServerConnect serverConnect;
    private int startFragmentMenuItemId = R.id.nav_light_remote;

    @Bind(R.id.nav_view)
    NavigationView navView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private ActionBarDrawerToggle lastActionBarDrawerToggle = null;
    private ServerStatus serverStatus;

    public MainActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGui(savedInstanceState);

        initDataManager();

        checkIntent(getIntent());

        if (savedInstanceState == null) {
            showFragment(startFragmentMenuItemId, true);
        }
    }

    /**
     * Initialisiert die GUI
     *
     * @param savedInstanceState Vorher gespeicherter Zustand
     */
    private void initGui(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navView.setNavigationItemSelectedListener(this);
        navView.getMenu().getItem(0).setChecked(true);

        LinearLayout navHeader = (LinearLayout) navView.getHeaderView(0);

        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.action_settings);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar, false);
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar, boolean useBackButton) {
        super.setSupportActionBar(toolbar);

        //Alten listener entfernen
        drawerLayout.removeDrawerListener(lastActionBarDrawerToggle);

        if (useBackButton) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } else {
            //neuen hinzufügen
            lastActionBarDrawerToggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(lastActionBarDrawerToggle);
            lastActionBarDrawerToggle.syncState();
        }
    }

    /**
     * Initialisiert alle Datensachen
     */
    private void initDataManager() {
        dataManager = new DataManager(this);
        dataManager.loadData();

        MediaScanner.checkAndScanSongs(this, dataManager);

        serverConnect = new ServerConnect(this);

        serverStatus = new ServerStatus(serverConnect);
    }

    /**
     * Prüft, ob die Activity mit einem Teilen-Intent gestartet wurde
     */
    private void checkIntent(Intent intent) {
        String action = intent.getAction();
        //MIME-Type
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("audio/")) {
                //Einzelne Mp3-Datei empfangen
                Uri audioUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

                if (audioUri != null) {
                    serverConnect.executeAction(Action.playFile(audioUri, null, this));
                }
                startFragmentMenuItemId = R.id.nav_music_remote;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("audio/")) {
                //mehrere Mp3-Dateien empfangen

                ArrayList<Uri> audioUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (audioUris != null) {
                    serverConnect.executeAction(Action.playFiles(audioUris, null, this));
                }
                startFragmentMenuItemId = R.id.nav_music_remote;
            }
        }
    }

    @Override
    protected void onStop() {
        //Serververbindung trennen, wenn die Activity nicht mehr sichtbar ist
        serverConnect.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Wird einmal beim laden der Activity ausgeführt
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showFragment(R.id.action_settings);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        showFragment(item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(int id) {
        showFragment(id, false);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void showFragment(int id, boolean isFirst) {
        Fragment fragment = null;
        String tag = "";

        if (id == R.id.nav_food_inventory) {
            fragment = getSupportFragmentManager().findFragmentByTag(FoodListFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new FoodListFragment();
            }
            tag = FoodListFragment.FRAGMENT_TAG;
        } else if (id == R.id.nav_shopping_list) {

        } else if (id == R.id.nav_music_remote) {
            fragment = getSupportFragmentManager().findFragmentByTag(MusicFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new MusicFragment();
            }
            tag = MusicFragment.FRAGMENT_TAG;
        } else if (id == R.id.nav_manage) {
            fragment = getSupportFragmentManager().findFragmentByTag(ToolsFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new ToolsFragment();
            }
            tag = ToolsFragment.FRAGMENT_TAG;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.action_settings) {
            fragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new SettingsFragment();
            }
            tag = SettingsFragment.FRAGMENT_TAG;
        } else if (id == R.id.nav_light_remote) {
            fragment = getSupportFragmentManager().findFragmentByTag(LightFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new LightFragment();
            }
            tag = LightFragment.FRAGMENT_TAG;
        }

        if (fragment != null) {
            showFragment(fragment, tag, isFirst);
        }
    }

    private void showFragment(Fragment frag, String fragmentTag, boolean isFirst) {
        if (isFirst) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, frag, fragmentTag)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    //.setCustomAnimations(R.anim.frag_enter, R.anim.frag_exit, R.anim.frag_pop_enter, R.anim.frag_pop_exit)
                    .replace(R.id.main_fragment_container, frag, fragmentTag)
                    .addToBackStack(fragmentTag)
                    .commit();
        }
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    /**
     * Wird ausgelöst, wenn der UploadStatus verändert wird
     *
     * @param text            Statustext
     * @param progressPercent Prozentualer Forschritt, über 100 wenn fertiggestellt.
     */
    @Override
    public void updateUploadStatus(final String text, final int progressPercent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MusicFragment musicFragment = MusicFragment.getInstance();
                if (musicFragment != null) {
                    musicFragment.updateUploadStatus(text, progressPercent);
                }
            }
        });
    }

    /**
     * Wird ausgelöst, wenn Einstellungen verändert werden
     *
     * @param sharedPreferences Das Einstellungsobjekt
     * @param key               Die veränderte Einstellungen
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Prefs.PREF_HOST_ADRESS.equals(key) || Prefs.PREF_HOST_PORT.equals(key)) {
            serverConnect.disconnect();
            serverConnect.refreshPrefs();
        }
    }

}
