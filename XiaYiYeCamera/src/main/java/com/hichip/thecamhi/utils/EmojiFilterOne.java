package com.hichip.thecamhi.utils;

import java.util.HashSet;
import java.util.Set;
import android.text.InputFilter;
import android.text.Spanned;
/**
 * EditText过滤器——过滤表情和中文字
 * @author lt
 */
public class EmojiFilterOne implements InputFilter {
	private static Set<String> filterSet = null;
	static {
		filterSet = new HashSet<String>();
		
		// See http://apps.timwhitlock.info/emoji/tables/unicode
		
		// 1F601 - 1F64F
		addUnicodeRangeToSet(filterSet, 0x1F601, 0X1F64F);
		
		// 2702 - 27B0
		addUnicodeRangeToSet(filterSet, 0x2702, 0X27B0);
		
		// 1F680 - 1F6C0
		addUnicodeRangeToSet(filterSet, 0X1F680, 0X1F6C0);
		
		// 24C2 - 1F251
		addUnicodeRangeToSet(filterSet, 0X24C2, 0X1F251);
		
		// 1F600 - 1F636
		addUnicodeRangeToSet(filterSet, 0X1F600, 0X1F636);
		
		// 1F681 - 1F6C5
		addUnicodeRangeToSet(filterSet, 0X1F681, 0X1F6C5);
		
		// 1F30D - 1F567
		addUnicodeRangeToSet(filterSet, 0X1F30D, 0X1F567);
		
		// not included 5. Uncategorized
		
	}

    private static void addUnicodeRangeToSet(Set<String> set, int start, int end) {
        if (set == null) {
            return;
        }
        if (start > end) {
            return;
        }

        for (int i = start; i <= end; i++) {
            filterSet.add(new String(new int[] {
                    i
            }, 0, 1));
        }
    }

    public EmojiFilterOne() {
        super();
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
            int dend) {
        // check black-list set
        if (filterSet.contains(source.toString())) {
            return "";
        }
        return source;
    }
}
    
