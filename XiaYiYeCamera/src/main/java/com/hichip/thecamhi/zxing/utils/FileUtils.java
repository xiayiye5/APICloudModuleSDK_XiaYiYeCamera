package com.hichip.thecamhi.zxing.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
	/**
	 * 获取app 的缓存目�?
	 * 
	 * @param context
	 * @return
	 */
	public static String getCacheDir(Context context) {

		File cacheDir = context.getCacheDir();// 文件�?在目录为getFilesDir();
		String cachePath = cacheDir.getPath();
		return cachePath;
	}
	/**
     * 获取 app 文件存储根目�? 
     * @param context
     * @return
     */
	public static String getFileRoot(Context context) {  
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
            File external = context.getExternalFilesDir(null);  
            if (external != null) {  
                return external.getAbsolutePath();  
            }  
        }  
        return context.getFilesDir().getAbsolutePath();  
    } 
	 
    /**
     * 获得�?个全称路�?  的路�?
     * @param fileName  文件�?+后缀
     * @return
     * 外部存贮路径  String filePath = Environment.getExternalStorageDirectory() + File.separator + "test.jpg"; 
     * 内部缓存	  String dir = FileUtils.getCacheDir(context) + "Image" + File.separator+"test.jpg";
     * 
     */
    public static String getFileAllPath(String fileName){
    	String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
    	return filePath;
    }
	
}
