package com.hichip.thecamhi.bean;

import com.hichip.tools.Packet;

public class CamHiDefines {


    public static final int HI_P2P_ALARM_TOKEN_REGIST = 0x00004132;/*报警推送注册，请在首次连接设置时间后调用*/
    public static final int HI_P2P_ALARM_TOKEN_UNREGIST = 0x00004133;    /*报警推送注销*/
    public static final int HI_P2P_GET_OSD_PARAM = 0x00003109;
    public static final int HI_P2P_SET_OSD_PARAM = 0x00003110;

    public static final int HI_P2P_ALARM_ADDRESS_GET = 0x0000415e;    /*获取报警服务器地址*/
    public static final int HI_P2P_ALARM_ADDRESS_SET = 0x0000415f;    /*设置报警服务器地址*/


    public static final int HI_P2P_CUSTOM_ALARM = 0x0000418c;    /*自定义录音*/
    public static final int HI_P2P_GET_SMART_HSR_PARAM = 0x0000419b;    /*获取人形识别参数*/
    public static final int HI_P2P_SET_SMART_HSR_PARAM = 0x0000419c;    /*设置人形识别参数*/
    public static final int HI_P2P_ALARM_HSR = 0x0000f006;    /*人形识别*/
    public static final int HI_P2P_SUPPORT_WIFICHECK = 0x000041a0;/*支持wifi check*/
    public static final int HI_P2P_SDK_TOKEN_LEN = 68;

    /******HI_P2P_ALARM_TOKEN_REGIST******/
    public static class HI_P2P_ALARM_TOKEN_INFO {
        int u32Chn;
        byte[] szTokenId = new byte[HI_P2P_SDK_TOKEN_LEN];
        int u32UtcTime;
        byte s8Enable;
        public byte sReserved[] = new byte[3];


        public static byte[] parseContent(int u32Chn, int szTokenId, int u32UtcTime, int enable) {
            byte[] info = new byte[77];

            byte[] bChannel = Packet.intToByteArray_Little(u32Chn);
            byte[] bToken = Packet.intToByteArray_Little(szTokenId);
            byte[] bUtcTime = Packet.intToByteArray_Little(u32UtcTime);
            byte[] bEnable = Packet.intToByteArray_Little(enable);


            System.arraycopy(bChannel, 0, info, 0, 4);
            System.arraycopy(bToken, 0, info, 4, 4);
            System.arraycopy(bUtcTime, 0, info, 72, 4);
            System.arraycopy(bEnable, 0, info, 76, 1);


            return info;
        }

        public HI_P2P_ALARM_TOKEN_INFO(byte[] byt) {
            u32Chn = Packet.byteArrayToInt_Little(byt, 0);
            System.arraycopy(byt, 0, szTokenId, 4, HI_P2P_SDK_TOKEN_LEN);
            u32UtcTime = Packet.byteArrayToInt_Little(byt, 72);
            s8Enable = byt[76];

        }

    }
    /******HI_P2P_ALARM_TOKEN_UNREGIST******/
    ///**************************HI_P2P_ALARM_TOKEN_REGIST***************************/
    //#define HI_P2P_ALARM_TOKEN_MAX	64	/*�����豸��ʹ�ã����token����*/
    //typedef struct
    //{
    //	HI_U32 u32Chn;				/*ipc :0*/
    //	HI_CHAR szTokenId[68];		/*token*/
    //	HI_U32 u32UtcTime;			/*�ͻ��˵�ǰutcʱ��(��λСʱ,�� /3600 )*/
    //	HI_S8 s8Enable;				/*app�˱��������Ƿ��, 1: ��*/
    //	HI_S8 sReserved[3];
    //}HI_P2P_ALARM_TOKEN_INFO;
    ///**************************HI_P2P_ALARM_TOKEN_UNREGIST***************************/


    /****************HI_P2P_GET_OSD_PARAM  HI_P2P_SET_OSD_PARAM*******************/
    public static class HI_P2P_S_OSD {
        int u32Chn;
        int u32EnTime;
        int u32EnName;
        int u32PlaceTime;
        int u32PlaceName;
        public byte[] strName = new byte[64];

        public static byte[] parseContent(int u32Chn, int u32EnTime, int u32EnName, int u32PlaceTime, int u32PlaceName, String name) {
            byte[] osd = new byte[84];

            byte[] bChn = Packet.intToByteArray_Little(u32Chn);
            byte[] bEnTime = Packet.intToByteArray_Little(u32EnTime);
            byte[] bEnName = Packet.intToByteArray_Little(u32EnName);
            byte[] bPlaceTime = Packet.intToByteArray_Little(u32PlaceTime);
            byte[] bPlaceName = Packet.intToByteArray_Little(u32PlaceName);
            byte[] bName = name.getBytes();

            System.arraycopy(bChn, 0, osd, 0, 4);
            System.arraycopy(bEnTime, 0, osd, 4, 4);
            System.arraycopy(bEnName, 0, osd, 8, 4);
            System.arraycopy(bPlaceTime, 0, osd, 12, 4);
            System.arraycopy(bPlaceName, 0, osd, 16, 4);
            System.arraycopy(bName, 0, osd, 20, bName.length > 64 ? 64 : bName.length);
            return osd;
        }


        public HI_P2P_S_OSD(byte[] byt) {
            u32Chn = Packet.byteArrayToInt_Little(byt, 0);
            u32EnTime = Packet.byteArrayToInt_Little(byt, 4);
            u32EnName = Packet.byteArrayToInt_Little(byt, 8);
            u32PlaceTime = Packet.byteArrayToInt_Little(byt, 12);
            u32PlaceName = Packet.byteArrayToInt_Little(byt, 16);

            System.arraycopy(byt, 20, strName, 0, strName.length > 64 ? 64 : strName.length);


        }


    }

    //	/****************HI_P2P_GET_OSD_PARAM  HI_P2P_SET_OSD_PARAM*******************/
    //	typedef struct
    //	{
    //	    HI_U32 u32Channel;/*ipc: 0*/
    //	    HI_U32 u32EnTime; /*ʱ��,0 :close  !0 :open*/
    //	    HI_U32 u32EnName; /*����,0 :close  !0 :open*/
    //	    HI_U32 u32PlaceTime;/*ʱ������*/
    //	    HI_U32 u32PlaceName;/*��������*/
    //	    HI_CHAR strName[64];
    //	} HI_P2P_S_OSD;
    //	/****************HI_P2P_GET_OSD_PARAM  HI_P2P_SET_OSD_PARAM*******************/


    public static class HI_P2P_ALARM_ADDRESS {
        public byte[] szAlarmAddr = new byte[32];
        byte[] sReserved = new byte[4];

        public static byte[] parseContent(String szAlarmAddr) {
            byte[] addr = new byte[32];

            byte[] bAlarmAddr = szAlarmAddr.getBytes();


            System.arraycopy(bAlarmAddr, 0, addr, 0, bAlarmAddr.length > 32 ? 32 : bAlarmAddr.length);

            return addr;
        }

        public HI_P2P_ALARM_ADDRESS(byte[] data) {
            System.arraycopy(data, 0, szAlarmAddr, 0, data.length > 32 ? 32 : data.length);
        }

    }


    //
    //	/********************HI_P2P_ALARM_ADDRESS_GET*********************/
    //	typedef struct
    //	{
    //		HI_CHAR szAlarmAddr[32];	/*������������ַ*/
    //		HI_CHAR sReserved[4];
    //	}HI_P2P_ALARM_ADDRESS;
    //	/********************HI_P2P_ALARM_ADDRESS_SET*********************/

    //	**********************HI_P2P_GET_VIDEO_IMAGE_PARAM_SCOPE***********************/
    //	typedef struct
    //	{
    //		HI_S32 profile;//视频编码从右至左分别表示baseline,mainprofile,highprofile,h265,如1:baseline ;7:baseline/mainprofile/highprofile;15:全支持
    //		HI_CHAR resolution[32];//分辨率;此字段列举出所有能修改的分辨率值,以分号";"间隔,如只出现一个值则表示只能是此分辨率,不可修改;如出现多个值，则表示不同分辨率
    //		HI_CHAR brightness[16];//亮度范围
    //		HI_CHAR saturation[16]; //饱和度范围
    //		HI_CHAR contrast[16]; //对比度范围
    //		HI_CHAR hue[16]; //色度范围
    //		HI_U8 u8Reserve[4];
    //
    //	}HI_P2P_VIDEO_IMAGE_PARAM_SCOPE;
    public static class HI_P2P_VIDEO_IMAGE_PARAM_SCOPE {
        int profile;
        public byte[] resolution = new byte[32];
        byte[] brightness = new byte[16];
        byte[] saturation = new byte[16];
        byte[] contrast = new byte[16];
        byte[] hue = new byte[16];
        byte[] u8Reserve = new byte[4];

        public HI_P2P_VIDEO_IMAGE_PARAM_SCOPE(byte[] byt) {
            if (byt.length >=36) {
                System.arraycopy(byt, 4, resolution, 0, resolution.length > 32 ? 32 : resolution.length);
            }
        }
    }

    public static class HI_P2P_GET_SMART_HSR_PARAM {
        public int u32HSRenable;/*智能人形识别*/
        public int u32DrawRect;/*框住人形*/
        public int u32Link;/*联动开关*/

        public byte[] parseContent() {
            byte[] result = new byte[16];
            byte[] u32HSRenable = Packet.intToByteArray_Little(this.u32HSRenable);
            byte[] u32DrawRect = Packet.intToByteArray_Little(this.u32DrawRect);
            byte[] u32Link = Packet.intToByteArray_Little(this.u32Link);
            System.arraycopy(u32HSRenable, 0, result, 0, 4);
            System.arraycopy(u32DrawRect, 0, result, 4, 4);
            System.arraycopy(u32Link, 0, result, 8, 4);
            return result;
        }

        public HI_P2P_GET_SMART_HSR_PARAM(byte[] byt) {
            if (byt.length >= 16) {
                this.u32HSRenable = Packet.byteArrayToInt_Little(byt, 0);
                this.u32DrawRect = Packet.byteArrayToInt_Little(byt, 4);
                this.u32Link = Packet.byteArrayToInt_Little(byt, 8);
            }

        }
    }
    //    typedef struct
    //    {
    //        HI_U32 u32Type;
    //        HI_U32 u32Num;
    //        HI_P2P_ALARM_MD hsr[0];
    //    }HI_P2P_SMART_HSR_AREA;

    public static class HI_P2P_SMART_HSR_AREA  {
        public int u32Type;
        public int u32Num;

        public HI_P2P_SMART_HSR_AREA(byte[] byt) {
            if (byt.length >= 8) {
                this.u32Type = Packet.byteArrayToInt_Little(byt, 0);
                this.u32Num = Packet.byteArrayToInt_Little(byt, 4);
            }
        }
    }


    //    typedef struct
    //    {
    //        HI_U32 u32Area;
    //        HI_U32 u32X;
    //        HI_U32 u32Y;
    //        HI_U32 u32Width;
    //        HI_U32 u32Height;
    //    } HI_P2P_ALARM_MD;
    public static class HI_P2P_ALARM_MD {
        public int u32Area;
        public int u32X;
        public int u32Y;
        public int u32Width;
        public int u32Height;

        public HI_P2P_ALARM_MD(byte[] byt) {
            if (byt.length >= 20) {
                this.u32Area = Packet.byteArrayToInt_Little(byt, 0);
                this.u32X = Packet.byteArrayToInt_Little(byt, 4);
                this.u32Y = Packet.byteArrayToInt_Little(byt, 8);
                this.u32Width = Packet.byteArrayToInt_Little(byt, 12);
                this.u32Height = Packet.byteArrayToInt_Little(byt, 16);
            }
        }

        @Override
        public String toString() {
            return "HI_P2P_ALARM_MD{" +
                    ", u32Area=" + u32Area +
                    ", u32X=" + u32X +
                    ", u32Y=" + u32Y +
                    ", u32Width=" + u32Width +
                    ", u32Height=" + u32Height +
                    '}';
        }
    }

}
