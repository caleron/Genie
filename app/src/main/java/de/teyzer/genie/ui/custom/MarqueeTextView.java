package de.teyzer.genie.ui.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView-Klasse, die sich selbst als permanent fokussiert ausgibt, damit die Marquee nicht stoppt
 * <p/>
 * http://stackoverflow.com/questions/1827751/is-there-a-way-to-make-ellipsize-marquee-always-scroll
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused)
            super.onFocusChanged(true, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused)
            super.onWindowFocusChanged(true);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
