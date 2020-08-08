package com.hichip.thecamhi.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.activity.VideoLocalActivity;
import com.hichip.thecamhi.activity.VideoOnlineActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;

import java.io.File;

/**
 * @author xiayiye
 */
public class VideoActivity extends HiActivity implements ICameraIOSessionCallback {
    private static int LOCAL_VIDEO_MODEL = 1;
    private static int ONLINE_VIDEO_MODEL = 0;
    private PictureListAdapter pictureAdapter;
    private String[] mState;
    private RadioGroup mRadiGro;
    private boolean mIsLocal = false;
    private ListView picture_fragment_camera_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video);
        initView();
        for (MyCamera myCamera : HiDataValue.CameraList) {
            myCamera.registerIOSessionListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (MyCamera myCamera : HiDataValue.CameraList) {
            myCamera.unregisterIOSessionListener(this);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void initView() {
        picture_fragment_camera_list = (ListView) findViewById(R.id.video_fragment_camera_list);
        mRadiGro = (RadioGroup) findViewById(R.id.vf_rg_loaanddownlad);
        mRadiGro.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.vf_radio_local) {
                    if (!mIsLocal) {
                        startAnimation();
                        mIsLocal = true;
                        pictureAdapter.notifyDataSetChanged();
                    }
                } else if (checkedId == R.id.vf_radio_online) {
                    if (mIsLocal) {
                        startAnimation();
                        mIsLocal = false;
                        pictureAdapter.notifyDataSetChanged();
                    }
                }

            }
        });


        pictureAdapter = new PictureListAdapter(this);
        picture_fragment_camera_list.setAdapter(pictureAdapter);
        picture_fragment_camera_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                MyCamera selectedCamera = HiDataValue.CameraList.get(position);
                Bundle extras = new Bundle();
                extras.putString(HiDataValue.EXTRAS_KEY_UID, selectedCamera.getUid());
                Intent intent = new Intent();
                intent.putExtras(extras);
                // 如果本地则跳到本地录像界面，远程则跳到online录像
                if (!mIsLocal) {
                    if (selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                        intent.setClass(VideoActivity.this, VideoOnlineActivity.class);
                    } else if (selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED
                            || selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                        selectedCamera.connect();
                        return;
                    } else {
                        HiToast.showToast(VideoActivity.this, getString(R.string.click_offline_setting));
                        return;

                    }
                } else {
                    if (getFileCount(selectedCamera.getUid()) > 0) {
                        intent.setClass(VideoActivity.this, VideoLocalActivity.class);
                    } else {
                        HiToast.showToast(VideoActivity.this, getString(R.string.tips_no_file));
                        return;
                    }
                }
                startActivity(intent);
            }
        });

        mState = getResources().getStringArray(R.array.connect_state);
    }

    protected void startAnimation() {
        AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
        animation.setDuration(500);
        picture_fragment_camera_list.setAnimation(animation);

    }

    public void selectModel(int model) {

        if (model == LOCAL_VIDEO_MODEL) {
            pictureAdapter.notifyDataSetChanged();

        } else if (model == ONLINE_VIDEO_MODEL) {
            pictureAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        pictureAdapter.notifyDataSetChanged();

    }

    protected class PictureListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        // public VideoListAdapter(LayoutInflater layoutInflater) {
        // this.mInflater = layoutInflater;
        // }

        public PictureListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);

            // this.mContext = context;
            // this.mInflater = layoutInflater;
        }

        @Override
        public int getCount() {
            return HiDataValue.CameraList.size();
        }

        @Override
        public Object getItem(int position) {
            return HiDataValue.CameraList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyCamera cam = HiDataValue.CameraList.get(position);

            if (cam == null)
                return null;

            ViewHolder holder = null;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.list_video_camera, null);

                holder = new ViewHolder();
                // holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.txt_video_camera_nike = (TextView) convertView.findViewById(R.id.txt_video_camera_nike);
                holder.txt_video_camera_uid = (TextView) convertView.findViewById(R.id.txt_video_camera_uid);
                holder.txt_video_camera_state = (TextView) convertView.findViewById(R.id.txt_video_camera_state);
                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            if (!mIsLocal) {
                holder.txt_video_camera_state.setVisibility(View.VISIBLE);
                if (cam.getConnectState() < 5 && cam.getConnectState() >= 0) {
                    holder.txt_video_camera_state.setText(mState[cam.getConnectState()]);
                }
                int state = cam.getConnectState();
                if (state == -8) {// 也要设置为连接中...
                    holder.txt_video_camera_state.setText(mState[2]);
                }

                switch (state) {
                    case 0:// DISCONNECTED
                        holder.txt_video_camera_state.setTextColor(getResources().getColor(R.color.color_disconnected));
                        break;
                    case -8:
                    case 1:// CONNECTING
                        holder.txt_video_camera_state.setTextColor(getResources().getColor(R.color.color_connecting));
                        break;
                    case 2:// CONNECTED
                        holder.txt_video_camera_state.setTextColor(getResources().getColor(R.color.color_connected));
                        break;
                    case 3:// WRONG_PASSWORD
                        holder.txt_video_camera_state.setTextColor(getResources().getColor(R.color.color_pass_word));
                        break;
                    case 4:// STATE_LOGIN
                        holder.txt_video_camera_state.setTextColor(getResources().getColor(R.color.color_login));
                        break;
                }
            } else {
                holder.txt_video_camera_state.setVisibility(View.GONE);
            }

            String uid = cam.getUid();

            if (holder != null) {
                holder.txt_video_camera_nike.setText(cam.getNikeName());
                if (mIsLocal) {
                    holder.txt_video_camera_uid.setText(uid + "(" + getFileCount(cam.getUid()) + ")");
                } else {
                    holder.txt_video_camera_uid.setText(uid);
                }
            }

            return convertView;

        }

        public final class ViewHolder {
            // public ImageView img;
            public TextView txt_video_camera_nike;
            public TextView txt_video_camera_uid;
            public TextView txt_video_camera_state;
        }
    }

    // 获取文件数量，
    private int getFileCount(String uid) {
        String localPath = HiDataValue.LOCAL_VIDEO_PATH + uid + "/";
        String onlinePath = HiDataValue.ONLINE_VIDEO_PATH + uid + "/";
        File LocalFolder = new File(localPath);
        File OnlineFolder = new File(onlinePath);
        deleteUselessFile(LocalFolder, OnlineFolder);
        String[] videoFiles = null;
        String[] onlineFolders = null;
        if (LocalFolder.exists()) {
            videoFiles = LocalFolder.list();
        }
        if (OnlineFolder.exists()) {
            onlineFolders = OnlineFolder.list();
        }
        int a = videoFiles == null ? 0 : videoFiles.length;
        int b = onlineFolders == null ? 0 : onlineFolders.length;

        return a + b;
    }


    // 删除小于1kb的文件
    private void deleteUselessFile(File LocalFolder, File OnlineFolder) {
        if (LocalFolder.isDirectory() && LocalFolder.listFiles().length > 0) {
            for (File file : LocalFolder.listFiles()) {
                if (file.length() <= 1024 && file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }

        if (OnlineFolder.isDirectory()) {
            for (File file : OnlineFolder.listFiles()) {
                if (file.length() <= 1024 && file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {

    }

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {
        Message message = Message.obtain();
        message.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
        message.arg1 = arg1;
        message.obj = arg0;
        mHandler.sendMessage(message);

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    if (pictureAdapter != null) {
                        pictureAdapter.notifyDataSetChanged();
                    }
                    break;

            }

        }

        ;
    };

}
