package com.hichip.thecamhi.zxing.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
/**
 * @version 1.0
 * @action 生成带有logo的 二维码
 * @time 2017年3月21日下午3:48:32
 */
public class CreateQRCode_LogoUtils {
	/**
	 * 生成二维码Bitmap ,保存到指定路径 带有logo的 二维码
	 *
	 * @param content
	 *            内容
	 * @param widthPix
	 *            二维码 图片宽度
	 * @param heightPix
	 *            二维码 图片高度
	 * @param logoBm
	 *            二维码中心的Logo图标（可以为null）
	 * @param filePath
	 *            用于存储二维码图片的文件路径
	 * @return 生成二维码及保存文件是否成功
	 */
	public static boolean create_Logo_QRImage(String content, int widthPix, int heightPix, Bitmap logoBm, String filePath) {
		try {
			if (content == null || "".equals(content)) {
				return false;
			}
			// //配置参数
			Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 容错级别
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			// 设置空白边距的宽度
			// hints.put(EncodeHintType.MARGIN, 2); //default is 4

			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
			int[] pixels = new int[widthPix * heightPix];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < heightPix; y++) {
				for (int x = 0; x < widthPix; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * widthPix + x] = 0xff000000;
					} else {
						pixels[y * widthPix + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix); // 此处生成二维码 完毕

			// 下面就是讲Logo 添加到 二维码中
			if (logoBm != null) {
				bitmap = addLogo(bitmap, logoBm);
			}

			// 必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
			File file=new File(filePath);
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			if(file.exists()){
				file.delete();
			}
			return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static final int BLACK = 0xff000000;

	/**
	 * 在二维码中间添加Logo图案
	 */
	private static Bitmap addLogo(Bitmap src, Bitmap logo) {
		if (src == null) {
			return null;
		}

		if (logo == null) {
			return src;
		}

		// 获取图片的宽高
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();
		int logoWidth = logo.getWidth();
		int logoHeight = logo.getHeight();

		if (srcWidth == 0 || srcHeight == 0) {
			return null;
		}

		if (logoWidth == 0 || logoHeight == 0) {
			return src;
		}

		// logo大小为二维码整体大小的1/3
		float scaleFactor = srcWidth * 1.0f / 7 / logoWidth;
		Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
		try {
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(src, 0, 0, null);
			canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
			canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

			canvas.save();

			canvas.restore();
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}

		return bitmap;
	}

	/**
	 * 从指定路径获取 bitmap
	 *
	 * @param strPath
	 *            全称路径=str+文件名.jpg；若不知道，可以参照getFileAllPath() 写法
	 * @param inSampleSize
	 *            压缩比例 intSample<=1 则图片将不做任何处理
	 * @return 不做处理的 bitmap
	 */
	public static Bitmap getBitmapFromPath(String strPath, int inSampleSize) {

		if (inSampleSize <= 1) {
			inSampleSize = 1;
		}
		Options options = new Options();
		options.inSampleSize = inSampleSize;
		return BitmapFactory.decodeFile(strPath, options);
	}

}
// class FileUtils {
// /**
// * 获取app 的缓存目录
// *
// * @param context
// * @return
// */
// public static String getCacheDir(Context context) {
//
// File cacheDir = context.getCacheDir();// 文件所在目录为getFilesDir();
// String cachePath = cacheDir.getPath();
// return cachePath;
// }
// /**
// * 获取 app 文件存储根目录
// * @param context
// * @return
// */
// public static String getFileRoot(Context context) {
// if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
// File external = context.getExternalFilesDir(null);
// if (external != null) {
// return external.getAbsolutePath();
// }
// }
// return context.getFilesDir().getAbsolutePath();
// }
//
// /**
// * 获得一个全称路径 的路径
// * @param fileName 文件名+后缀
// * @return
// * 外部存贮路径 String filePath = Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
// * 内部缓存 String dir = FileUtils.getCacheDir(context) + "Image" + File.separator+"test.jpg";
// *
// */
// public static String getFileAllPath(String fileName){
// String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
// return filePath;
// }
//
// }
