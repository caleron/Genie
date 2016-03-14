package de.teyzer.genie.ui;

import android.content.Context;
import android.widget.RelativeLayout;

import de.teyzer.genie.R;

public class PlayerBar extends RelativeLayout {
    public PlayerBar(Context context) {
        super(context);
        inflate(context, R.layout.bar_player, this);
    }


}
