package com.hichip.thecamhi.utils;

import com.hichip.R;
import com.hichip.thecamhi.base.HiToast;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
/**
 * @author lt
 */
public class SpcialCharFilterWIFISET implements InputFilter {
	private Context mContext;

	public SpcialCharFilterWIFISET(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		String ascCode = stringToAscii(source.toString());
		if (!TextUtils.isEmpty(source)) {
			String[] chars = ascCode.split(",");
			for (int i = 0; i < chars.length; i++) {
				int code=Integer.parseInt(chars[i]);
				//  34 "       38 &        92 \         61 =  
				if(code >32 && code <= 126 && code != 34 && code != 38 && code != 92 && code != 61&&code!=36){//空格  $  老机器也必须限制,空格ASCII码为32,$为36
					continue;
				}else {
					if (start != end) {
						HiToast.showToast(mContext, mContext.getString(R.string.tip_not_spcialchar));
					}
					return "";
				}
			}
		}
		return null;
	}

	
	public String stringToAscii(String value) {
		StringBuffer sbu = new StringBuffer();
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i != chars.length - 1) {
				sbu.append((int) chars[i]).append(",");
			} else {
				sbu.append((int) chars[i]);
			}
		}
		return sbu.toString();
	}

}





