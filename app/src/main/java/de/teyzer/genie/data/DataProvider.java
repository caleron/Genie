package de.teyzer.genie.data;

import de.teyzer.genie.connect.ServerConnect;

public interface DataProvider {
    DataManager getDataManager();
    ServerConnect getServerConnect();
}
