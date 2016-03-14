package de.teyzer.genie.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.data.DataManager;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.Album;
import de.teyzer.genie.model.Artist;
import de.teyzer.genie.model.Track;

public class MusicListFragment extends AbstractFragment {
    public static final int MODE_TITLE = 0;
    public static final int MODE_ARTIST = 1;
    public static final int MODE_ALBUM = 2;

    @Bind(R.id.music_list)
    RecyclerView trackListView;

    private MusicAdapter musicAdapter;
    private MusicFragment parentFragment;

    private int displayMode;
    private boolean searchMode = false;
    private boolean isEmptySearchString = true;

    ArrayList<Track> searchTracks = new ArrayList<>();
    ArrayList<Artist> searchArtists = new ArrayList<>();
    ArrayList<Album> searchAlbums = new ArrayList<>();

    public void setArguments(MusicFragment parentFragment, int mode) {
        this.parentFragment = parentFragment;

        this.displayMode = mode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.bind(this, root);

        RecyclerView.LayoutManager mListLayoutManager = new LinearLayoutManager(getActivity());
        trackListView.setLayoutManager(mListLayoutManager);

        musicAdapter = new MusicAdapter();
        trackListView.setAdapter(musicAdapter);

        return root;
    }
    /**
     * Setzt den Suchmodus.
     *
     * @param searchMode True, wenn Suchlisten verwendet werden sollen.
     */
    public void setSearchMode(boolean searchMode) {
        this.searchMode = searchMode;
        isEmptySearchString = true;
        if (!searchMode) {
            musicAdapter.notifyDataSetChanged();
        }
    }

    public void updateSearchString(String text) {
        DataManager dataManager = mListener.getDataManager();
        if (text.length() == 0) {
            isEmptySearchString = true;
        } else {
            isEmptySearchString = false;
            switch (displayMode) {
                case MODE_TITLE:
                    searchTracks = dataManager.findTracks(text);
                    break;
                case MODE_ALBUM:
                    searchAlbums = dataManager.findAlbums(text);
                    break;
                case MODE_ARTIST:
                    searchArtists = dataManager.findArtists(text);
                    break;
            }
        }
        musicAdapter.notifyDataSetChanged();
    }

    /**
     * Adapter zum Darstellen der Musiktitel in der Liste
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_music, parent, false);
            return new ViewHolder(v, dataManager);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //Bindet einen neuen Track an einen bestehenden ViewHolder
            switch (displayMode) {
                case MODE_TITLE:
                    Track track;
                    if (searchMode && !isEmptySearchString) {
                        track = searchTracks.get(position);
                    } else {
                        track = dataManager.getTrackAt(position);
                    }
                    holder.bindTrack(track);
                    break;
                case MODE_ALBUM:
                    Album album;
                    if (searchMode && !isEmptySearchString) {
                        album = searchAlbums.get(position);
                    } else {
                        album = dataManager.getAlbumAt(position);
                    }
                    holder.bindAlbum(album);
                    break;
                case MODE_ARTIST:
                    Artist artist;
                    if (searchMode && !isEmptySearchString) {
                        artist = searchArtists.get(position);
                    } else {
                        artist = dataManager.getArtistAt(position);
                    }
                    holder.bindArtist(artist);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            switch (displayMode) {
                case MODE_TITLE:
                    if (searchMode && !isEmptySearchString) {
                        return searchTracks.size();
                    } else {
                        return dataManager.getTracks().size();
                    }
                case MODE_ALBUM:
                    if (searchMode && !isEmptySearchString) {
                        return searchAlbums.size();
                    } else {
                        return dataManager.getAlbums().size();
                    }
                case MODE_ARTIST:
                    if (searchMode && !isEmptySearchString) {
                        return searchArtists.size();
                    } else {
                        return dataManager.getArtists().size();
                    }
                default:
                    Log.e(this.toString(), "Incorrect display mode = " + displayMode);
                    return 0;
            }
        }
    }


    /**
     * Verwaltet einen Eintrag in der RecyclerView
     */
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        TextView titleView;
        TextView subTitleView;

        Track track;
        Album album;
        Artist artist;

        DataManager dataManager;

        public ViewHolder(View itemView, DataManager dataManager) {
            super(itemView);
            this.itemView = itemView;

            titleView = (TextView) itemView.findViewById(R.id.titleText);
            subTitleView = (TextView) itemView.findViewById(R.id.subTitleText);

            this.itemView = itemView;
            this.dataManager = dataManager;

            itemView.setOnClickListener(this);
        }

        public void bindTrack(Track track) {
            this.track = track;

            titleView.setText(track.getTitle());
            subTitleView.setText(track.getArtist());
        }

        public void bindAlbum(Album album) {
            this.album = album;

            titleView.setText(album.getAlbumName());
            subTitleView.setText(album.getArtistName());
        }

        public void bindArtist(Artist artist) {
            this.artist = artist;

            titleView.setText(artist.getName());
            subTitleView.setText(artist.albums.size() + " Alben");
        }

        @Override
        public void onClick(View v) {
            switch (displayMode) {
                case MODE_TITLE:
                    File f = new File(track.getPath());
                    Uri uri = Uri.fromFile(f);
                    System.out.println(uri);
                    mListener.getServerConnect().executeAction(Action.playFile(uri, parentFragment, parentFragment));
                    break;
                case MODE_ALBUM:

                    break;
                case MODE_ARTIST:

                    break;
            }
        }
    }
}
