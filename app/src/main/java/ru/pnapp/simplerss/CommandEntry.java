/*
 *  Copyright 2016 P.N.Alekseev <pnaleks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pnapp.simplerss;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Комбинация {@link AutoCompleteTextView} и двух {@link ImageButton} для использования
 * в качестве CustomView для {@link android.app.ActionBar}
 * <ul>
 *     <li>кнопка слева - play/stop</li>
 *     <li>кнопка справа - clear</li>
 * </ul>
 *
 *
 * @author P.N. Alekseev
 * @author pnaleks@gmail.com
 */
public class CommandEntry extends FrameLayout {
    public enum ACTION {STOP, PLAY}

    private ImageButton mButtonLeft;
	private ImageButton mButtonRight;
	private AutoCompleteTextView mTextView;
	
	private Drawable mDrawablePlay;
    private Drawable mDrawableStop;

	private int[] mPadding = new int[4];
	private int   mWidePadding;
	
	private boolean hasFocus;
    private ACTION mAction = ACTION.PLAY;

    private Context mContext;
	
	private DisplayMetrics mMetrics = new DisplayMetrics();

	public CommandEntry(Context context) { super(context); CreateView(context); }
	public CommandEntry(Context context, AttributeSet attrs) { super(context, attrs); CreateView(context); }
	public CommandEntry(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); CreateView(context); }
	
	private void CreateView(Context context) {
        mContext = context;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.entry, this);

		mButtonLeft   = (ImageButton) v.findViewById(R.id.left);
		mButtonRight  = (ImageButton) v.findViewById(R.id.right);
		mTextView     = (AutoCompleteTextView) v.findViewById(R.id.text);

        mTextView.setOnKeyListener( new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_BACK && v.hasFocus() ) {
                    if (  event.getAction() == KeyEvent.ACTION_UP ) v.clearFocus();
                    return true;
                }
                return false;
            }
        });

        mDrawablePlay    = ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_white_24dp);
        mDrawableStop    = ContextCompat.getDrawable(mContext, R.drawable.ic_stop_white_24dp);

		mButtonLeft.setImageDrawable(mDrawablePlay);
		mButtonRight.setImageResource(R.drawable.ic_clear_white_24dp);

		mTextView.setOnFocusChangeListener(mOnFocusChangeListener);
		mTextView.setOnEditorActionListener(mOnEditorActionListener);

		mPadding[0] = mTextView.getPaddingLeft();
		mPadding[1] = mTextView.getPaddingTop();
		mPadding[2] = mTextView.getPaddingRight();
		mPadding[3] = mTextView.getPaddingBottom();

		mWidePadding = context.getResources().getDimensionPixelSize(R.dimen.action_button_min_width);

		mOnFocusChangeListener.onFocusChange(mTextView, false);

		mButtonRight.setOnClickListener( new OnClickListener() { @Override public void onClick(View v) { onRightButton(); } });
		
		mButtonLeft.setOnClickListener( new OnClickListener() { @Override public void onClick(View v) { onLeftButton(); } });
		
		((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mMetrics);
	}
	
	public void setText(CharSequence text) {
		mTextView.setText(text);
	}

	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		mTextView.setAdapter(adapter);
	}
	
    private void setAction() {
        switch( mAction ) {
            case STOP:
                mButtonLeft.setImageDrawable(mDrawableStop);
                mTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case PLAY:
                mButtonLeft.setImageDrawable(mDrawablePlay);
                mTextView.setImeOptions(EditorInfo.IME_ACTION_GO);
                break;
        }
    }

	public void setAction(ACTION action) {
        mAction = action;
        setAction();
	}

    public void toggleAction() {
        mAction = (mAction == ACTION.PLAY) ? ACTION.STOP : ACTION.PLAY;
        setAction();
    }

	private void onLeftButton() {
        if( mContext instanceof Callback ) {
            switch( mAction ) {
                case STOP:
                    ((Callback) mContext).onActionStop();
                    break;
                case PLAY:
                    String str = mTextView.getText().toString();
                    if ( !str.isEmpty() ) ((Callback) mContext).onActionPlay(str);
                    break;
            }
        } else {
            toggleAction();
        }
	}
	
	private void onRightButton() {
		if (hasFocus) {
			mTextView.setText("");
		}
	}
	
	OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			CommandEntry.this.hasFocus = hasFocus;
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (CommandEntry.this.hasFocus) {
                imm.showSoftInput(mTextView, InputMethodManager.SHOW_FORCED);
				mButtonLeft.setVisibility(INVISIBLE);
				mButtonRight.setVisibility(VISIBLE);
				mTextView.setPadding(mPadding[0], mPadding[1], mPadding[2], mPadding[3]);
			} else {
                imm.hideSoftInputFromWindow(mTextView.getWindowToken(), 0);
				mButtonLeft.setVisibility(VISIBLE);
				mButtonRight.setVisibility(INVISIBLE);
				mTextView.setPadding(mWidePadding, mPadding[1], mPadding[2], mPadding[3]);
			}
		}
	};
	
	OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            v.clearFocus();
			if ( mAction == ACTION.PLAY ) { onLeftButton(); }
			return true;
		}
	};
	
    /**
     * Интерфейс для получения откликов от {@link CommandEntry}. Контекст должен поддерживать этот
     * интерфейс чтобы получать отклики.
     */
	public interface Callback {
        /**
         * Вызывается когда пользователь нажимает кнопку play
         * @param text текущий текст в поле ввода
         */
        void onActionPlay(String text);

        /**
         * Вызывается когда пользователь нажимает кнопку stop
         */
        void onActionStop();
    }
}
