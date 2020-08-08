package com.hichip.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
/**
 * 不能拖拽的SeekBar
 * @author lt
 */
public class CannotDragSeekBar extends SeekBar {
	public CannotDragSeekBar(Context context) {
		super(context);
	}

	public CannotDragSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.seekBarStyle);
	}

	public CannotDragSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * onTouchEvent 是在 SeekBar 继承的抽象类 AbsSeekBar 里 你可以看下他们的继承关系
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 原来是要将TouchEvent传递下去的,我们不让它传递下去就行了
		// return super.onTouchEvent(event);

		return false;
	}

}
