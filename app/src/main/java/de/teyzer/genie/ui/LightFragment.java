package de.teyzer.genie.ui;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
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
public class LightFragment extends AbstractFragment implements View.OnClickListener, ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {
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
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    public LightFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_light, container, false);
        ButterKnife.bind(this, root);

        mListener.setSupportActionBar(toolbar);

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

        lightColorChooseModeRadioGroup.setOnCheckedChangeListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        revalidateColorMode();
        revalidateManuallyMode();
    }

    @OnClick(R.id.light_rgb_manually_switch)
    public void onClick(View view) {
        revalidateManuallyMode();
        boolean musicMode = !lightRgbManuallySwitch.isChecked();
        mListener.getServerConnect().executeAction(Action.setColorMode(musicMode, null));

    }

    private void revalidateColorMode() {
        if (lightColorPickerModeRadioButton.isChecked()) {
            //Colorpicker anzeigen
            lightRgbModeBox.setVisibility(View.GONE);
            lightColorPickerModeBox.setVisibility(View.VISIBLE);

            lightColorPicker.setColor(Color.rgb(lightRedSeekbar.getProgress(),
                    lightGreenSeekbar.getProgress(), lightBlueSeekbar.getProgress()));

        } else {
            //RGB-Schieber anzeigen
            lightRgbModeBox.setVisibility(View.VISIBLE);
            lightColorPickerModeBox.setVisibility(View.GONE);

            int color = lightColorPicker.getColor();
            lightRedSeekbar.setProgress(Color.red(color));
            lightGreenSeekbar.setProgress(Color.green(color));
            lightBlueSeekbar.setProgress(Color.blue(color));
        }
    }

    private void revalidateManuallyMode() {
        boolean musicMode = !lightRgbManuallySwitch.isChecked();
        if (musicMode) {
            lightColorManuallyBox.setVisibility(View.GONE);
        } else {
            lightColorManuallyBox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        //wird ausgelöst, wenn der Farbmodus verändert wurde
        revalidateColorMode();
    }

    @Override
    public void onColorChanged(int color) {
        //System.out.println("changed color = [" + color + "]    " + lightColorPicker.getColor());
        mListener.getServerConnect().executeAction(Action.setColor(color, null));
    }

    @Override
    public void onColorSelected(int color) {
        //System.out.println("selected color = [" + color + "]" + "    " + lightColorPicker.getColor());
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
