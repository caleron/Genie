package de.teyzer.genie.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.Album;
import de.teyzer.genie.model.Track;

public class AlbumFragment extends Fragment {

    DataProvider mListener;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.album_track_list)
    RecyclerView trackListView;

    private MusicAdapter musicAdapter;
    private AlbumFragment albumFragment;
    private MusicFragment musicFragment;

    private Album displayAlbum;
    private ArrayList<Track> displayTracks;

    public AlbumFragment() {
        albumFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar);
        mListener.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Titel setzen
        toolbar.setTitle(displayAlbum.getAlbumName());

        RecyclerView.LayoutManager mListLayoutManager = new LinearLayoutManager(getActivity());
        trackListView.setLayoutManager(mListLayoutManager);

        musicAdapter = new MusicAdapter();
        trackListView.setAdapter(musicAdapter);

        return root;
    }

    public void setArguments(MusicFragment parentFragment, Album album) {
        musicFragment = parentFragment;
        displayAlbum = album;
        displayTracks = album.getTracks();
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
        TextView titleView;
        TextView subTitleView;

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
            mListener.getServerConnect().executeAction(Action.playFile(uri, musicFragment, musicFragment));
        }
    }
}