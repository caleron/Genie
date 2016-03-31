package de.teyzer.genie.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import de.teyzer.genie.connect.UploadStatusListener;
import de.teyzer.genie.model.Album;
import de.teyzer.genie.model.Artist;
import de.teyzer.genie.model.Track;

public class MusicListFragment extends AbstractFragment {
    private static final int MODE_TITLE = 0;
    private static final int MODE_ARTIST = 1;
    private static final int MODE_ALBUM = 2;

    @Bind(R.id.music_list)
    RecyclerView trackListView;

    private MusicAdapter musicAdapter;
    private UploadStatusListener listener;

    private int displayMode;

    private ArrayList<Track> displayTracks;
    private ArrayList<Artist> displayArtists;
    private ArrayList<Album> displayAlbums;

    private ArrayList<Track> allTracks;
    private ArrayList<Artist> allArtists;
    private ArrayList<Album> allAlbums;

    public void setTrackMode(UploadStatusListener parentFragment, ArrayList<Track> tracks) {
        this.listener = parentFragment;
        this.displayMode = MODE_TITLE;
        allTracks = tracks;
        displayTracks = tracks;
    }

    public void setAlbumMode(UploadStatusListener parentFragment, ArrayList<Album> albums) {
        this.listener = parentFragment;
        this.displayMode = MODE_ALBUM;
        allAlbums = albums;
        displayAlbums = albums;
    }

    public void setArtistMode(UploadStatusListener parentFragment, ArrayList<Artist> artists) {
        this.listener = parentFragment;
        this.displayMode = MODE_ARTIST;
        allArtists = artists;
        displayArtists = artists;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Einmalige Sachen müssen in onCreate gemacht werden, da onCreateView jedes Mal neu ausgeführt
        //wird, nachdem das Fragment im Hintegrund war
        musicAdapter = new MusicAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.bind(this, root);

        RecyclerView.LayoutManager mListLayoutManager = new LinearLayoutManager(getActivity());
        trackListView.setLayoutManager(mListLayoutManager);
        trackListView.setAdapter(musicAdapter);

        return root;
    }


    /**
     * Sucht Tracks zu einem Titel heraus. Falls der Suchbegriff ein leerer String ist, werden
     * alle Tracks zurückgegeben.
     *
     * @param title Der Suchtitel
     * @return Liste passender Tracks
     */
    private ArrayList<Track> findTracks(String title) {
        if (title.length() == 0) {
            return allTracks;
        }

        title = title.toLowerCase();
        ArrayList<Track> result = new ArrayList<>();

        for (Track track : allTracks) {
            if (track.getTitle().toLowerCase().contains(title)) {
                result.add(track);
            }
        }
        return result;
    }

    /**
     * Sucht Künstler nach einem Suchbegriff heraus. Falls der Suchbegriff ein leerer String ist,
     * werden alle Künstler zurückgegeben.
     *
     * @param title Der Suchbegriff
     * @return Liste passender Interpreten
     */
    private ArrayList<Artist> findArtists(String title) {
        if (title.length() == 0) {
            return allArtists;
        }

        title = title.toLowerCase();
        ArrayList<Artist> result = new ArrayList<>();

        for (Artist artist : allArtists) {
            if (artist.getName().toLowerCase().contains(title)) {
                result.add(artist);
            }
        }
        return result;
    }

    /**
     * Sucht Alben nach einem Suchbegriff heraus. Falls der Suchbegriff ein leerer String ist, werden
     * alle Alben zurückgegeben.
     *
     * @param title Der Suchbegriff
     * @return Liste passender Alben
     */
    private ArrayList<Album> findAlbums(String title) {
        if (title.length() == 0) {
            return allAlbums;
        }
        title = title.toLowerCase();
        ArrayList<Album> result = new ArrayList<>();

        for (Album album : allAlbums) {
            if (album.getAlbumName().toLowerCase().contains(title)) {
                result.add(album);
            }
        }
        return result;
    }

    /**
     * Setzt den Suchmodus.
     *
     * @param searchMode True, wenn Suchlisten verwendet werden sollen.
     */
    public void setSearchMode(boolean searchMode) {
        if (!searchMode) {
            //Wenn deaktiviert
            displayTracks = allTracks;
            displayAlbums = allAlbums;
            displayArtists = allArtists;
            musicAdapter.notifyDataSetChanged();
        }
    }

    public void updateSearchString(String text) {
        switch (displayMode) {
            case MODE_TITLE:
                displayTracks = findTracks(text);
                break;
            case MODE_ALBUM:
                displayAlbums = findAlbums(text);
                break;
            case MODE_ARTIST:
                displayArtists = findArtists(text);
                break;
        }
        musicAdapter.notifyDataSetChanged();
    }

    /**
     * Adapter zum Darstellen der Musiktitel in der Liste
     */
    private class MusicAdapter extends RecyclerView.Adapter<ViewHolder> {

        /**
         * Erstellt einen neuen MusicAdapter
         */
        public MusicAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Wird so oft ausgeführt, wie ViewHolder auf den Bildschirm passen
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_music, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //Bindet einen neuen Track an einen bestehenden ViewHolder
            switch (displayMode) {
                case MODE_TITLE:
                    Track track;
                    track = displayTracks.get(position);
                    holder.bindTrack(track);
                    break;
                case MODE_ALBUM:
                    Album album;
                    album = displayAlbums.get(position);
                    holder.bindAlbum(album);
                    break;
                case MODE_ARTIST:
                    Artist artist;
                    artist = displayArtists.get(position);
                    holder.bindArtist(artist);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            switch (displayMode) {
                case MODE_TITLE:
                    return displayTracks.size();
                case MODE_ALBUM:
                    return displayAlbums.size();
                case MODE_ARTIST:
                    return displayArtists.size();
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
        final TextView titleView;
        final TextView subTitleView;

        Track track;
        Album album;
        Artist artist;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.titleText);
            subTitleView = (TextView) itemView.findViewById(R.id.subTitleText);

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
                    //Datei abspielen
                    File f = new File(track.getPath());
                    Uri uri = Uri.fromFile(f);
                    System.out.println(uri);
                    mListener.getServerStatus().playFile(uri, listener);
                    break;
                case MODE_ALBUM:
                    //Album anzeigen
                    FragmentManager fragmentManager = mListener.getSupportFragmentManager();
                    AlbumFragment fragment = (AlbumFragment) fragmentManager.findFragmentByTag(AlbumFragment.FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new AlbumFragment();
                    }
                    fragment.setArguments(listener, album);

                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.frag_enter, R.anim.frag_exit, R.anim.frag_pop_enter, R.anim.frag_pop_exit)
                            .replace(R.id.main_fragment_container, fragment, AlbumFragment.FRAGMENT_TAG)
                            .addToBackStack(AlbumFragment.FRAGMENT_TAG)
                            .commit();
                    break;
                case MODE_ARTIST:
                    //Künstler anzeigen
                    fragmentManager = mListener.getSupportFragmentManager();
                    ArtistFragment artistFragment = (ArtistFragment) fragmentManager.findFragmentByTag(ArtistFragment.FRAGMENT_TAG);
                    if (artistFragment == null) {
                        artistFragment = new ArtistFragment();
                    }
                    artistFragment.setArguments(listener, artist);

                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.frag_enter, R.anim.frag_exit, R.anim.frag_pop_enter, R.anim.frag_pop_exit)
                            .replace(R.id.main_fragment_container, artistFragment, ArtistFragment.FRAGMENT_TAG)
                            .addToBackStack(ArtistFragment.FRAGMENT_TAG)
                            .commit();
                    break;
            }
        }
    }
}
