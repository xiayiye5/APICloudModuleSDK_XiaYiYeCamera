package com.hichip.hichip.activity.Share;

import java.io.File;

import com.hichip.R;
import com.hichip.sdk.HiChipSDK;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.zxing.utils.CreateQRCode_LogoUtils;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ShareQRCodeActivity extends HiActivity implements OnClickListener {
    private TitleView titleView;
    private ImageView iv_qrcode;
    private TextView tv_save_to_phone, tv_share, tv_camera_num;
    private File mFileQrImage;
    private String mContent;
    private int mCameraNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_qrcode);
        getIntentData();
        initView();
        setListerners();
        initData();
    }

    private void getIntentData() {
        mContent = getIntent().getStringExtra("sharelist");
        mCameraNum = getIntent().getIntExtra("camer_num", 1);
    }

    private void setListerners() {
        tv_share.setOnClickListener(this);
        tv_save_to_phone.setOnClickListener(this);

    }

    private void initData() {
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        if (!TextUtils.isEmpty(mContent)) {
            byte[] data = mContent.getBytes();
            byte[] buf = new byte[(int) Math.ceil(data.length / 3.00) * 4 + 32];
            System.arraycopy(data, 0, buf, 0, data.length);
            int ret = HiChipSDK.Aes_Encrypt(buf, data.length);
            String encryStr = getString(R.string.app_name) + "_AC" + new String(buf).trim();
            int widthPix = HiTools.dip2px(ShareQRCodeActivity.this, 285);// 二维码 图片宽度
            int heightPix = HiTools.dip2px(ShareQRCodeActivity.this, 260);// 二维码 图片高度
            mFileQrImage = new File(Environment.getExternalStorageDirectory() + "/share/", HiTools.getFileNameWithTime(0));
            boolean creat_suc = CreateQRCode_LogoUtils.create_Logo_QRImage(encryStr, widthPix, heightPix, getRoundCornerBitmap(logo, 10), mFileQrImage.getAbsolutePath());
            iv_qrcode.setImageBitmap(BitmapFactory.decodeFile(mFileQrImage.getAbsolutePath()));
        }
    }

    /**
     * 获取圆角位图的方法
     *
     * @param bitmap 数据源
     * @param pixels 圆角角度，360是圆形
     */
    private Bitmap getRoundCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);// ��ȡ�����ص�ͼƬ
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private void initView() {
        titleView = (TitleView) findViewById(R.id.title_share_qrcode);
        titleView.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
        titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        titleView.setTitle(getString(R.string.tips_share_qrcode));
        titleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        finish();
                        break;
                    case TitleView.NAVIGATION_BUTTON_RIGHT:
                        Intent intent = new Intent(ShareQRCodeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;

                }

            }
        });
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        tv_save_to_phone = (TextView) findViewById(R.id.tv_save_to_phone);
        tv_share = (TextView) findViewById(R.id.tv_share);
        tv_camera_num = (TextView) findViewById(R.id.tv_camera_num);
        tv_camera_num.setText(getString(R.string.tips_device_num) + ":  " + mCameraNum);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_share) {// 由文件得到uri
            Intent shareIntent = new Intent();
            Uri imageUri;
            if (HiDataValue.ANDROID_VERSION >= 24) {
                imageUri = FileProvider.getUriForFile(ShareQRCodeActivity.this, HiDataValue.FILEPROVIDER, mFileQrImage);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                imageUri = Uri.fromFile(mFileQrImage);
            }
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.tips_share_to)));
        } else if (id == R.id.tv_save_to_phone) {// 发通知图库更新文件显示
            if (mFileQrImage.exists() && mFileQrImage.isFile()) {
                Uri localUri = Uri.fromFile(mFileQrImage);
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                sendBroadcast(localIntent);
                HiToast.showToast(ShareQRCodeActivity.this, getString(R.string.success_to_album));
            }
        }

    }
}
