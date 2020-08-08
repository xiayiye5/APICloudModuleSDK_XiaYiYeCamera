package com.hichip.thecamhi.zxing;

/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.FileNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.zxing.LuminanceSource;
import com.hichip.thecamhi.zxing.utils.CreateQRCode_Utils;


/**
 * This class is used to help decode images from files which arrive as RGB data
 * from Android bitmaps. It does not support cropping or rotation.
 * 
 */
public final class RGBLuminanceSource extends LuminanceSource {
	private final byte[] luminances;

	public RGBLuminanceSource(String path) throws FileNotFoundException {
		this(loadBitmap(path));
	}

	public RGBLuminanceSource(Bitmap bitmap) {
		super(bitmap.getWidth(), bitmap.getHeight());
		//寰楀埌鍥剧墖鐨勫锟??  
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		//寰楀埌鍥剧墖鐨勫儚锟??
		int[] pixels = new int[width * height];
		//
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		// In order to measure pure decoding speed, we convert the entire image
		//涓轰簡娴嬮噺绾В鐮侊拷?锟藉害锛屾垜浠皢鏁翠釜鍥惧儚鐏板害闃靛垪鍓嶉潰锛岃繖鏄竴鏍风殑閫氶亾
		// to a greyscale array
		// up front, which is the same as the Y channel of the
		// YUVLuminanceSource in the real app.
		
		//寰楀埌鍍忕礌澶у皬鐨勫瓧鑺傛暟
		luminances = new byte[width * height];
		//寰楀埌鍥剧墖姣忕偣鍍忕礌棰滆壊
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				//褰撴煇锟??鐐逛笁绉嶉鑹诧拷?锟界浉鍚屾椂锛岀浉搴斿瓧鑺傚搴旂┖闂磋祴锟?? 涓哄叾璧嬶拷??
				if (r == g && g == b) {
					// Image is already greyscale, so pick any channel.
					luminances[offset + x] = (byte) r;
				} else {
					 //鍏跺畠鎯呭喌瀛楄妭绌洪棿瀵瑰簲璧嬶拷?锟戒负锟??
					// Calculate luminance cheaply, favoring green.
					luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
//					luminances[offset + x] = (byte) ((0.299*r + 0.578*g + 0.114*b));
				}
			}
		}
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		System.arraycopy(luminances, y * width, row, 0, width);
		return row;
	}

	// Since this class does not support cropping, the underlying byte array
	// already contains
	// exactly what the caller is asking for, so give it to them without a copy.
	@Override
	public byte[] getMatrix() {
		return luminances;
	}

	private static Bitmap loadBitmap(String path) throws FileNotFoundException {
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		if (bitmap == null) {
			throw new FileNotFoundException("Couldn't open " + path);
		}
		return bitmap;
	}
	
	 /**鎵弿閫夋嫨鐩稿唽鍥剧墖,鍒ゆ柇鏄惁鍘嬬缉鐨勪复鐣岋拷??*/
    public static final Long SCAN_DEFAULT_SIZE=(long)4000000; 
    /**
     * 4000000,鎴戝彂鐜扮殑闂鎵嬫満3000000灏辨寕锟??,
     * 锟??浠ョ粰浜嗚繖涓拷?锟藉叿浣撶殑涓寸晫鍊兼垜涔熶笉鏄緢娓呮,
     * 浣犲彲鑳戒細闂负锟??涔堣缁欎釜涓寸晫鍊肩洏鍒ゆ柇鏄惁鍘嬬缉,閭ｆ槸鍥犱负鏈変簺灏忓浘鍘嬬缉浜嗕笉鑳借В鏋愯瘑锟??,
     * 锟??浠ユ垜杩涜浜嗗垽锟??,澶у浘鍘嬬缉瑙ｆ瀽,灏忓浘鐩存帴瑙ｆ瀽
     * */
	private static Bitmap loadBitmap(String path, boolean isOkSize) throws FileNotFoundException {
        Bitmap bitmap = null;
        if (isOkSize) {  //灏忓浘,鐩存帴璋冪敤鍔熻兘绯荤粺鐨勮В鏋愯繑锟??
            bitmap = BitmapFactory.decodeFile(path);
        } else { //澶у浘 鍏堝帇缂╁湪杩斿洖
            bitmap = CreateQRCode_Utils.compress(path);
        }
        //ZXing婧愮爜
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + path);
        }
        return bitmap;
    }
	
	
}

