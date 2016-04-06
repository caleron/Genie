package de.teyzer.genie.ui.fragments;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;
import de.teyzer.genie.ui.custom.SpectrogramView;

public class FrequencyFragment extends AbstractFragment {
    public static final String FRAGMENT_TAG = "frequency_fragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.frequency_spectrogram)
    SpectrogramView spectrogramView;
    @Bind(R.id.frequency_layout)
    LinearLayout linearLayout;

    private FFTTask fftTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_frequency, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        fftTask = new FFTTask();
        fftTask.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        fftTask.interrupt();
    }

    @Override
    public View getMainLayout() {
        return linearLayout;
    }

    private class FFTTask extends Thread {

        final int audioSource = MediaRecorder.AudioSource.MIC;
        final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        final int BLOCK_SIZE = 2048;
        final int SAMPLE_RATE = 44100;

        @Override
        public void run() {
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, channelConfig, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(audioSource, SAMPLE_RATE, channelConfig, audioEncoding, bufferSize);

            short[] buffer = new short[BLOCK_SIZE];

            try {
                audioRecord.startRecording();
            } catch (Throwable t) {
                System.out.println("Recording failed");
            }

            while (!isInterrupted()) {
                audioRecord.read(buffer, 0, BLOCK_SIZE);

                List<Map.Entry<Double, Double>> amps = analyzeChannel(buffer);

                updateProgress(amps);
            }

            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                System.out.println("stop failed");
            }
        }

        private List<Map.Entry<Double, Double>> analyzeChannel(short[] samples) {
            int sampleFrame = samples.length;

            double[] real = new double[sampleFrame];
            double[] imag = new double[sampleFrame];

            for (int i = 0; i < sampleFrame; i++) {
                real[i] = samples[i];
                imag[i] = 0;
            }

            FFT fft = new FFT(BLOCK_SIZE);
            fft.fft(real, imag);

            double[] amps = new double[sampleFrame / 2];

            //nur die erste hälfte ist wichtig, der Rest ist "gespiegelt"
            for (int i = 0; i < sampleFrame / 2; i++) {
                double amp = Math.hypot(real[i], imag[i]) / sampleFrame;

                amps[i] = amp;
            }

            List<Map.Entry<Double, Double>> result = new ArrayList<>();
            for (int i = 0; i < amps.length; i++) {
                //Frequenz entspricht SAMPLE_RATE / sampleFrame * Index
                double freq = ((1.0 * SAMPLE_RATE) / (1.0 * sampleFrame)) * i;

                result.add(new AbstractMap.SimpleEntry<>(freq, amps[i]));
            }
            return result;
        }

        private void updateProgress(List<Map.Entry<Double, Double>> values) {
            if (spectrogramView != null) {
                spectrogramView.updateList(values);
            }
        }
    }


    /**
     * http://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html
     */
    private class FFT {
        final int n, m;

        // Lookup tables.  Only need to recompute when size of FFT changes.
        double[] cos;
        double[] sin;

        public FFT(int n) {
            this.n = n;
            this.m = (int) (Math.log(n) / Math.log(2));

            // Make sure n is a power of 2
            if (n != (1 << m))
                throw new RuntimeException("FFT length must be power of 2");

            // precompute tables
            cos = new double[n / 2];
            sin = new double[n / 2];

            for (int i = 0; i < n / 2; i++) {
                cos[i] = Math.cos(-2 * Math.PI * i / n);
                sin[i] = Math.sin(-2 * Math.PI * i / n);
            }
        }

        /***************************************************************
         * fft.c Douglas L. Jones University of Illinois at Urbana-Champaign January 19, 1992
         * http://cnx.rice.edu/content/m12016/latest/
         * <p/>
         * fft: in-place radix-2 DIT DFT of a complex input
         * <p/>
         * input: n: length of FFT: must be a power of two m: n = 2**m input/output x: double array of length n with real
         * part of data y: double array of length n with imag part of data
         * <p/>
         * Permission to copy and use this program is granted as long as this header is included.
         ****************************************************************/
        public void fft(double[] x, double[] y) {
            int i, j, k, n1, n2, a;
            double c, s, e, t1, t2;


            // Bit-reverse
            j = 0;
            n2 = n / 2;
            for (i = 1; i < n - 1; i++) {
                n1 = n2;
                while (j >= n1) {
                    j = j - n1;
                    n1 = n1 / 2;
                }
                j = j + n1;

                if (i < j) {
                    t1 = x[i];
                    x[i] = x[j];
                    x[j] = t1;
                    t1 = y[i];
                    y[i] = y[j];
                    y[j] = t1;
                }
            }

            // FFT
            n1 = 0;
            n2 = 1;

            for (i = 0; i < m; i++) {
                n1 = n2;
                n2 = n2 + n2;
                a = 0;

                for (j = 0; j < n1; j++) {
                    c = cos[a];
                    s = sin[a];
                    a += 1 << (m - i - 1);

                    for (k = j; k < n; k = k + n2) {
                        t1 = c * x[k + n1] - s * y[k + n1];
                        t2 = s * x[k + n1] + c * y[k + n1];
                        x[k + n1] = x[k] - t1;
                        y[k + n1] = y[k] - t2;
                        x[k] = x[k] + t1;
                        y[k] = y[k] + t2;
                    }
                }
            }
        }
    }
}
