package com.hichip.customview;
import com.hichip.R;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.MyLiveViewGLMonitor;
import com.hichip.thecamhi.utils.SharePreUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
/**
 * 自定义View 引导view
 * @author lt
 */
public class CircularView extends View {

	private Paint mPaint;
	private Context mContext;
	private int screenWidth;
	private int screenHeight;
	private int topHeight;
	private int centerPoint;
	private GestureDetector mGestureDetector;
	private MyLiveViewGLMonitor mMonitor;

	public CircularView(Context context) {
		super(context, null);
		init();
	}

	public CircularView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext =context;
		init();
		mGestureDetector = new GestureDetector(mContext, new Double());
	}

	public void setMonitor(MyLiveViewGLMonitor monitor){
		this.mMonitor=monitor;
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(mContext.getResources().getColor(R.color.color_19B4ED));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(10);
		/**
		 * 获取屏幕的宽高
		 */
		WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		topHeight = HiTools.dip2px(mContext, 45);
		centerPoint = screenWidth / 2 / 3;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/**
		 * 绘制圆环drawCircle的前两个参数表示圆心的XY坐标， 这里我们用到了一个工具类获取屏幕尺寸以便将其圆心设置在屏幕中心位置， 第三个参数是圆的半径，第四个参数则为我们的画笔
		 */
		mPaint.setStrokeWidth(10);
		canvas.drawCircle(screenWidth / 2, screenWidth / 2 + topHeight, screenWidth / 2 - 5, mPaint);
		mPaint.setStrokeWidth(5);
		canvas.drawCircle(screenWidth / 2, screenWidth / 2 + topHeight, centerPoint, mPaint);
		// 画直线
		float startX = screenWidth / 2 + centerPoint;
		float startY = screenWidth / 2 + topHeight;
		float stopX = screenWidth;
		float stopY = screenWidth / 2 + topHeight;
		// 参数参考点--------------
		canvas.drawLine(startX, startY, stopX, stopY, mPaint);
		canvas.drawLine(0, startY, screenWidth / 2 - centerPoint, startY, mPaint);
		canvas.drawLine(screenWidth / 2, topHeight + screenWidth / 2 - centerPoint, screenWidth / 2, topHeight, mPaint);
		canvas.drawLine(screenWidth / 2, topHeight + screenWidth / 2 + centerPoint, screenWidth / 2, topHeight + screenWidth, mPaint);

		// 画斜线
		startX = (float) (centerPoint * 0.7) + screenWidth / 2;
		startY = (float) (topHeight + (screenWidth / 2 - centerPoint * 0.7));
		stopX = (float) (screenWidth / 2 + screenWidth / 2 * 0.7);
		stopY = (float) (topHeight + (screenWidth / 2 - (screenWidth / 2 * 0.7)));
		canvas.drawLine(startX, startY, stopX, stopY, mPaint);

		startX = (float) (screenWidth / 2 - centerPoint * 0.7);
		startY = (float) (topHeight + (screenWidth / 2 - centerPoint * 0.7));
		stopX = (float) (screenWidth / 2 - screenWidth / 2 * 0.7);
		stopY = (float) (topHeight + (screenWidth / 2 - screenWidth / 2 * 0.7));
		canvas.drawLine(startX, startY, stopX, stopY, mPaint);

		startX = (float) (screenWidth / 2 - centerPoint * 0.7);
		startY = (float) (topHeight + (screenWidth / 2 + centerPoint * 0.7));
		stopX = (float) (screenWidth / 2 - screenWidth / 2 * 0.7);
		stopY = (float) (topHeight + (screenWidth / 2 + screenWidth / 2 * 0.7));
		canvas.drawLine(startX, startY, stopX, stopY, mPaint);

		startX = (float) (screenWidth / 2 + centerPoint * 0.7);
		startY = (float) (topHeight + (screenWidth / 2 + centerPoint * 0.7));
		stopX = (float) (screenWidth / 2 + screenWidth / 2 * 0.7);
		stopY = (float) (topHeight + (screenWidth / 2 + screenWidth / 2 * 0.7));
		canvas.drawLine(startX, startY, stopX, stopY, mPaint);

		// 画数字
		int dis = HiTools.dip2px(mContext, 8);
		mPaint.setTextSize(80f);

		float x = screenWidth / 2;
		float y = screenWidth / 2 + topHeight;
		canvas.drawText("9", x - dis, y + dis, mPaint);
		x = (float) (centerPoint * 0.7 / 2 + screenWidth / 2) + dis * 2;
		y = topHeight + (screenWidth / 2 - centerPoint) / 2 + dis * 2;
		canvas.drawText("1", x, y, mPaint);

		x = (float) (screenWidth / 2 + (screenWidth / 2 * 0.7 + centerPoint * 0.7) / 2) + dis * 2;
		y = (float) (screenWidth / 2 - centerPoint * 0.7 + topHeight) + dis * 2;
		canvas.drawText("2", x, y, mPaint);

		x = (float) (screenWidth / 2 + (screenWidth / 2 * 0.7 + centerPoint * 0.7) / 2) + dis * 2;
		y = (float) (screenWidth / 2 + centerPoint * 0.7 + topHeight) + dis * 2;
		canvas.drawText("3", x, y, mPaint);

		x = (float) (centerPoint * 0.7 / 2 + screenWidth / 2) + dis * 2;
		y = (float) (topHeight + screenWidth / 2 + (centerPoint * 0.7 + screenWidth / 2 * 0.7) / 2) + dis * 4;
		canvas.drawText("4", x, y, mPaint);

		x = (float) (screenWidth / 2 - centerPoint * 0.7) - dis * 2;
		y = (float) (topHeight + screenWidth / 2 + (centerPoint * 0.7 + screenWidth / 2 * 0.7) / 2) + dis * 4;
		canvas.drawText("5", x, y, mPaint);

		x = (float) ((centerPoint * 0.7 + screenWidth / 2 * 0.7) / 2) - dis * 2;
		y = (float) (screenWidth / 2 + centerPoint * 0.7 + topHeight) + dis * 2;
		canvas.drawText("6", x, y, mPaint);

		x = (float) ((centerPoint * 0.7 + screenWidth / 2 * 0.7) / 2) - dis * 2;
		y = (float) (screenWidth / 2 - centerPoint * 0.7 + topHeight) + dis * 2;
		canvas.drawText("7", x, y, mPaint);

		x = (float) (screenWidth / 2 - centerPoint * 0.7) - dis * 2;
		y = topHeight + (screenWidth / 2 - centerPoint) / 2 + dis * 2;
		canvas.drawText("8", x, y, mPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 获取宽-测量规则的模式和大小
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension(widthSize, screenWidth+topHeight);

	}

	private int centerPointX;
	private int centerPointY;

	private class Double extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float x = e.getRawX();
			float y = e.getRawY();

			int pxMoniter = screenWidth;
			int pxTopview = HiTools.dip2px(getContext(), 45) + getStatusBarHeight();
			centerPoint = screenWidth / 2 / 3;// 测试中心区域的值,先随便写一个值
			centerPointX = screenWidth / 2;
			centerPointY = screenWidth / 2 + pxTopview;

			// 区域 1
			boolean area_one = x < screenWidth / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
			// 区域2
			boolean area_two = x > screenWidth / 2 && x < screenWidth && y < pxTopview + pxMoniter / 2 && y > pxTopview;
			// 区域3
			boolean area_there = x < screenWidth / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
			// 区域4
			boolean area_four = x > screenWidth / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;

			handlerFrameMode1(x, y, pxTopview, centerPoint, area_one, area_two, area_there, area_four);
			RelativeLayout rl=(RelativeLayout) getParent();
			rl.setVisibility(View.GONE);
			mMonitor.mIsZoom=true;
			SharePreUtils.putBoolean("cache", mContext, "isFirst", true);
			return super.onDoubleTap(e);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	private void handlerFrameMode1(float x, float y, float pxTopview, float centerPoint, boolean area_one, boolean area_two, boolean area_there, boolean area_four) {
		if (area_one) {
			float distanceX = screenWidth / 2 - x;
			float distanceY = screenWidth / 2 + pxTopview - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screenWidth / 2 && distance > centerPoint) {// 根据勾股定理算出来的距离如果小于半径就在区域1里面
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的6模块
					mMonitor.SetPosition(true, 6);
					mMonitor.mSetPosition = 6;
				} else if (absX < absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的6模块
					mMonitor.SetPosition(true, 7);
					mMonitor.mSetPosition = 7;
				}
			} else if (distance < centerPoint) {
				mMonitor.SetPosition(true, 8);
				mMonitor.mSetPosition = 8;
			}
		} else if (area_two) {
			float distanceX = x - screenWidth / 2;
			float distanceY = screenWidth / 2 + pxTopview - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screenWidth / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的1模块
					mMonitor.SetPosition(true, 1);
					mMonitor.mSetPosition = 1;
				} else if (absX < absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的0模块
					mMonitor.SetPosition(true, 0);
					mMonitor.mSetPosition = 0;
				}
			} else if (distance < centerPoint) {
				mMonitor.SetPosition(true, 8);
				mMonitor.mSetPosition = 8;
			}
		} else if (area_there) {
			float distanceX = screenWidth / 2 - x;
			float distanceY = y - (screenWidth / 2 + pxTopview);
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screenWidth / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的5模块
					mMonitor.SetPosition(true, 5);
					mMonitor.mSetPosition = 5;
				} else if (absX < absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的4模块
					mMonitor.SetPosition(true, 4);
					mMonitor.mSetPosition = 4;
				}
			} else if (distance < centerPoint) {
				mMonitor.SetPosition(true, 8);
				mMonitor.mSetPosition = 8;
			}
		} else if (area_four) {
			float distanceX = x - screenWidth / 2;
			float distanceY = y - screenWidth / 2 - pxTopview;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screenWidth / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的2模块
					mMonitor.SetPosition(true, 2);
					mMonitor.mSetPosition = 2;
				} else if (absX < absY && distance < screenWidth / 2 && distance > centerPoint) {// 我是真正的3模块
					mMonitor.SetPosition(true, 3);
					mMonitor.mSetPosition = 3;
				}
			} else if (distance < centerPoint) {
				mMonitor.SetPosition(true, 8);
				mMonitor.mSetPosition = 8;
			}
		}
	}

}