package com.tulusha.timetracker.widgets;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author marco
 * Workaround to be able to scroll text inside a TextView without it required
 * to be focused. For some strange reason there isn't an easy way to do this
 * natively.
 * 
 * Original code written by Evan Cummings:
 * http://androidbears.stellarpc.net/?p=185
 */
public class ScrollingTextView extends TextView {

	public ScrollingTextView(Context context, AttributeSet attrs,
	        int defStyle) {
    	super(context, attrs, defStyle);
    }

    public ScrollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollingTextView(Context context) {
        super(context);
        this.setMovementMethod(new ScrollingMovementMethod());
        this.setMaxLines(6);
    }
}
