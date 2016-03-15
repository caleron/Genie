package de.teyzer.genie.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;

public class UploadStatusDialogFragment extends DialogFragment {
    public static final String FRAGMENT_TAG = "upload_status_dialog_fragment";

    @Bind(R.id.upload_dialog_title)
    TextView title;
    @Bind(R.id.upload_dialog_progressbar)
    ProgressBar progressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.dialog_upload_status, null);
        ButterKnife.bind(this, rootView);

        builder.setView(rootView);

        builder.setCancelable(false);
        setCancelable(false);

        return builder.create();
    }

    private void cancel() {
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
    }

    public void updateStatus(final String text, final int progressPercent) {
        if (title == null)
            return;

        //Auf UI-Thread ausführen, falls dies nicht UI-Thread ist
        if (Looper.myLooper() != Looper.getMainLooper()) {
            title.post(new Runnable() {
                @Override
                public void run() {
                    updateStatus(text, progressPercent);
                }
            });
        } else {
            title.setText(text);
            progressBar.setProgress(progressPercent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
