package com.hichip.thecamhi.zxing.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @version 1.0
 *
 * @author DongXiang
 *
 * @action 生成没有logo的 二维码
 *
 * @time 2017年3月21日下午3:48:45
 *
 */
public class CreateQRCode_Utils {

	/**
	 * 生成一个二维码图像
	 *
	 * @param url
	 *            传入的字符串，通常是一个URL
	 * @param QR_WIDTH
	 *            宽度（像素值px）
	 * @param QR_HEIGHT
	 *            高度（像素值px）
	 * @return
	 */
	public static final Bitmap create2DCoderBitmap(String url, int QR_WIDTH,
												   int QR_HEIGHT) {
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url,
					BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			// 显示到一个ImageView上面
			// sweepIV.setImageBitmap(bitmap);
			return bitmap;
		} catch (WriterException e) {
			Log.e("log", "生成二维码错误" + e.getMessage());
			return null;
		}
	}

	private static final int BLACK = 0xff000000;

	/**
	 * 生成一个二维码图像
	 *
	 * @param url
	 *            传入的字符串，通常是一个URL
	 * @param widthAndHeight
	 *           图像的宽高
	 * @return
	 */
	public static Bitmap createQRCode(String str, int widthAndHeight)
			throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 压缩  制定路径的图片
	 * @param srcPath
	 * @return
	 */
	public static Bitmap compress(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f           这里我写死了尺寸
		float ww = 480f;//这里设置宽度为480f           这里我写死了尺寸
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		return bitmap;//压缩好比例大小后再进行质量压缩
		//return bitmap;
	}
	/**
	 * 压缩已存在的图片对象，并返回压缩后的图片：   返回的图片格式是 PNG，质量100，
	 *
	 * @param bitmap
	 * @param reqsW
	 * @param reqsH
	 * @return
	 */
	public static Bitmap compressBitmap(Bitmap bitmap, int reqsW, int reqsH) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			bitmap.getConfig();
			Log.e("bitmap.getConfig", bitmap.getConfig().toString());
			bitmap.compress(CompressFormat.JPEG, 100, baos);//将图片 转换成对应格式的图片
			byte[] bts = baos.toByteArray();
			Bitmap res = compressBitmap(bts, reqsW, reqsH);
			baos.close();
			return res;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return bitmap;
		}
	}
	/**
	 * 压缩指定byte[]图片，并得到压缩后的图像
	 *
	 * @param bts
	 * @param reqsW
	 * @param reqsH
	 * @return
	 */
	public static Bitmap compressBitmap(byte[] bts, int reqsW, int reqsH) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
		options.inSampleSize = caculateInSampleSize(options, reqsW, reqsH);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(bts, 0, bts.length, options);

	}

	/**
	 * caculate the bitmap sampleSize
	 *
	 * 获取bitmap的压缩比例，
	 *
	 * @param options
	 * @param rqsW
	 * @param rqsH
	 * @return rqsW或rqsH=0 ，则不压缩（返回值是1），获取宽 高压缩比例（ >1 ）===取最小值
	 */
	public static int caculateInSampleSize(Options options, int rqsW, int rqsH) {
		int height = options.outHeight;
		int width = options.outWidth;
		// 主要修改这里，让最宽的对应设定的宽，高同理
		if (width > height) {//保证height 最大
			int temp = width;
			width = height;
			height = temp;
		}
		if (rqsW > rqsH) {//保证rqsh 最大
			int rqsT = rqsH;
			rqsH = rqsW;
			rqsW = rqsT;
		}

		int inSampleSize = 1;
		if (rqsW == 0 || rqsH == 0)
			return 1;
		if (height > rqsH || width > rqsW) {
			int heightRatio = Math.round((float) height / (float) rqsH);
			int widthRatio = Math.round((float) width / (float) rqsW);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}




}
