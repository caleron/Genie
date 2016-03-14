package de.teyzer.genie.data;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import de.teyzer.genie.connect.ServerConnect;

public interface DataProvider {
    DataManager getDataManager();

    ServerConnect getServerConnect();

    FragmentManager getSupportFragmentManager();

    ActionBar getSupportActionBar();

    void setSupportActionBar(Toolbar actionBar);

    void setSupportActionBar(Toolbar actionBar, boolean useBackButton);
}
