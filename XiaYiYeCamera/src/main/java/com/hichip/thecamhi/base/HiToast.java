package com.hichip.thecamhi.base;

import android.content.Context;
import android.widget.Toast;

import com.hichip.thecamhi.widget.toast.ToastCompat;

public class HiToast {
	private static ToastCompat toast;
	public static void showToast(Context context,String str){
		if(toast==null){
			toast=ToastCompat.makeText(context, str, Toast.LENGTH_SHORT);
		}else {
			toast.setText(str);
		}
		toast.show();
	}
}
