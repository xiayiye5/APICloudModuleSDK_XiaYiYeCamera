package com.hichip.thecamhi.utils;

import com.hichip.R;
import com.hichip.thecamhi.base.HiToast;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
/**
 * 全字符集和字符串取并集
 * @author lt
 */
public class FullCharUnionFilter implements InputFilter {
	private Context mContext;

	public FullCharUnionFilter(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		String ascCode = stringToAscii(source.toString());
		if (!TextUtils.isEmpty(source)) {
			String[] chars = ascCode.split(",");
			for (int i = 0; i < chars.length; i++) {
				int code=Integer.parseInt(chars[i]);
				if((code >= 32 && code <= 126)||code==8364||code==65509){//￥    符号
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
	// 1:添加摄像机界面和编辑摄像机界面去全字符集和之前字符串的并集。

}




