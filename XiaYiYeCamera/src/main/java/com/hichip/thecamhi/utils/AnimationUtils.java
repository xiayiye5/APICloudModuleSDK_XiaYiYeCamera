package com.hichip.thecamhi.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class AnimationUtils {
	private AnimationUtils(){
	}
	public static void scaleAnim(View view,long durationMillis){
		ScaleAnimation scaleAnim=new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		scaleAnim.setDuration(durationMillis);
		scaleAnim.setFillAfter(true);
		view.startAnimation(scaleAnim);
	}
}
