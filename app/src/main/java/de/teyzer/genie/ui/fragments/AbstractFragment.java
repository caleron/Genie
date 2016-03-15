package de.teyzer.genie.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import butterknife.ButterKnife;
import de.teyzer.genie.data.DataProvider;

public class AbstractFragment extends Fragment {

    protected DataProvider mListener;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
