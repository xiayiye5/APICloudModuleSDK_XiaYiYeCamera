package com.hichip.customview.dialog;

import com.nineoldandroids.animation.ObjectAnimator;

import android.view.View;

public class RotateRight extends BaseEffects {

	@Override
	protected void setupAnimation(View view) {
		getAnimatorSet().playTogether(
                ObjectAnimator.ofFloat(view, "rotationY", 90, 0).setDuration(mDuration),
                ObjectAnimator.ofFloat(view, "translationX", 300, 0).setDuration(mDuration),
                ObjectAnimator.ofFloat(view, "alpha", 0, 1).setDuration(mDuration*3/2)

        );
	}

}
