package de.teyzer.genie.ui;

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
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.model.Album;
import de.teyzer.genie.model.Artist;
import de.teyzer.genie.model.Track;
import de.teyzer.genie.ui.custom.ArtistFragment;

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

    ArrayList<Track> searchTracks;
    ArrayList<Artist> searchArtists;
    ArrayList<Album> searchAlbums;

    ArrayList<Track> displayTracks;
    ArrayList<Artist> displayArtists;
    ArrayList<Album> displayAlbums;

    public void setTrackMode(MusicFragment parentFragment, ArrayList<Track> tracks) {
        this.parentFragment = parentFragment;
        this.displayMode = MODE_TITLE;
        displayTracks = tracks;
    }

    public void setAlbumMode(MusicFragment parentFragment, ArrayList<Album> albums) {
        this.parentFragment = parentFragment;
        this.displayMode = MODE_ALBUM;
        displayAlbums = albums;
    }

    public void setArtistMode(MusicFragment parentFragment, ArrayList<Artist> artists) {
        this.parentFragment = parentFragment;
        this.displayMode = MODE_ARTIST;
        displayArtists = artists;
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
     * Sucht Tracks zu einem Titel heraus. Falls der Suchbegriff ein leerer String ist, werden
     * alle Tracks zurückgegeben.
     *
     * @param title Der Suchtitel
     * @return Liste passender Tracks
     */
    public ArrayList<Track> findTracks(String title) {
        if (title.length() == 0) {
            return displayTracks;
        }

        title = title.toLowerCase();
        ArrayList<Track> result = new ArrayList<>();

        for (Track track : displayTracks) {
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
    public ArrayList<Artist> findArtists(String title) {
        if (title.length() == 0) {
            return displayArtists;
        }

        title = title.toLowerCase();
        ArrayList<Artist> result = new ArrayList<>();

        for (Artist artist : displayArtists) {
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
    public ArrayList<Album> findAlbums(String title) {
        if (title.length() == 0) {
            return displayAlbums;
        }
        title = title.toLowerCase();
        ArrayList<Album> result = new ArrayList<>();

        for (Album album : displayAlbums) {
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
        this.searchMode = searchMode;
        isEmptySearchString = true;
        if (!searchMode) {
            musicAdapter.notifyDataSetChanged();
        }
    }

    public void updateSearchString(String text) {
        if (text.length() == 0) {
            isEmptySearchString = true;
        } else {
            isEmptySearchString = false;
            switch (displayMode) {
                case MODE_TITLE:
                    searchTracks = findTracks(text);
                    break;
                case MODE_ALBUM:
                    searchAlbums = findAlbums(text);
                    break;
                case MODE_ARTIST:
                    searchArtists = findArtists(text);
                    break;
            }
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
                    if (searchMode && !isEmptySearchString) {
                        track = searchTracks.get(position);
                    } else {
                        track = displayTracks.get(position);
                    }
                    holder.bindTrack(track);
                    break;
                case MODE_ALBUM:
                    Album album;
                    if (searchMode && !isEmptySearchString) {
                        album = searchAlbums.get(position);
                    } else {
                        album = displayAlbums.get(position);
                    }
                    holder.bindAlbum(album);
                    break;
                case MODE_ARTIST:
                    Artist artist;
                    if (searchMode && !isEmptySearchString) {
                        artist = searchArtists.get(position);
                    } else {
                        artist = displayArtists.get(position);
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
                        return displayTracks.size();
                    }
                case MODE_ALBUM:
                    if (searchMode && !isEmptySearchString) {
                        return searchAlbums.size();
                    } else {
                        return displayAlbums.size();
                    }
                case MODE_ARTIST:
                    if (searchMode && !isEmptySearchString) {
                        return searchArtists.size();
                    } else {
                        return displayArtists.size();
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

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            titleView = (TextView) itemView.findViewById(R.id.titleText);
            subTitleView = (TextView) itemView.findViewById(R.id.subTitleText);

            this.itemView = itemView;

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
                    FragmentManager fragmentManager = mListener.getSupportFragmentManager();
                    AlbumFragment fragment = (AlbumFragment) fragmentManager.findFragmentByTag(AlbumFragment.FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new AlbumFragment();
                    }
                    fragment.setArguments(parentFragment, album);

                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.frag_enter, R.anim.frag_exit, R.anim.frag_pop_enter, R.anim.frag_pop_exit)
                            .replace(R.id.main_fragment_container, fragment, AlbumFragment.FRAGMENT_TAG)
                            .addToBackStack(AlbumFragment.FRAGMENT_TAG)
                            .commit();
                    break;
                case MODE_ARTIST:
                    fragmentManager = mListener.getSupportFragmentManager();
                    ArtistFragment artistFragment = (ArtistFragment) fragmentManager.findFragmentByTag(ArtistFragment.FRAGMENT_TAG);
                    if (artistFragment == null) {
                        artistFragment = new ArtistFragment();
                    }
                    artistFragment.setArguments(parentFragment, artist);

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
