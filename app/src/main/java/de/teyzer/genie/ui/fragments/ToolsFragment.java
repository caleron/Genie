package de.teyzer.genie.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.teyzer.genie.R;
import de.teyzer.genie.scanner.IntentIntegrator;
import de.teyzer.genie.scanner.IntentResult;

public class ToolsFragment extends AbstractFragment {
    public static final String FRAGMENT_TAG = "tools_fragment";

    @Bind(R.id.toolbar)
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar);

        return root;
    }

    @OnClick({R.id.batch_scan_btn, R.id.tools_freq_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.batch_scan_btn:
                startScan();
                break;
            case R.id.tools_freq_btn:
                AbstractFragment frag = (AbstractFragment) mListener.getSupportFragmentManager().findFragmentByTag(FrequencyFragment.FRAGMENT_TAG);
                if (frag == null) {
                    frag = new FrequencyFragment();
                }
                mListener.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, frag, FrequencyFragment.FRAGMENT_TAG)
                        .addToBackStack(FrequencyFragment.FRAGMENT_TAG)
                        .commit();
                break;
        }
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Wird ausgelöst, wenn eine via startActivityForResult gestartete Activity fertig ist

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            //Scan-result bearbeiten
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null) {
                mListener.getServerStatus().sendString(scanResult.getContents());
                //neuen scan starten
                startScan();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
