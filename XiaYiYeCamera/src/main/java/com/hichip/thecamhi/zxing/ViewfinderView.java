package com.hichip.thecamhi.zxing;


import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.hichip.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int frameColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    // 二维码 美化功能 添加处
    private boolean laserLinePortrait = true;
    Rect mRect;
    int i = 0;
    GradientDrawable mDrawable;
    Paint textPaint;
    private int textColor;
    private int rectCorner_color;
    private int laserColor_left;
    private int laserColor_center;
    private int laserColor_right;
    private Context context;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = new Paint();

        // 二维码 美化功能 添加处
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
        //		int laserColor_left = Color.parseColor("#CACACA");
        //		int laserColor_center = Color.RED;
        //		int laserColor_right = Color.parseColor("#CACACA");
        Resources resources = getResources();

        laserColor_left = resources.getColor(R.color.viewfinder_laser_left);// 设置 扫描线的  左边 的颜色
        laserColor_center = resources.getColor(R.color.viewfinder_laser_center);// 设置 扫描线的  中间 的颜色
        laserColor_right = resources.getColor(R.color.viewfinder_laser_right);// 设置 扫描线的 右边  的颜色

        mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{laserColor_left, laserColor_center, laserColor_right});


        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        laserColor = resources.getColor(R.color.viewfinder_laser);// 设置 扫描线的 的颜色
        textColor = resources.getColor(R.color.viewfinder_text);// 设置 扫描 框下面的的 的颜色
        resultPointColor = resources.getColor(R.color.possible_result_points);//  设置闪点的 颜色
        rectCorner_color = resources.getColor(R.color.viewfinder_4_corner);//设置 扫描框的 四个角的颜色

        scannerAlpha = 0;
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        // 获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        //画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        //										扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);

        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            // 二维码 美化功能  替换处
            //			//画扫描框边上的角，总共8个部分
            //			// Draw a two pixel solid black border inside the framing rect
            //			paint.setColor(frameColor);
            //			canvas.drawRect(frame.left, frame.top, frame.right + 1,frame.top + 2, paint);
            //
            //			canvas.drawRect(frame.left, frame.top + 2, frame.left + 2,frame.bottom - 1, paint);
            //
            //			canvas.drawRect(frame.right - 1, frame.top, frame.right + 1,frame.bottom - 1, paint);
            //
            //			canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1,frame.bottom + 1, paint);
            //
            //
            //			// Draw a red "laser scanner" line through the middle to show
            //			// decoding is active
            //			 //画扫描框里面的线条
            //			paint.setColor(laserColor);
            //			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            //			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            //			int middle = frame.height() / 2 + frame.top;
            //			canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
            //					middle + 2, paint);

            //	二维码 美化功能 替换处
            paint.setColor(rectCorner_color);
            //画出四个角：以屏幕左上角 为原点进行画边-画矩形：遵守  左-上-右-下原则
            canvas.drawRect(frame.left - 10, frame.top - 10, frame.left + 20, frame.top, paint);
            canvas.drawRect(frame.left - 10, frame.top - 10, frame.left, frame.top + 20, paint);
            canvas.drawRect(frame.right - 20, frame.top - 10, frame.right + 10, frame.top, paint);
            canvas.drawRect(frame.right, frame.top - 10, frame.right + 10, frame.top + 20, paint);

            //			这个 美化下面四个角有错：下边  不能再上边的 上边 ，画不出来；  一定要遵循上面的原则
            //			canvas.drawRect(frame.left - 10, frame.bottom + 10, frame.left + 20, frame.bottom, paint);
            //			canvas.drawRect(frame.left - 10, frame.bottom + 10, frame.left, frame.bottom - 20, paint);
            //			canvas.drawRect(frame.right - 20, frame.bottom + 10, frame.right + 10, frame.bottom, paint);
            //			canvas.drawRect(frame.right, frame.bottom + 10, frame.right + 10, frame.bottom - 20, paint);
            //			这个 美化下面四个角 更正
            canvas.drawRect(frame.left - 10, frame.bottom, frame.left + 20, frame.bottom + 10, paint);
            canvas.drawRect(frame.left - 10, frame.bottom - 20, frame.left, frame.bottom + 10, paint);
            canvas.drawRect(frame.right - 20, frame.bottom, frame.right + 10, frame.bottom + 10, paint);
            canvas.drawRect(frame.right, frame.bottom - 20, frame.right + 10, frame.bottom + 10, paint);

            int middle = frame.width() / 2;
            //			int pxValue=Utils.sp2px(context, getResources().getDimension(R.dimen.laser_bottom_text_sp));//设置字体的大小； 将sp 转换成px\

            int pxValue = (int) getResources().getDimension(R.dimen.laser_bottom_text_sp);//这个 返回来的 就是px 值
            //			Log.e("spValue", " 16 spValue="+pxValue+"  \n 16dpValue="+getResources().getDimension(R.dimen.activity_horizontal_margin));//由此可判断 从资源文件获取的东西 都是px

            textPaint.setTextSize(pxValue);
            textPaint.setColor(textColor);//字体颜色
            String text = getResources().getString(R.string.dimension_content);

            int textLen = (int) textPaint.measureText(text);//获得文本的长度
            canvas.drawText(text, frame.width() / 2 + frame.left - textLen / 2, frame.bottom + 60, textPaint);

            //			画一个红色的线
            // Draw a red "laser scanner" line through the middle to show
            // decoding is active
            paint.setColor(laserColor);// 这句话 似乎没有用
            paint.setStrokeWidth(2);//
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            //上下走的线   ：实现线 上下走
            if (laserLinePortrait) {

                if ((i += 5) < frame.bottom - frame.top) {
                    /*
                     * canvas.drawRect(frame.left + 2, frame.top - 2 + i, frame.right - 1, frame.top + 2 + i,
                     * paint);
                     */
                    int r = 8;
                    //			  shap 样式可以自己自定义 ，使用getResources().getDrawable(id)  进行调用
                    mDrawable.setShape(GradientDrawable.RECTANGLE);//设置 矩形线
                    mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//LINEAR_GRADIENT  设置线的颜色渐变
                    setCornerRadii(mDrawable, r, r, r, r);
                    mRect.set(frame.left + 2, frame.top - 3 + i, frame.right - 1, frame.top + 3 + i);
                    mDrawable.setBounds(mRect);
                    mDrawable.draw(canvas);
                    invalidate();
                } else {
                    i = 0;
                }

            } else {
                float left = frame.left + (frame.right - frame.left) / 2 - 2;
                canvas.drawRect(left, frame.top, left + 2, frame.bottom - 2, paint);
            }

            //			paint.setColor(laserColor);//一条闪烁的横线
            //			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            //			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            //			int middle = frame.height() / 2 + frame.top;
            //			canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
            //					middle + 2, paint);


            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);//  设置扫描  亮点的颜色
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                }
            }

            // Request another update at the animation interval, but only
            // repaint the laser line,
            // not the entire viewfinder mask.
            //只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    public void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
        drawable.setCornerRadii(new float[]{r0, r0, r1, r1, r2, r2, r3, r3});
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

}
