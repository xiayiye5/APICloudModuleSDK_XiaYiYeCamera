package com.hichip.thecamhi.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapUtils {


	//剪裁鱼眼的缩略图
	public static Bitmap ImageCrop(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int bmW = bitmap.getWidth();
		int bmH = bitmap.getHeight();
		Log.i("tedu", "--bmW--:"+bmW+"--bmH--:"+bmH);
		int radius = bmH / 2;
		int rectLen = (int) (radius * 0.7 * 2);

		int x = (int) (bmW / 2 - radius * 0.7);
		int y = (int) (radius - radius * 0.7);

		Bitmap bmp = Bitmap.createBitmap(bitmap, x, y, rectLen, rectLen);//圆内切正方形
		if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}

		return bmp;
	}

	/**
	 * 设置图片为圆角
	 * @param bitmap
	 * @param roundPx  圆角角度
	 * @return
	 */
	public  static Bitmap setRoundedCorner(Bitmap bitmap, float roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),  Config.ARGB_4444);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		/*
		 * 椭圆形
		 */
		final RectF rectF = new RectF(rect);
		/*
		 * 去锯齿
		 */
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);

		/*
		 * 绘制圆角矩形
		 */
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		/*
		 * 设置两个图形相交
		 */
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

}










