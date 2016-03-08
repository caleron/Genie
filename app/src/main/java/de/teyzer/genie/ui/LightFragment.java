package de.teyzer.genie.ui;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.teyzer.genie.R;
import de.teyzer.genie.connect.Action;
import de.teyzer.genie.data.DataProvider;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightFragment extends Fragment implements View.OnClickListener, ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, SeekBar.OnSeekBarChangeListener {
    public static final String FRAGMENT_TAG = "light_fragment";
    @Bind(R.id.light_white_seekbar)
    SeekBar lightWhiteSeekbar;
    @Bind(R.id.light_rgb_manually_switch)
    Switch lightRgbManuallySwitch;
    @Bind(R.id.light_red_seekbar)
    SeekBar lightRedSeekbar;
    @Bind(R.id.light_green_seekbar)
    SeekBar lightGreenSeekbar;
    @Bind(R.id.light_blue_seekbar)
    SeekBar lightBlueSeekbar;
    @Bind(R.id.light_color_rgb_mode_radio_btn)
    RadioButton lightColorRgbModeRadioBtn;
    @Bind(R.id.light_color_picker_mode_radio_button)
    RadioButton lightColorPickerModeRadioButton;
    @Bind(R.id.light_color_picker)
    ColorPicker lightColorPicker;
    @Bind(R.id.light_color_svbar)
    SVBar lightColorSvbar;
    @Bind(R.id.light_color_choose_mode_radio_group)
    RadioGroup lightColorChooseModeRadioGroup;
    @Bind(R.id.light_red_label)
    TextView lightRedLabel;
    @Bind(R.id.light_rgb_mode_box)
    RelativeLayout lightRgbModeBox;
    @Bind(R.id.light_color_picker_mode_box)
    RelativeLayout lightColorPickerModeBox;
    @Bind(R.id.light_color_manually_box)
    RelativeLayout lightColorManuallyBox;
    private DataProvider mListener;

    public LightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_light, container, false);
        ButterKnife.bind(this, root);

        lightColorPicker.addSVBar(lightColorSvbar);
        lightColorPicker.setShowOldCenterColor(false);

        lightColorPickerModeRadioButton.setChecked(true);
        onClick(lightColorPickerModeRadioButton);

        lightRgbManuallySwitch.setChecked(false);
        onClick(lightRgbManuallySwitch);

        lightColorPicker.setOnColorChangedListener(this);
        lightColorPicker.setOnColorSelectedListener(this);

        lightWhiteSeekbar.setOnSeekBarChangeListener(this);
        lightRedSeekbar.setOnSeekBarChangeListener(this);
        lightGreenSeekbar.setOnSeekBarChangeListener(this);
        lightBlueSeekbar.setOnSeekBarChangeListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataProvider) {
            mListener = (DataProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataProvider");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DataProvider) {
            mListener = (DataProvider) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement DataProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick({R.id.light_color_rgb_mode_radio_btn, R.id.light_color_picker_mode_radio_button, R.id.light_rgb_manually_switch})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.light_color_rgb_mode_radio_btn:
            case R.id.light_color_picker_mode_radio_button:

                if (lightColorPickerModeRadioButton.isChecked()) {
                    lightRgbModeBox.setVisibility(View.GONE);
                    lightColorPickerModeBox.setVisibility(View.VISIBLE);
                } else {
                    lightRgbModeBox.setVisibility(View.VISIBLE);
                    lightColorPickerModeBox.setVisibility(View.GONE);
                }

                break;
            case R.id.light_rgb_manually_switch:
                boolean musicMode = !lightRgbManuallySwitch.isChecked();
                if (musicMode) {
                    lightColorManuallyBox.setVisibility(View.GONE);
                } else {
                    lightColorManuallyBox.setVisibility(View.VISIBLE);
                }

                mListener.getServerConnect().executeAction(Action.setColorMode(musicMode, null));
                break;
        }
    }

    @Override
    public void onColorChanged(int color) {
        mListener.getServerConnect().executeAction(Action.setColor(color, null));
    }

    @Override
    public void onColorSelected(int color) {
        mListener.getServerConnect().executeAction(Action.setColor(color, null));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.light_white_seekbar:
                mListener.getServerConnect().executeAction(Action.setWhiteBrightness(progress, null));
                break;
            case R.id.light_red_seekbar:
            case R.id.light_green_seekbar:
            case R.id.light_blue_seekbar:
                int red, green, blue;

                red = lightRedSeekbar.getProgress();
                green = lightGreenSeekbar.getProgress();
                blue = lightBlueSeekbar.getProgress();

                mListener.getServerConnect().executeAction(Action.setRGBColor(red, green, blue, null));

                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
