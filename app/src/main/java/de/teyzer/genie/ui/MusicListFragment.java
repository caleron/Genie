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
import de.teyzer.genie.model.Track;

public class MusicListFragment extends Fragment {

    @Bind(R.id.music_list)
    RecyclerView trackListView;

    private MusicAdapter musicAdapter;

    private DataProvider mListener;

    private MusicFragment parentFragment;

    public void setParentFragment(MusicFragment parentFragment) {
        this.parentFragment = parentFragment;
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
            Track track = dataManager.getTrackAt(position);
            holder.bindTrack(track);
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
        TextView artistView;
        Track track;

        DataManager dataManager;

        public ViewHolder(View itemView, DataManager dataManager) {
            super(itemView);
            this.itemView = itemView;

            titleView = (TextView) itemView.findViewById(R.id.titleText);
            artistView = (TextView) itemView.findViewById(R.id.artistText);

            this.itemView = itemView;
            this.dataManager = dataManager;

            itemView.setOnClickListener(this);
        }

        public void bindTrack(Track track) {
            this.track = track;

            titleView.setText(track.getTitle());
            artistView.setText(track.getArtist());
        }

        @Override
        public void onClick(View v) {
            File f = new File(track.getPath());
            Uri uri = Uri.fromFile(f);
            System.out.println(uri);
            mListener.getServerConnect().executeAction(Action.playFile(uri, parentFragment, parentFragment));
        }
    }
}
