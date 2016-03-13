package de.teyzer.genie.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.data.DataManager;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.Album;
import de.teyzer.genie.model.Artist;
import de.teyzer.genie.model.Track;

public class MusicListFragment extends Fragment {
    public static final int MODE_TITLE = 0;
    public static final int MODE_ARTIST = 1;
    public static final int MODE_ALBUM = 2;

    @Bind(R.id.music_list)
    RecyclerView trackListView;

    private MusicAdapter musicAdapter;

    private DataProvider mListener;
    private MusicFragment parentFragment;

    private int displayMode;

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

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("MusicListFragment.onResume" + displayMode);
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("MusicListFragment.onPause" + displayMode);
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
                    Track track = dataManager.getTrackAt(position);
                    holder.bindTrack(track);
                    break;
                case MODE_ALBUM:
                    Album album = dataManager.getAlbumAt(position);
                    holder.bindAlbum(album);
                    break;
                case MODE_ARTIST:
                    Artist artist = dataManager.getArtistAt(position);
                    holder.bindArtist(artist);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return dataManager.getTracks().size();
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
