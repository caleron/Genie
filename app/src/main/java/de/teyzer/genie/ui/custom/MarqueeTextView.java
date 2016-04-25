package de.teyzer.genie.ui.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

/**
 * TextView-Klasse, die sich selbst als permanent fokussiert ausgibt, damit die Marquee nicht stoppt
 * <p/>
 * http://stackoverflow.com/questions/1827751/is-there-a-way-to-make-ellipsize-marquee-always-scroll
 */
public class MarqueeTextView extends TextView {

    private Animation marqueeAnimation;

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

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    public void animateTextView() {
        int textWidth = getMeasuredWidth();
        int width = getWidth();

    /* Start animation only when text is longer than dislay width. */
        if (width < textWidth) {
            if (marqueeAnimation != null) {
                marqueeAnimation.cancel();
            }
            marqueeAnimation = new TranslateAnimation(0, width - textWidth, 0, 0);
            marqueeAnimation.setDuration(3000);    // Set custom duration.
            marqueeAnimation.setStartOffset(500);    // Set custom offset.
            marqueeAnimation.setRepeatMode(Animation.REVERSE);    // This will animate text back ater it reaches end.
            marqueeAnimation.setRepeatCount(Animation.INFINITE);    // Infinite animation.

            startAnimation(marqueeAnimation);
        }
    }
}
