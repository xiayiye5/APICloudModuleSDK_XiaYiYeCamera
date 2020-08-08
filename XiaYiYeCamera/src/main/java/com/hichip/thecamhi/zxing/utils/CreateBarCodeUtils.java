package com.hichip.thecamhi.zxing.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.Html;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @version 1.0
 *
 * @author DongXiang
 *
 * @action 生成条形砿
 *
 * @time 2017广3朿21日下卿3:48:56
 *
 */
public class CreateBarCodeUtils {
	/**
	 * 生成条形砿
	 *
	 * @param context
	 * @param dataStr
	 *            霿要生成的内容
	 * @param barCodedWidth
	 *            生成条形码的宽带
	 * @param barCodeHeight
	 *            生成条形码的高度
	 * @param displayCode
	 *            是否在条形码下方显示内容
	 * @return
	 */
	public static Bitmap creatBarcode(Context context, String dataStr,
									  int barCodedWidth, int barCodeHeight, boolean displayCode) {
		Bitmap ruseltBitmap = null;
		/** 图片两端承保留的空白的宽度*/
		int marginW = 10;
		/** 条形码的编码类型*/
		BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

		if (displayCode) {
			Bitmap barcodeBitmap = encodeAsBitmap(dataStr,
					barcodeFormat,
					barCodedWidth + 2 * marginW,
					barCodeHeight);
			Bitmap codeBitmap = creatCodeBitmap(dataStr,
					barCodedWidth + 2* marginW,
					barCodeHeight, context);
			ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(
					0, barCodeHeight));
		} else {
			ruseltBitmap = encodeAsBitmap(dataStr, barcodeFormat,
					barCodedWidth, barCodeHeight);
		}

		return ruseltBitmap;
	}

	/**
	 * 生成条形码的Bitmap; 生成条形码图牿
	 *
	 * @param contents
	 *            霿要生成的内容
	 * @param format
	 *            编码格式
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
										int desiredWidth, int desiredHeight) {
		final int WHITE = 0x000000FF;// 背景颜色 完全透明
		final int BLACK = 0xFF000000;

		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = null;
		try {
			result = writer.encode(contents, format, desiredWidth,
					desiredHeight, null);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 将文孿 生成 文字图片 生成显示编码的Bitmap
	 *
	 * @param contents
	 * @param width
	 * @param height
	 * @param context
	 * @return
	 */
	public static Bitmap creatCodeBitmap(String contents, int width,
										 int height, Context context) {
		TextView tv = new TextView(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(layoutParams);
		tv.setText(contents);
		// tv.setTextSize(Utils.px2sp(context, height/3));
		// tv.setText(Html.fromHtml(contents));
		// tv.setHeight(height);上面布局已经写好亿 参数布局
		tv.setWidth(width);// 但是宽度 还是应该保持和上面图片的宽度丿样的
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setDrawingCacheEnabled(true);
		tv.setTextColor(Color.BLACK);
		tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

		tv.buildDrawingCache();
		Bitmap bitmapCode = tv.getDrawingCache();
		return bitmapCode;
	}

	/**
	 * 将两个Bitmap合并成一丿
	 *
	 * @param first
	 * @param second
	 * @param fromPoint
	 *            第二个Bitmap弿始绘制的起始位置（相对于第一个Bitmap＿
	 * @return
	 */
	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
									   PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		/**
		 * 图片两端承保留的空白的宽度
		 */
		int marginW = 10;
		Bitmap newBitmap = Bitmap.createBitmap(
				// first.getWidth() + second.getWidth() +
				// marginW,//两张图片同等宽度：这是上下组合，不是左右组合＿
				first.getWidth() + marginW * 2,
				first.getHeight() + second.getHeight(), Config.ARGB_4444);// 上下组合，高相加
		Canvas cv = new Canvas(newBitmap);// 创建 丿个画帿
		cv.drawBitmap(first, marginW, 0, null);// 先画第一张图片，起始点：marginW 是空白间隔；画笔
		// 为null
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);// 画笔皿
		// x=0，y=first皿
		// 高；
		cv.save();
		cv.restore();

		return newBitmap;
	}
}
