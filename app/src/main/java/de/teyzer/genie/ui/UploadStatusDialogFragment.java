package de.teyzer.genie.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.teyzer.genie.R;

public class UploadStatusDialogFragment extends DialogFragment {
    public static final String FRAGMENT_TAG = "upload_status_dialog_fragment";

    private TextView title;
    private ProgressBar progressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.upload_status_dialog, null);

        builder.setView(rootView);

        title = (TextView) rootView.findViewById(R.id.upload_dialog_title);
        progressBar = (ProgressBar) rootView.findViewById(R.id.upload_dialog_progressbar);
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
}
