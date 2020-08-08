package com.hichip.thecamhi.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.hichip.base.HiLog;

import android.app.ActivityManager;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

public class MemoryInfo {
    public static void displayBriefMemory(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        HiLog.e("系统剩余内存:" + (info.availMem >> 10) + "k");
        HiLog.e("系统是否处于低内存运行：" + info.lowMemory);
        HiLog.e("当系统剩余内存低于" + info.threshold + "时就看成低内存运行");
    }

    public static String getAvailMemory(Context context) {// 获取android当前可用内存大小

        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    public static String getTotalMemory(Context context) {
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";// 返回1GB/2GB/3GB/4GB
    }

}