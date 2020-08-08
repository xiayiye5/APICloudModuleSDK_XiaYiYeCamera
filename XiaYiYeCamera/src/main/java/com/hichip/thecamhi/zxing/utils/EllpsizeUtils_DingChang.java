package com.hichip.thecamhi.zxing.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public class EllpsizeUtils_DingChang {

	/**
	 * 字符串的单个字符是否是汉�?
	 * 
	 * @param c
	 *            单个字符
	 * @return 字符对应的ASCIIS 值， 负�?? 是汉字；
	 */
	public static int ascii(String c) {
		byte x[] = new byte[2];// 这里是两个元�?
		x = c.getBytes();// 按照原有�? 编码格式生成字节数组�?

		// x=c.getBytes("utf-8");// 按照�?么编码格式生�? 字节数组�?
		// x=c.getBytes(srcBegin, srcEnd, dst, dstBegin);

		if (x == null || x.length > 2 || x.length <= 0) {// 没有字符，为空字符串（空格也是字符串�?
			return -1;
		}
		if (x.length == 1) {// 英文字符
			return 1;
		}
		Pattern p=Pattern.compile("[\u4e00-\u9fa5]");
	    Matcher m=p.matcher(c);
	     if(m.matches()){
//	      Toast.makeText(Main.this,"输入的是汉字", Toast.LENGTH_SHORT).show();
	    	 return -1;
	     }

		return 0;
	}

	/**
	 * 去掉首位空格，做其它处理
	 * 
	 * @param string
	 * @return
	 */
	public static String goodStr(String string) {

		string = string.trim();
		return string;
	}
	/**
	 * 判断 有多少个 汉字  长度 取整�?
	 * @param string
	 * @return
	 */
	public static int letterSum(String string) {
		if (null != string) {
			string = goodStr(string);// 这个函数是干�?么用处的？去�? 首位空格
			if (string.length() <= 0) {
				return 0;
			} else {
				String str;
				double len = 0;
				for (int i = 0; i < string.length(); i++) {
					// 是否是汉�? ascii<0;
					str = string.substring(i, i + 1);
					if (ascii(str) < 0) {
						len++;
					} else {
						len += 0.5;
					}
					
				}
				Log.e("num", (int) Math.round(len)+";  len="+ len);
				return (int) Math.round(len);
			}
		}
		return 0;

	}
	
	/**
	 * 判断有几个汉字，不是长度�? 可以修改成： 英文字符有几�?
	 * @param string
	 * @return
	 */
	public static int chineseSum(String string) {
		if (!TextUtils.isEmpty(string)) {//字符串内容不不为�?
			string = goodStr(string);// 这个函数是干�?么用处的？去�? 首位空格
			if (string.length() <= 0) {
				return 0;
			} else {
				String str;
				double len = 0;
				for (int i = 0; i < string.length(); i++) {
					// 是否是汉�? ascii<0;
					str = string.substring(i, i + 1);
					if (ascii(str) < 0) {//是汉�?
						len++;
					}
//					else {//不是汉字
//						len += 0.5;
//					}
					
				}
				Log.e("num", (int) Math.round(len)+";  len="+ len);
				return (int) Math.round(len);
			}
		}
		return 0;
	}
	/**
	 * 获取 多少�? 字符�?
	 * 
	 * @param string
	 *            字符串数据，
	 * @param size
	 *            要获取的长度 ( 是长�? 不是字符个数，是长度)：中文为�?个，英文�?0.5�?
	 *            注意：假设有十个长度：全为中文，则为10个汉字；20个字母， 若某字符串字符数小于 10，则该字符串没有达到省略要求�?
	 * @return
	 */
	public static String limitStr(String string, int size) {// 要多长的 字符�?
		if (null != string) {
			string = goodStr(string);// 这个函数是干�?么用处的？去�? 首位空格
			if (string.length() <= size) {
				return string;
			} else {
				StringBuffer buffer = new StringBuffer();
				String str;
				double len = 0;
				for (int i = 0; i < string.length(); i++) {
					// 是否是汉�? ascii<0;
					str = string.substring(i, i + 1);
					if (ascii(str) < 0) {
						buffer.append(str);
						len++;
					} else {
						buffer.append(str);
						len += 0.5;
					}
					if (len >= size)
						break;
				}
				return buffer.toString();
			}
		}
		return "";

	}

	/**
	 * 获取 以特�? 字符串endStr 为结尾的字符串， 
	 * 
	 * @param strData 字符串数�?
	 * @param size
	 * @param endStr
	 * @return  返回以特定省略符号为结尾的字符串
	 */
	public static String limitStr_Ending(String strData, int size, String endStr) {
		strData = goodStr(strData);//去掉首位空格
		if (size < endStr.length() || strData.length() < endStr.length()) {// 结尾的字符串过长，子�?
																			// 结尾的字符串为主
			Log.e("endStr is too long","endStr is too long! Please cut it.");
		}
		String  cutStr;
		cutStr = limitStr(strData, size);
		if (cutStr.length()!=strData.length()) {//如果字符串被裁减�? 则执行下面操�?
			cutStr = cutStr.substring(0, cutStr.length() - letterSum(endStr))+ endStr;
		}

		return cutStr;
	}
}
