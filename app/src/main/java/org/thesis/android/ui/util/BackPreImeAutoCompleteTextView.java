package org.thesis.android.ui.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

public class BackPreImeAutoCompleteTextView extends AutoCompleteTextView {
    private Runnable mTask = null;

    public BackPreImeAutoCompleteTextView(Context context) {
        super(context);
    }

    public BackPreImeAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackPreImeAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackPressedAdditionalTask(Runnable task) {
        mTask = task;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (mTask != null && keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent
                .ACTION_UP) {
            this.post(mTask);
        }

        return super.onKeyPreIme(keyCode, event);
    }
}
