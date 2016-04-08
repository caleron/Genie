package de.teyzer.genie.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.ServerStatus;
import de.teyzer.genie.connect.StatusChangedListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightFragment extends AbstractFragment implements ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener, StatusChangedListener {
    public static final String FRAGMENT_TAG = "light_fragment";

    @Bind(R.id.light_white_seekbar)
    SeekBar lightWhiteSeekbar;
    @Bind(R.id.light_color_brightness_seekbar)
    SeekBar colorBrightnessSeekbar;
    @Bind(R.id.light_red_seekbar)
    SeekBar lightRedSeekbar;
    @Bind(R.id.light_green_seekbar)
    SeekBar lightGreenSeekbar;
    @Bind(R.id.light_blue_seekbar)
    SeekBar lightBlueSeekbar;
    @Bind(R.id.light_color_picker)
    ColorPicker lightColorPicker;
    @Bind(R.id.light_color_svbar)
    SVBar lightColorSvbar;
    @Bind(R.id.light_rgb_mode_box)
    RelativeLayout lightRgbModeBox;
    @Bind(R.id.light_color_picker_mode_box)
    RelativeLayout lightColorPickerModeBox;
    @Bind(R.id.light_color_manually_box)
    RelativeLayout lightColorManuallyBox;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.light_color_mode_spinner)
    Spinner colorModeSpinner;
    @Bind(R.id.light_main_layout)
    LinearLayout mainLayout;

    //nötig, damit keine Events gesendet werden, während die Instanz wiederhergestellt wird
    private boolean suppressEvents = true;
    private ServerStatus serverStatus;

    private Handler handler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            suppressServerState = false;
            serverStatusChanged(false);
        }
    };
    /**
     * Während die Seekbars benutzt werden, sollen diese nicht aktualisiert werden, um "springen"
     * zu vermeiden
     */
    private boolean suppressServerState = false;

    public LightFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_light, container, false);
        ButterKnife.bind(this, root);

        serverStatus = mListener.getServerStatus();
        serverStatus.addStatusChangedListener(this);

        mListener.setSupportActionBar(toolbar);

        lightColorPicker.addSVBar(lightColorSvbar);
        lightColorPicker.setShowOldCenterColor(false);

        lightColorPicker.setOnColorChangedListener(this);
        lightColorPicker.setOnColorSelectedListener(this);

        lightWhiteSeekbar.setOnSeekBarChangeListener(this);
        colorBrightnessSeekbar.setOnSeekBarChangeListener(this);
        lightRedSeekbar.setOnSeekBarChangeListener(this);
        lightGreenSeekbar.setOnSeekBarChangeListener(this);
        lightBlueSeekbar.setOnSeekBarChangeListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.color_modes, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorModeSpinner.setAdapter(adapter);
        colorModeSpinner.setOnItemSelectedListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            int colorMode = savedInstanceState.getInt("colorMode", 0);
            colorModeSpinner.setSelection(colorMode);

            lightWhiteSeekbar.setProgress(savedInstanceState.getInt("white"));
            colorBrightnessSeekbar.setProgress(savedInstanceState.getInt("colorBrightness"));
            if (colorMode == 1) {
                //RGB-Modus
                lightRedSeekbar.setProgress(savedInstanceState.getInt("red"));
                lightGreenSeekbar.setProgress(savedInstanceState.getInt("green"));
                lightBlueSeekbar.setProgress(savedInstanceState.getInt("blue"));
            } else if (colorMode == 2) {
                //ColorPicker-Modus
                lightColorPicker.setColor(savedInstanceState.getInt("color"));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (colorModeSpinner != null) {
            int colorMode = colorModeSpinner.getSelectedItemPosition();

            outState.putInt("colorMode", colorMode);
            outState.putInt("white", lightWhiteSeekbar.getProgress());
            outState.putInt("colorBrightness", colorBrightnessSeekbar.getProgress());

            if (colorMode == 1) {
                //Farbkanalmodus
                outState.putInt("red", lightRedSeekbar.getProgress());
                outState.putInt("green", lightGreenSeekbar.getProgress());
                outState.putInt("blue", lightBlueSeekbar.getProgress());
            } else if (colorMode == 2) {
                //ColorPicker-Modus
                outState.putInt("color", lightColorPicker.getColor());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setColorMode(colorModeSpinner.getSelectedItemPosition(), false);
        suppressEvents = false;
        serverStatus.requestNewStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        suppressEvents = true;
    }

    @Override
    public void onColorChanged(int color, boolean fromUser) {
        //System.out.println("changed color = [" + color + "]    " + lightColorPicker.getColor());
        if (suppressEvents || !fromUser)
            return;

        suppressServerState = true;
        handler.removeCallbacks(refreshRunnable);
        handler.postDelayed(refreshRunnable, 1000);

        serverStatus.setRGBColor(color);
    }

    /**
     * Kann eigentlich nur vom User sein. Siehe {@link ColorPicker#onTouchEvent(MotionEvent)}
     */
    @Override
    public void onColorSelected(int color, boolean fromUser) {
        //System.out.println("selected color = [" + color + "]" + "    " + lightColorPicker.getColor());
        if (!fromUser || suppressEvents)
            return;

        suppressServerState = false;
        serverStatus.setRGBColor(color);
    }

    /**
     * Event von den SeekBars für RGB und Weiß
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (suppressEvents || !fromUser)
            return;

        switch (seekBar.getId()) {
            case R.id.light_white_seekbar:
                serverStatus.setWhiteBrightness(progress);
                break;
            case R.id.light_color_brightness_seekbar:
                serverStatus.setColorBrightness(progress);
                break;
            case R.id.light_red_seekbar:
            case R.id.light_green_seekbar:
            case R.id.light_blue_seekbar:
                int red, green, blue;

                red = lightRedSeekbar.getProgress();
                green = lightGreenSeekbar.getProgress();
                blue = lightBlueSeekbar.getProgress();

                serverStatus.setRGBColor(red, green, blue);

                break;
        }
    }

    /**
     * Event von den SeekBars für RGB und Weiß
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        suppressServerState = true;
    }

    /**
     * Event von den SeekBars für RGB und Weiß
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(refreshRunnable);
        handler.postDelayed(refreshRunnable, 1000);

    }

    /**
     * Vom colorModeSpinner
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String mode = setColorMode(position, true);

        if (!suppressEvents) {
            serverStatus.setColorMode(mode);
        }
    }

    @NonNull
    private String setColorMode(int position, boolean setColor) {
        String mode;
        switch (position) {
            case 0: //Musik
                mode = "music";
                lightColorManuallyBox.setVisibility(View.GONE);
                break;

            case 1: //Farbkanäle
                mode = "custom";
                lightColorManuallyBox.setVisibility(View.VISIBLE);

                //RGB-Schieber anzeigen
                lightRgbModeBox.setVisibility(View.VISIBLE);
                lightColorPickerModeBox.setVisibility(View.GONE);

                if (setColor) {
                    int color = lightColorPicker.getColor();
                    lightRedSeekbar.setProgress(Color.red(color));
                    lightGreenSeekbar.setProgress(Color.green(color));
                    lightBlueSeekbar.setProgress(Color.blue(color));
                }
                break;

            case 2: //Farbkreis
                mode = "custom";
                lightColorManuallyBox.setVisibility(View.VISIBLE);

                lightRgbModeBox.setVisibility(View.GONE);
                lightColorPickerModeBox.setVisibility(View.VISIBLE);

                if (setColor) {
                    lightColorPicker.setColor(Color.rgb(lightRedSeekbar.getProgress(),
                            lightGreenSeekbar.getProgress(), lightBlueSeekbar.getProgress()));
                }

                break;

            case 3: //Farbkreis-Animation
                mode = "colorCircle";
                lightColorManuallyBox.setVisibility(View.GONE);
                break;

            default: //nicht möglich
                mode = "";
        }
        return mode;
    }

    /**
     * Vom colorModeSpinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void serverStatusChanged(final boolean newSong) {
        if (colorModeSpinner == null || suppressServerState)
            return;

        //Auf UI-Thread ausführen
        if (Looper.myLooper() != Looper.getMainLooper()) {
            colorModeSpinner.post(new Runnable() {
                @Override
                public void run() {
                    serverStatusChanged(newSong);
                }
            });
            return;
        }

        String colorMode = serverStatus.getColorMode();

        if (colorMode == null)
            return;

        switch (colorMode) {
            case "music":
                colorModeSpinner.setSelection(0);
                break;

            case "custom":
                int currentSelectedItem = colorModeSpinner.getSelectedItemPosition();
                if (currentSelectedItem != 1 && currentSelectedItem != 2) {
                    colorModeSpinner.setSelection(1);
                    currentSelectedItem = 1;
                }

                int color = serverStatus.getCurrentColor();
                if (currentSelectedItem == 1) {
                    //RGB-Modus
                    lightRedSeekbar.setProgress(Color.red(color));
                    lightGreenSeekbar.setProgress(Color.green(color));
                    lightBlueSeekbar.setProgress(Color.blue(color));
                } else {
                    //ColorPicker-Modus
                    lightColorPicker.setColor(color);
                }

                break;

            case "colorCircle":
                colorModeSpinner.setSelection(3);
                break;
        }

        lightWhiteSeekbar.setProgress(serverStatus.getWhiteBrightness());
        colorBrightnessSeekbar.setProgress(serverStatus.getColorBrightness());
    }

    @Override
    public View getMainLayout() {
        return mainLayout;
    }
}
