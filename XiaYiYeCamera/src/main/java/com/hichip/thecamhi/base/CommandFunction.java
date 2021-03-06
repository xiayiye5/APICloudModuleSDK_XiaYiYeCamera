package com.hichip.thecamhi.base;

import com.hichip.base.HiLog;
import com.hichip.content.HiChipDefines.HI_P2P_FUNCTION;

public class CommandFunction
{
	
	private final static int cmd_list[] = {

			0x00001001,	   //开始码流
			0x00001002,	   //停止码流
			0x0000f001,  //流控
			0x0000f002,
			0x00003101,   //视频参数
			0x00003102,
			0x00003107,   //视频制式
			0x00003108,


			0x00003109,	   //OSD
			0x00003110,
			0x0000410a,  //视频编码
			0x0000410b,
			0x00009106,  //最大码流、分辨率
			0x0000411c,	  //分辨率
			0x0000411d,
			0x00003103,   //开始音频


			0x00003104,   //停止音频
			0x00001011,	   //开始对讲
			0x00001012,   //停止对讲
			0x00003111,   //音频参数
			0x00003112,
			0x00003105,	   //图像参数
			0x00003106,
			0x00003207,   //恢复默认


			0x00004112,	  //高级图像参数
			0x00004113,
			0x00002001,   //录像列表
			0x00002000,	  //查询有录像的天数
			0x00002002,  //停止获取录像列表
			0x00002003,   //回放控制
			0x00009105,   //手动抓拍
			0x00004101,   //网络参数


			0x00004102,
			0x00004106,	   //RTSP参数
			0x00004107,
			0x00004114,	   //onvif参数
			0x00004115,
			0x00004103,   //wifi参数
			0x00004104,
			0x00004105,   //wifi列表


			0x0000410c,   //wifi测试
			0x00008101,   //云台控制
			0x0000411e,	  //云台控制扩展
			0x00008103,   //云台预置位
			0x0000411f,	  //云台预置位扩展
			0x00008104,   //云台参数
			0x00008105,
			0x00008106,  //485云台参数


			0x00008107,
			0x00004118,	   //云台指示灯
			0x00004119,
			0x00005107,   //报警参数
			0x00005108,
			0x00005101,   //移动侦测
			0x00005102,
			0x00005103,   //IO报警


			0x00005104,
			0x00005105,   //音频报警
			0x00005106,
			0x00005109,   //报警时间
			0x00005110,
			0x00004110,   //报警抓拍参数
			0x00004111,
			0x00004108,	   //视频遮挡


			0x00004109,
			0x00001000,		//登陆
			0x00007105,   //用户管理
			0x00007106,
			0x00006101,   //定时抓拍
			0x00006102,
			0x00006103, //定时抓拍时间
			0x00006104,



			0x00006105,   //定时录像参数
			0x00006106,
			0x00006107,   //定时录像时间
			0x00006108,
			0x00009107,   //红外灯
			0x00009103,
			0x00007107,   //时间
			0x00007108,


			0x00007116,   //时区
			0x00007117,
			0x00007109,   //NTP参数
			0x00007110,
			0x00007114,   //厂商信息
			0x00007111,   //设备信息
			0x00004117,   //系统信息拓展
			0x00007112,   //sd信息


			0x00007113,   //sd格式化
			0x0000411b,   //sd格式化扩展
			0x0000f005,	  //能力集
			0x00009104,   //继电器控制
			0x00009101,   //重启
			0x00009102,   //恢复出厂设置
			0x00007101,   //FTP参数
			0x00007102,


			0x0000410e,   //FTP拓展
			0x0000410f,
			0x00007103,   //Email参数
			0x00007104,
			0x0000410d,  //Email拓展
			0x00004116,	  //获取系统日志
			0x0000411a,	  //停止获取系统日志
			0x00004801,	  //设备升级设置


			0x00004121,	//有报警推送功能
			0x00004122,		//开始上传录像文件
			0x00004123,		//停止上传录像文件
			0x00004124,		//暂停上传录像文件
			0x00004125,	//录像回放拖动
			0x00004126,		//灯状态获取
			0x00004127,		//灯状态设置
			0x00004128,		//输入报警状态获取

//		第15个字节的8个功能：
			0x00004129,		//输入报警状态设置
			0x0000412a,	//白光灯模式获取扩展
			0x0000412b,		//白光灯模式设置扩展
			0x0000412c,		//I帧丢失错误修复
			0x0000412d,	 	//获取录像列表(按时间降序)
			0x0000412e,    //报警录像时长
			0x0000412f,
			0x00004130,		//获取预置位状态


//		第16个字节的8个功能：
			0x00004131,		//开始上传录像文件
			0x00004132,		//报警推送注册
			0x00004133,		//报警推送注销
			0x00004134,		//获取温湿度
			0x00004135,		//设置PIO投影灯
			0x00004136,		//获取MP3列表
			0x00004137,		//播放MP3
			0x00004138,	    //获取报警日志

//		第17个字节的8个功能：
			0x00004139,		//删除报警日志
			0x0000413a,		//设置TIO灯光
			0x0000413b,		//修改用户名
			0x0000413d,	 //录像回放搜索(时间)
			0x0000414e,	 //获取录像列表(文件名)
			0x0000414f,		//获取RF报警状态
			0x00004150,		//RF报警开关设置
			0x00004151,		//单个RF参数获取

			//第18个字节的8个功能：
			0x00004152,		//单个RF参数设置
			0x00004153,		//所有RF参数获取
			0x00004154,  //开始RF对码,获取码值
			0x00004155,		//获取温度报警范围
			0x00004156,		//设置温度报警范围
			0x00004157,		//获取湿度报警范围
			0x00004158,		//设置湿度报警范围
			0x00004159,		//在0x0000413d基础上去掉夏令时
			//第19个字节的8个功能：

			0x0000415a,	//在0x0000414e基础上去掉夏令时(尚未使用)
			0x0000415b,	//报警总开关
			0x0000415c,
			0x0000415d,	 //回放新接口(支持H265)
			0x0000415e,		//获取报警服务器地址
			0x0000415f,
			0x00004160,
			0x00004161,
			//第20个字节的8个功能：
			0x00004162,
			0x00004163,
			0x00004164,
			0x00004165,
			0x00004166, /*设置用户名密码扩展接口(用户名密码256)*/
			0x00004167, /*获取用户名密码扩展接口(用户名密码256)*/
			0x00004168,/*获取EMAIL参数扩展接口(用户名密码256)*/
			0x00004169, /*设置EMAIL参数扩展接口(用户名密码256)*/
			//第21个字节的8个功能：
			0x0000416a,/*获取FTP参数扩展接口(用户名密码256)*/
			0x00000416b,/*设置FTP参数扩展接口(用户名密码256)*/
			0x00000416c,/*获取wifi参数扩展接口(用户名密码256)*/
			0x00000416e,/*设置wifi参数扩展接口(用户名密码256)*/
			/************新增2017.08.01智能追踪************/
			0x00000416f, /*设置智能追踪参数*/
			0x000004170, /*获取智能追踪参数*/
			//#define HI_P2P_PB_PLAY_FAST 0x000004171 /*设置快速播放*/
			/************新增2017.08.24定时重启功能akl*******/
			0x000004171, /*设置定时重启参数*/
			0x000004172, /*获取定时重启参数*/
			//第22个字节的8个功能：
			/************新增2017.09.04录像回放*******/
			0x00004173,
			/************新增2017.09.04报警日志获取扩展接口*******/
			0x000004174,
			0x000004175,
			0x000004176,
			0x000004177,
			0x000004178,  /*2017 11 01 fish eye  */
			0x000004179,/*2017 12 27 fish eye  */
			0x00000417a,
			//第23个字节的8个功能：
			0x00000417b,
			0x00000417c,
			0x00000417d,    /*获取是否为全字符集接口，只需判断能力集*/
			0x00000417e,/*获取纯白光灯*/
			0x00000417f,/*设置纯白光灯*/
			0x00004180,
			0x00004181,
			0x00004182,
			//第24个字节的8个功能：
			0x00004183,
			0x00004184,
			0x00004185,
			0x00004186,
			0x00004187,
			0x00004188,
			0x00004189,
			0x0000418a,
			//第25个字节的8个功能：
			0x0000418b,
			0x0000418c,
			0x0000418d,
			0x0000418e,
			0x0000418f,
			0x00004190,   //485设置
			0x00004191,//485获取
			0x00004192,//网络指示灯
			//第26个字节的8个功能：
			0x00004193,//
			0x00004194,//email全字符
			0x00004195,//email非全字符
			0x00004196,///*获取声光报警*/
			0x00004197,///*设置声光报警*/
			0x00004198,///*config_capinfo其他参数获取*
			0x00004199,///*获取音响参数*/
			0x0000419a,//*设置音响参数*/
			//
			0x0000419b,/*获取人形识别参数*/
			0x0000419c ,/*设置人形识别参数*/
			0x0000419d, /*设置云存储参数*/
			0x0000419e, /*获取云存储参数*/
			0x0000419f ,/*支持I帧间隔为4*/
			0x000041a0 /*支持wifi check*/



	};


	private final static int func_mod_list[] = {
			0x01,
			0x02,
			0x04,
			0x08,
			0x10,
			0x20,
			0x40,
			0x80
	};


	private HI_P2P_FUNCTION cmd_function = null;



	public void setCmdfunction(HI_P2P_FUNCTION cmd_function) {
		this.cmd_function = cmd_function;
	}

	public boolean getCmdFunction(int cmd) {

		boolean ret = false;
		if(cmd_function == null) {
			return false;
		}
		HiLog.v("getCmdFunction："+cmd);
		int i = 0;
		int index = 0;
		for(i=0;i<cmd_list.length;i++) {
			if(cmd_list[i] == cmd) {
				index = i;
				ret = true;
				break;
			}
		}
		int step = index / 8;
		int mod = index % 8;

		HiLog.v("getCmdFunction："+step+":::::"+mod);
		if((cmd_function.s32Function[step] & func_mod_list[mod]) != 0 && ret) {
			return true;
		}

		return false;
	}


}
