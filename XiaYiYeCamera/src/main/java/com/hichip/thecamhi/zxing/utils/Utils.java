package com.hichip.thecamhi.zxing.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	// 取得版本�?
		public static String getVersionName(Context context) {
			try {
				PackageInfo manager = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				return manager.versionName;
			} catch (NameNotFoundException e) {
				return "Unknown";
			}
		}

		// 取得版本�?
		public static int getVersionCode(Context context) {
			try {
				PackageInfo manager = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				return manager.versionCode;
			} catch (NameNotFoundException e) {
				return -1;
			}
		}
		public static String getPhoneDetails(){
			return "Product Model: "
					+ android.os.Build.MODEL + ","//获取手机型号:HM NOTE 1S,
	                + android.os.Build.VERSION.SDK + ","//  SDK 版本�? : 19
	                + android.os.Build.VERSION.RELEASE;//获取版本�?:4.4.4
		}
		/**
		 * @param context
		 * @param toastContent
		 */
		public static void toastUtilString(Context context ,String toastContent){
			if (toastContent==null) {
				String data=getPhoneDetails()+"\n VersionCode="+getVersionCode(context)+"\n getVersionName="+getVersionName(context);
				Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
			}else {
				Toast.makeText(context, toastContent, Toast.LENGTH_SHORT).show();
			}
			 
		}
		public static void logEUtils(Context context) {
			String data=getPhoneDetails()+"\n VersionCode="+getVersionCode(context)+"\n getVersionName="+getVersionName(context);
			String packageName=context.getPackageName();
			Log.e(packageName,data );
		}
		public static void logICommon(Context context,String logStrData) {
			Log.i(context.getPackageName(), logStrData+"**");
		}
		
		
		/**
		 * �? dp 转成px
		 *  从dimens.xml 获取�? 尺寸都是  px，即使你写的是dp 和sp
		 * @param dp
		 * @param context
		 * @return
		 */
		public static int  dp2Pix(int dp,Context context) {
			
			 float scale = context.getResources().getDisplayMetrics().density;
			 	
//			 	int widthPx=200*2;
			 int pix=(int) (dp*scale + 0.5f);
			
			return pix;
		}
		 /** 
		  * 
         * 将px值转换为dip或dp值，保证尺寸大小不变 
         *  从dimens.xml 获取�? 尺寸都是  px，即使你写的是dp 和sp
         * @param pxValue 
         * @param scale 
         *            （DisplayMetrics类中属�?�density�? 
         * @return 
         */  
        public static int px2dip(Context context, float pxValue) {  
            final float scale = context.getResources().getDisplayMetrics().density;  
            Log.e("scale==", scale+"*******");
            return (int) (pxValue / scale + 0.5f);  
        }  
      
        /** 
         * 将dip或dp值转换为px值，保证尺寸大小不变 
         *  从dimens.xml 获取�? 尺寸都是  px，即使你写的是dp 和sp
         * @param dipValue 
         * @param scale 
         *            （DisplayMetrics类中属�?�density�? 
         * @return 
         */  
        public static int dip2px(Context context, float dipValue) {  
            final float scale = context.getResources().getDisplayMetrics().density;  
            Log.e("scale==", scale+"*******");
            return (int) (dipValue * scale + 0.5f);  
        }  
      
        /** 
         * 将px值转换为sp值，保证文字大小不变 
         *  从dimens.xml 获取�? 尺寸都是  px，即使你写的是dp 和sp
         * @param pxValue 
         * @param fontScale 
         *            （DisplayMetrics类中属�?�scaledDensity�? 
         * @return 
         */  
        public static int px2sp(Context context, float pxValue) {  
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
            Log.e("scale==", fontScale+"*******");
            return (int) (pxValue / fontScale + 0.5f);  
        }  
      
        /** 
         * 将sp值转换为px值，保证文字大小不变 
         *  从dimens.xml 获取�? 尺寸都是  px，即使你写的是dp 和sp
         * @param spValue 
         * @param fontScale 
         *            （DisplayMetrics类中属�?�scaledDensity�? 
         * @return 
         */  
        public static int sp2px(Context context, float spValue) {  
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
            Log.e("scale==", fontScale+"*******");
            return (int) (spValue * fontScale + 0.5f);  
        } 
}
