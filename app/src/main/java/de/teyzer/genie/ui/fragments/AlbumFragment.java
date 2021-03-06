package de.teyzer.genie.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import de.teyzer.genie.model.Track;
import de.teyzer.genie.ui.custom.PlayerBar;

public class AlbumFragment extends AbstractFragment {
    public static final String FRAGMENT_TAG = "album_fragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.album_track_list)
    RecyclerView trackListView;
    @Bind(R.id.album_player_bar)
    PlayerBar playerBar;
    @Bind(R.id.album_coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private UploadStatusListener listener;

    private Album displayAlbum;
    private ArrayList<Track> displayTracks;
    private MusicAdapter adapter;

    public AlbumFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new MusicAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar, true);

        //Titel setzen
        toolbar.setTitle(displayAlbum.getAlbumName());

        playerBar.setDataProvider(mListener);

        RecyclerView.LayoutManager mListLayoutManager = new LinearLayoutManager(getActivity());
        trackListView.setLayoutManager(mListLayoutManager);

        trackListView.setAdapter(adapter);

        return root;
    }

    public void setArguments(UploadStatusListener listener, Album album) {
        this.listener = listener;
        displayAlbum = album;
        displayTracks = album.getTracks();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Playerstate neu abfragen
        playerBar.requestStatusRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        playerBar.destroyTimer();
    }

    @Override
    public View getMainLayout() {
        return coordinatorLayout;
    }

    @Override
    public void dataSetChanged() {
        adapter.notifyDataSetChanged();
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
            Track track = displayTracks.get(position);
            holder.bindTrack(track);
        }

        @Override
        public int getItemCount() {
            return displayTracks.size();
        }
    }


    /**
     * Verwaltet einen Eintrag in der RecyclerView
     */
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        final TextView titleView;
        final TextView subTitleView;

        Track track;

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

        @Override
        public void onClick(View v) {
            File f = new File(track.getPath());
            Uri uri = Uri.fromFile(f);
            System.out.println(uri);
            mListener.getServerStatus().playFile(uri, listener);
        }
    }
}
