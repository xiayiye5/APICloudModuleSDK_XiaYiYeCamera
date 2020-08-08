package com.hichip.thecamhi.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;
import com.hichip.hichip.activity.FishEye.FishEyeActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedOnlineActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedPhotoActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedPlayLocalActivity;
import com.hichip.base.HiLog;
import com.hichip.base.HiThread;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiGLMonitor;
import com.hichip.data.HiDeviceInfo;
import com.hichip.sdk.HiChipP2P;
import com.hichip.thecamhi.bean.MyCamera;

public class MyLiveViewGLMonitor extends HiGLMonitor implements OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
	private GestureDetector mGestureDetector;
	private static final int FLING_MIN_DISTANCE = 50;
	public static int PTZ_STEP = 50; // 云台步长
	private MyCamera mCamera = null;
	Matrix matrix = new Matrix();
	private OnTouchListener mOnTouchListener;
	private Activity context;
	private int state = 0; // normal=0, larger=1,two finger touch=3
	private int touchMoved; // not move=0, move=1, two point=2
	public int left;
	public int width;
	public int height;
	public int bottom;
	public int screen_width;
	public int screen_height;
	// public int mOritation; // 1竖屏 0横屏
	public static int centerPoint;
	private float pxTopview;
	public boolean mVisible = true;

	public MyLiveViewGLMonitor(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, this);
		super.setOnTouchListener(this);
		setOnTouchListener(this);
		setFocusable(true);
		setClickable(true);
		setLongClickable(true);
		this.context = (Activity) context;

		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		screen_width = dm.widthPixels;
		screen_height = dm.heightPixels;
		pxTopview = HiTools.dip2px(getContext(), 45) + getStatusBarHeight();
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public int getTouchMove() {
		return this.touchMoved;
	}

	public void setTouchMove(int touchMoved) {
		this.touchMoved = touchMoved;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// View当前的位置
	private float rawX = 0;
	private float rawY = 0;
	// View之前的位置
	private float lastX = 0;
	private float lastY = 0;

	int xlenOld;
	int ylenOld;

	private int pyl = 20;
	double nLenStart = 0;

	@SuppressLint("WrongCall")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mOnTouchListener != null) {
			mOnTouchListener.onTouch(v, event);// 必须要回调非当前的OnTouch方法,不然会栈溢出崩溃
		}
		int nCnt = event.getPointerCount();

		if (state == 1) {// 放大就是1
			if (nCnt == 2) {
				return false;
			}
			// 处理放大后,移动界面(类似移动云台,只不过云台没有动)
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 获取手指落下的坐标并保存
					rawX = (event.getRawX());
					rawY = (event.getRawY());
					lastX = rawX;
					lastY = rawY;
					break;
				case MotionEvent.ACTION_MOVE:
					Log.i("tedu", "--移动移动了--:"+touchMoved);
					if (touchMoved == 2) {
						break;
					}
					HiLog.e("mMonitor.ACTION_MOVE");
					// 手指拖动时，获得当前位置
					rawX = event.getRawX();
					rawY = event.getRawY();
					// 手指移动的x轴和y轴偏移量分别为当前坐标-上次坐标
					float offsetX = rawX - lastX;
					float offsetY = rawY - lastY;
					// 通过View.layout来设置左上右下坐标位置
					// 获得当前的left等坐标并加上相应偏移量
					if (Math.abs(offsetX) < pyl && Math.abs(offsetY) < pyl) {

						return false;
					}
					left += offsetX;
					bottom -= offsetY;
					if (left > 0) {
						left = 0;
					}
					if (bottom > 0) {
						bottom = 0;
					}
					if ((left + width < (screen_width))) {
						left = (int) (screen_width - width);
					}
					if (bottom + height < screen_height) {
						bottom = (int) (screen_height - height);
					}
					if (left <= (-width)) {
						left = (-width);
					}
					if (bottom <= (-height)) {
						bottom = (-height);
					}
					Log.i("tedu", "--哈哈 我也走了--");
					setMatrix(left, bottom, width, height);
					// 移动过后，更新lastX与lastY
					lastX = rawX;
					lastY = rawY;
					break;

			}
			return mGestureDetector.onTouchEvent(event);
		} else if (state == 0 && nCnt == 1) {
			return mGestureDetector.onTouchEvent(event);
		}
		return true;
	}

	public void saveMatrix(int left, int bottom, int width, int height) {
		this.left = left;
		this.bottom = bottom;
		this.width = width;
		this.height = height;
	}

	float resetWidth;
	float resetHeight;
	private int centerPointX;
	private int centerPointY;

	public void setView() {
		WindowManager.LayoutParams wlp = context.getWindow().getAttributes();
		wlp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

		context.getWindow().setAttributes(wlp);
		context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		float screen_width = dm.widthPixels;
		float screen_height = dm.heightPixels;

		if (resetWidth == 0) {
			resetWidth = screen_width;
			resetHeight = screen_height;
		}
		resetWidth += 100;
		resetHeight += 100;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		down_x = e.getRawX();
		down_y = e.getRawY();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private float down_x = 0;
	private float down_y = 0;
	private float move_X = 0;
	private float move_Y = 0;
	private int mNO = 0;
	private int mScrollOri = 0;

	// distanceX 为e1-e2的偏移量 e1为起始点的坐标 e2为移动变化点的坐标
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		Log.i("tedu", "--onScrollonScrollonScrollonScroll--");
		if(mCamera!=null&&mCamera.isWallMounted){
			return true;
		}
		screen_width = context.getWindowManager().getDefaultDisplay().getWidth();
		screen_height = context.getWindowManager().getDefaultDisplay().getHeight();
		if (mCamera.isFishEye()) {
			if (FishEyeActivity.misFullScreen == 2) {// 22222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222
				if (mCamera.isFishEye()) {
					if (FishEyeActivity.mFrameMode == 4) {
						if (Math.abs(distanceX) < 5) {
							return true;
						}
						handlerFrameMode_4(distanceX, e1.getX(), e1.getY());
						return true;
					} else if (FishEyeActivity.mFrameMode == 3 && mCamera.mInstallMode == 0) {// 二画面
						handerFrameMode_3(e1, e2, distanceX);
					} else if (FishEyeActivity.mFrameMode == 1 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5 || mCamera.mInstallMode == 0 || mCamera.mInstallMode == 1) {
						if(FishEyeActivity.mFrameMode==2||FishEyeActivity.mFrameMode==5){
							int numX = (int) Math.ceil(Math.abs(distanceX) / 10);
							if(distanceX<0){//右滑
								setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
							}
							if(distanceX>0){
								setGesture(HiGLMonitor.GESTURE_LEFT, numX);
							}
							return true;
						}
						move_X = e2.getRawX();
						move_Y = e2.getRawY();
						float disX = move_X - down_x;
						float disY = move_Y - down_y;
						float K = disY / disX;
						float x = e2.getRawX();
						float y = e2.getRawY();

						centerPointX = screen_width / 2;
						centerPointY = (int) (screen_height / 2);
						float absX = Math.abs(x - centerPointX);
						float absY = Math.abs(y - centerPointY);
						boolean area_1 = false, area_2 = false, area_3 = false, area_4 = false, area_5 = false, area_6 = false, area_7 = false, area_8 = false;
						// 区域 1
						boolean area_one = x < screen_width / 2 && y < screen_height / 2;
						if (area_one) {
							if (absY > absX) {
								area_8 = true;
							} else if (absX > absY) {
								area_7 = true;
							}
						}
						// 区域2
						boolean area_two = x > screen_width / 2 && y < screen_height / 2;
						if (area_two) {
							if (absY > absX) {
								area_1 = true;
							} else if (absX > absY) {
								area_2 = true;
							}
						}
						// 区域3
						boolean area_there = x < screen_width / 2 && y > screen_height / 2;
						if (area_there) {
							if (absY > absX) {
								area_5 = true;
							} else if (absX > absY) {
								area_6 = true;
							}
						}
						// 区域4
						boolean area_four = x > screen_width / 2 && y > screen_height / 2;
						if (area_four) {
							if (absY > absX) {
								area_4 = true;
							} else if (absX > absY) {
								area_3 = true;
							}
						}
						int numX = (int) Math.ceil(Math.abs(distanceX) / 10) + 1;
						return handMyScro(e2, distanceX, distanceY, K, area_1, area_2, area_3, area_4, area_5, area_6, area_7, area_8, numX, 0, screen_height);
					}
				}
			} else if (FishEyeActivity.misFullScreen == 1) {// 111111111111111111111111111111111111111111111111111111111111111111111111
				if (FishEyeActivity.mFrameMode == 4 && mCamera.mInstallMode == 0) {
					if (Math.abs(distanceX) < 5) {
						return true;
					}
					handlerFrameMode_4(distanceX, e1.getRawX(), e1.getRawY());
				} else if (FishEyeActivity.mFrameMode == 3 && mCamera.mInstallMode == 0) {// 二画面
					handerFrameMode_3(e1, e2, distanceX);
				} else if (FishEyeActivity.mFrameMode == 1 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5 || mCamera.mInstallMode == 0 || mCamera.mInstallMode == 1) {
					if(FishEyeActivity.mFrameMode==2||FishEyeActivity.mFrameMode==5){
						int numX = (int) Math.ceil(Math.abs(distanceX) / 10);
						if(distanceX<0){//右滑
							setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
						}
						if(distanceX>0){
							setGesture(HiGLMonitor.GESTURE_LEFT, numX);
						}
						return true;
					}

					move_X = e2.getRawX();
					move_Y = e2.getRawY();
					float disX = move_X - down_x;
					float disY = move_Y - down_y;
					float K = disY / disX;
					float x = e2.getRawX();
					float y = e2.getRawY();
					float pxMoniter = screen_width;
					centerPointX = screen_width / 2;
					centerPointY = (int) (screen_width / 2 + pxTopview);
					float absX = Math.abs(x - centerPointX);
					float absY = Math.abs(y - centerPointY);
					boolean area_1 = false, area_2 = false, area_3 = false, area_4 = false, area_5 = false, area_6 = false, area_7 = false, area_8 = false;
					// 区域 1
					boolean area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
					if (area_one) {
						if (absY > absX) {
							area_8 = true;
						} else if (absX > absY) {
							area_7 = true;
						}
					}
					// 区域2
					boolean area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
					if (area_two) {
						if (absY > absX) {
							area_1 = true;
						} else if (absX > absY) {
							area_2 = true;
						}
					}
					// 区域3
					boolean area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
					if (area_there) {
						if (absY > absX) {
							area_5 = true;
						} else if (absX > absY) {
							area_6 = true;
						}
					}
					// 区域4
					boolean area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
					if (area_four) {
						if (absY > absX) {
							area_4 = true;
						} else if (absX > absY) {
							area_3 = true;
						}

					}

					int numX = (int) Math.ceil(Math.abs(distanceX) / 10) + 1;
					return handMyScro(e2, distanceX, distanceY, K, area_1, area_2, area_3, area_4, area_5, area_6, area_7, area_8, numX, pxTopview, screen_width);
				}
			}
		}
		down_x = move_X;
		down_y = move_Y;
		return true;
	}

	/*
	 * 竖屏 传 pxTopview 横屏传 0 竖屏 传 screen_width 横屏 传screen_height
	 */
	private boolean handMyScro(MotionEvent e1, float distanceX, float distanceY, float K, boolean area_1, boolean area_2, boolean area_3, boolean area_4, boolean area_5, boolean area_6, boolean area_7, boolean area_8, int numX, float pxTopview, int screen_width) {
		if (distanceX < 0) {// 右滑
			if (K < 3 && K > 0.3) {// 1.右下滑
				if (mScrollOri != 1) {
					mNO = 0;
					mScrollOri = 1;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				Log.i("tedu", "---回调了: onScroll---右下滑--->" + numX + "-K->" + K);
				if (this.GetFishLager() == 0.0) {
					if (area_8 || area_1 || area_2 || area_3) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					this.SetGesture(7);
					this.SetGesture(7);
					this.SetGesture(7);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_RIGHT);
						this.SetGesture(HiGLMonitor.GESTURE_DOWN);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > -3 && K < -0.33) {// 2.右上滑
				if (mScrollOri != 2) {
					mNO = 0;
					mScrollOri = 2;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				Log.i("tedu", "------回调了: onScroll---右上滑---->" + numX + "-K->" + K);
				if (this.GetFishLager() == 0.0) {
					if (area_8 || area_1 || area_6 || area_7) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					this.SetGesture(6);
					this.SetGesture(6);
					this.SetGesture(6);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_RIGHT);
						this.SetGesture(HiGLMonitor.GESTURE_UP);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > 3 || K < -3) {
			} else {
				if (mScrollOri != 3) {
					mNO = 0;
					mScrollOri = 3;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				Log.i("tedu", "------回调了: onScroll---右滑---->" + numX+"-this.GetFishLager()-:"+this.GetFishLager());
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() ==0.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX / 2);
					}else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX/2);
					}
				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX / 2);
					}else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX/2);
					}
				} else {
					setGesture(HiGLMonitor.GESTURE_RIGHT, numX / 2);
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			}
		}
		if (distanceX > 0) {// 左滑
			if (K < 3 && K > 0.33) {// 3.左上滑
				if (mScrollOri != 4) {
					mNO = 0;
					mScrollOri = 4;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				Log.i("tedu", "------回调了: onScroll---左上滑---->" + numX + "-K->" + K);
				if (this.GetFishLager() == 0.0) {
					if (area_8 || area_1 || area_2 || area_3) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					this.SetGesture(4);
					this.SetGesture(4);
					this.SetGesture(4);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_LEFT);
						this.SetGesture(HiGLMonitor.GESTURE_UP);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > -3 && K < -0.33) {// 4.左下滑
				if (mScrollOri != 5) {
					mNO = 0;
					mScrollOri = 5;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				Log.i("tedu", "------回调了: onScroll---左下滑---->" + numX + "-K->" + K);
				if (this.GetFishLager() == 0.0) {
					if (area_1 || area_8 || area_7 || area_6) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					this.SetGesture(5);
					this.SetGesture(5);
					this.SetGesture(5);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_LEFT);
						this.SetGesture(HiGLMonitor.GESTURE_DOWN);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > 3 || K < -3) {
			} else {
				if (mScrollOri != 6) {
					mNO = 0;
					mScrollOri = 6;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				Log.i("tedu", "------回调了: onScroll---左滑---->" + numX + "-K->" + K);
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() == 0.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX / 2);
					}else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX/2);
					}
				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() == 0.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX / 2);
					}else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX/2);
					}
				} else {
					setGesture(HiGLMonitor.GESTURE_LEFT, numX / 2);
				}

				down_x = move_X;
				down_y = move_Y;
				return true;
			}
		}
		int num = (int) Math.ceil(Math.abs(distanceY) / 40) + 1;
		if (distanceY < 0 && Math.abs(K) > 3.0) {// 5.下滑
			if (mScrollOri != 7) {
				mNO = 0;
				mScrollOri = 7;
			}
			mNO++;
			if (mNO < 2) {
				return false;
			}
			Log.i("tedu", "-----回调了: onScroll---下滑---->" + num + "--K-->" + K);
			if (this.GetFishLager() != 0.0) {
				if (this.GetFishLager() < 8.0 && this.GetFishLager() >= 0) {
					setGesture(HiGLMonitor.GESTURE_DOWN, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_DOWN, num - 1);
				}
			}
			if (this.GetFishLager() == 0.0) {
				if (area_1 || area_2 || area_3 || area_4) {
					setGesture(HiGLMonitor.GESTURE_RIGHT, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_LEFT, num);
				}
			}
			down_x = move_X;
			down_y = move_Y;
			return true;
		}
		if (distanceY > 0 && Math.abs(K) > 3.0) {// 6.上滑
			if (mScrollOri != 8) {
				mNO = 0;
				mScrollOri = 8;
			}
			mNO++;
			if (mNO < 2) {
				return false;
			}
			Log.i("tedu", "---回调了: onScroll---上滑---->" + num + "--K-->" + K);
			if (this.GetFishLager() != 0.0) {
				if (this.GetFishLager() < 8.0 && this.GetFishLager() >= 0.0) {
					setGesture(HiGLMonitor.GESTURE_UP, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_UP, num - 1);
				}
			}
			if (this.GetFishLager() == 0.0) {
				if (area_1 || area_2 || area_3 || area_4) {
					setGesture(HiGLMonitor.GESTURE_LEFT, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_RIGHT, num);
				}
			}
			down_x = move_X;
			down_y = move_Y;
			return true;
		}
		return false;
	}

	private void setGesture(int gesture, int num) {
		for (int i = 0; i < num; i++) {
			this.SetGesture(gesture);
			this.SetGesture(gesture);
		}
	}

	private void handerFrameMode_3(MotionEvent e1, MotionEvent e2, float distanceX) {
		boolean area_top = false;
		boolean area_bottom = false;
		float y = e1.getRawY();
		if (FishEyeActivity.misFullScreen == 1) { // 竖
			area_top = y > pxTopview && y < pxTopview + screen_width / 2;
			area_bottom = y > pxTopview + screen_width / 2 && y < pxTopview + screen_width;
		} else {// 横屏
			area_top = y < screen_height / 2;
			area_bottom = y > screen_height / 2 && y < screen_height;
		}

		move_X = e2.getRawX();
		move_Y = e2.getRawY();
		float disX = move_X - down_x;
		float disY = move_Y - down_y;
		float K = disY / disX;
		if (area_top) {
			if (distanceX < 0 && Math.abs(K) < 1) {// 右滑
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
			} else if (distanceX > 0 && Math.abs(K) < 1) {// 左滑
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
			}
		} else if (area_bottom) {
			if (distanceX < 0 && Math.abs(K) < 1) {// 右滑
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
			} else if (distanceX > 0 && Math.abs(K) < 1) {// 左滑
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
			}
		}
	}

	private void handlerFrameMode_4(float distanceX, float x, float y) {
		float pxMoniter = screen_width;
		boolean area_one = false;
		boolean area_two = false;
		boolean area_there = false;
		boolean area_four = false;
		if (FishEyeActivity.misFullScreen == 1) { // 竖屏
			// 区域 1
			area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
			// 区域2
			area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
			// 区域3
			area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
			// 区域4
			area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
		} else { // 横屏
			// 区域 1
			area_one = x < screen_width / 2 && y < screen_height / 2;
			// 区域2
			area_two = x > screen_width / 2 && y < screen_height / 2;
			// 区域3
			area_there = x < screen_width / 2 && y > screen_height / 2;
			// 区域4
			area_four = x > screen_width / 2 && y > screen_height / 2;
		}

		int num = 1;
		if (area_one) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 2);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 2);
				}
			}
		} else if (area_two) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
				}
			}
		} else if (area_there) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 3);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 3);
				}
			}
		} else if (area_four) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
				}
			}
		}
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	// velocityX——每秒x轴方向移动的像素 速率; velocityY——每秒y轴方向移动的像素 速率;
	// e1 起点移动时间 按下时的事件
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Log.i("tedu", "--velocityX:" + velocityX + "--velocityY:" + velocityY+"-FishEyeActivity.mFrameMode-:"+FishEyeActivity.mFrameMode);
		if (mCamera == null)     return false;
		if(mCamera!=null&&mCamera.isWallMounted){
			return true;
		}
		PTZ_STEP = mCamera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE &&mCamera.getDeviceType()!=3? 25 : 50;
		if (state == 0) {
			if (!mCamera.isFishEye()) {
				if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_LEFT, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
				} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_RIGHT, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
				} else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > Math.abs(velocityX)) {
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_UP, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
				} else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > Math.abs(velocityX)) {
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_DOWN, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
				}
			} else {
				if (getTouchMove() == 2) {
					return false;
				}
				if (FishEyeActivity.misFullScreen == 2) {// 222222222222222222222222222222222    横屏
					if (mCamera.isFishEye()) {
						if (FishEyeActivity.mFrameMode == 4 && Math.abs(velocityX) >= 1000) {
							float distanceX = e1.getRawX() - e2.getRawX();
							handlerFrameMode_4_Rotate(distanceX, e1.getX(), e1.getY());
							return true;
						}
						if (FishEyeActivity.mFrameMode == 3) {
							boolean area_top = e1.getRawY() < screen_height / 2;
							boolean area_bottom = e1.getRawY() > screen_height / 2 && e1.getRawY() < screen_height;
							float disX = e2.getRawX() - e1.getRawX();
							if (area_top) {
								if (disX > 0 && Math.abs(velocityX) >= 1600) {// 右滑
									startRotateMode4(HiGLMonitor.GESTURE_RIGHT, 100, 0);
								} else if (disX < 0 && Math.abs(velocityX) >= 1600) {// 左滑
									startRotateMode4(HiGLMonitor.GESTURE_LEFT, 100, 0);
								}
							} else if (area_bottom) {
								if (disX > 0 && Math.abs(velocityX) >= 1600) {// 右滑
									startRotateMode4(HiGLMonitor.GESTURE_RIGHT, 100, 1);
								} else if (disX < 0 && Math.abs(velocityX) >= 1600) {// 左滑
									startRotateMode4(HiGLMonitor.GESTURE_LEFT, 100, 1);
								}
							}
						}

						if(FishEyeActivity.mFrameMode==2||FishEyeActivity.mFrameMode == 5){//圆柱和碗
							int numX = (int) (Math.abs(e2.getRawX() - e1.getRawX()) / 3) / 2;
							if(velocityX>5000){
								startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
							}
							if(velocityX<-5000){
								startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
							}
							return true;
						}

						if (FishEyeActivity.mFrameMode == 1) {
							int numX = (int) (Math.abs(e2.getRawX() - e1.getRawX()) / 3) / 3;
							float x = e2.getRawX();
							float y = e2.getRawY();
							centerPointX = screen_width / 2;
							centerPointY = (int) (screen_height / 2);
							float absX = Math.abs(x - centerPointX);
							float absY = Math.abs(y - centerPointY);
							boolean area_1 = false, area_2 = false, area_3 = false, area_4 = false, area_5 = false, area_6 = false, area_7 = false, area_8 = false;
							// 区域 1
							boolean area_one = x < screen_width / 2 && y < screen_height / 2;
							if (area_one) {
								if (absY >= absX) {
									area_8 = true;
								} else if (absX > absY) {
									area_7 = true;
								}
							}
							// 区域2
							boolean area_two = x > screen_width / 2 && y < screen_height / 2;
							if (area_two) {
								if (absY >= absX) {
									area_1 = true;
								} else if (absX > absY) {
									area_2 = true;
								}
							}
							// 区域3
							boolean area_there = x < screen_width / 2 && y > screen_height / 2;
							if (area_there) {
								if (absY >= absX) {
									area_5 = true;
								} else if (absX > absY) {
									area_6 = true;
								}
							}
							// 区域4
							boolean area_four = x > screen_width / 2 && y > screen_height / 2;
							if (area_four) {
								if (absY >=absX) {
									area_4 = true;
								} else if (absX > absY) {
									area_3 = true;
								}
							}
							return handMyRota(e2, velocityX, velocityY,numX, area_1, area_2, area_3, area_4,area_5,area_6,area_7,area_8, 0, screen_height);
						}
					}
				} else {// 111111111111111111111111111111111111111111111111111111111111111111  竖屏
					Log.i("tedu", "--当前是竖屏--"+"--FishEyeActivity.mFrameMode--:"+FishEyeActivity.mFrameMode);
					if (FishEyeActivity.mFrameMode == 4 && Math.abs(velocityX) >= 1000 && mCamera.mInstallMode == 0) {
						float distanceX = e1.getRawX() - e2.getRawX();
						handlerFrameMode_4_Rotate(distanceX, e1.getRawX(), e1.getRawY());
						return true;
					}
					if (FishEyeActivity.mFrameMode == 3 && mCamera.mInstallMode == 0) {
						Log.i("tedu", "--33333333333333--");
						boolean area_top = false;
						boolean area_bottom = false;
						float y = e1.getRawY();
						if (FishEyeActivity.misFullScreen == 1) { // 竖
							area_top = y > pxTopview && y < pxTopview + screen_width / 2;
							area_bottom = y > pxTopview + screen_width / 2 && y < pxTopview + screen_width;
						} else {// 横屏
							area_top = y < screen_width / 2;
							area_bottom = y > screen_width / 2 && y < screen_width;
						}
						float disX = e2.getRawX() - e1.getRawX();
						if (area_top) {
							if (disX > 0 && Math.abs(velocityX) >= 1600) {// 右滑
								startRotateMode4(HiGLMonitor.GESTURE_RIGHT, 100, 0);
							} else if (disX < 0 && Math.abs(velocityX) >= 1600) {// 左滑
								startRotateMode4(HiGLMonitor.GESTURE_LEFT, 100, 0);
							}
						} else if (area_bottom) {
							if (disX > 0 && Math.abs(velocityX) >= 1600) {// 右滑
								startRotateMode4(HiGLMonitor.GESTURE_RIGHT, 100, 1);
							} else if (disX < 0 && Math.abs(velocityX) >= 1600) {// 左滑
								startRotateMode4(HiGLMonitor.GESTURE_LEFT, 100, 1);
							}
						}
						return true;
					}

					if(FishEyeActivity.mFrameMode==2||FishEyeActivity.mFrameMode == 5){//圆柱和碗
						int numX = (int) (Math.abs(e2.getRawX() - e1.getRawX()) / 3) / 2;
						if(velocityX>5000){
							startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
						}
						if(velocityX<-5000){
							startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
						}
						return true;
					}

					if (FishEyeActivity.mFrameMode == 1|| mCamera.mInstallMode == 0 || mCamera.mInstallMode == 1) {
						int numX = (int) (Math.abs(e2.getRawX() - e1.getRawX()) / 3) / 2;
						float x = e2.getRawX();
						float y = e2.getRawY();
						float pxMoniter = screen_width;
						centerPointX = screen_width / 2;
						centerPointY = (int) (screen_width / 2 + pxTopview);
						float absX = Math.abs(x - centerPointX);
						float absY = Math.abs(y - centerPointY);
						boolean area_1 = false, area_2 = false, area_3 = false, area_4 = false, area_5 = false, area_6 = false, area_7 = false, area_8 = false;
						// 区域 1
						// boolean area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
						boolean area_one = x < screen_width / 2 && y < pxTopview + pxMoniter / 2;
						if (area_one) {
							if (absY >= absX) {
								area_8 = true;
							} else if (absX > absY) {
								area_7 = true;
							}
						}
						// 区域2
						// boolean area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
						boolean area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2;
						if (area_two) {
							if (absY >= absX) {
								area_1 = true;
							} else if (absX > absY) {
								area_2 = true;
							}
						}
						// 区域3
						// boolean area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
						boolean area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2;
						if (area_there) {
							if (absY >= absX) {
								area_5 = true;
							} else if (absX > absY) {
								area_6 = true;
							}
						}
						// 区域4
						// boolean area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
						boolean area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2;
						if (area_four) {
							if (absY >= absX) {
								area_4 = true;
							} else if (absX > absY) {
								area_3 = true;
							}
						}
						return handMyRota(e2, velocityX, velocityY, numX, area_1, area_2, area_3, area_4, area_5, area_6, area_7, area_8, pxTopview, screen_width);
					}
				}
			}
		}
		return true;
	}

	private boolean handMyRota(MotionEvent e1, float velocityX, float velocityY, int numX, boolean area_1, boolean area_2, boolean area_3, boolean area_4, boolean area_5, boolean area_6, boolean area_7, boolean area_8, float pxTopview, int screen_width) {
		if (velocityX > 5000) {// 右滑
			if (velocityY > 3000) {// 1.右下滑
				Log.i("tedu", "------回调了: onFling----右下滑--->"+"-this.GetFishLager():"+this.GetFishLager()+"-FishEyeActivity.mFrameMode:"+FishEyeActivity.mFrameMode);
				if (this.GetFishLager() == 0.0 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5) {
					if (area_8 || area_1 || area_2 || area_3) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					startRotate(1, 7, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_RIGHT, numX / 2);
						startRotate(2, GESTURE_DOWN, numX / 2);
					}
				}
				return true;
			} else if (velocityY < -3000) {// 2.右上滑
				Log.i("tedu", "------回调了: onFling----右上滑--->" + numX+"--this.GetFishLager():"+this.GetFishLager());
				if (this.GetFishLager() == 0.0 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5) {
					if (area_8 || area_1 || area_6 || area_7) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					startRotate(1, 6, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_RIGHT, numX / 2);
						startRotate(2, GESTURE_UP, numX / 2);
					}
				}
				return true;
			} else{
				Log.i("tedu", "------回调了: onFling----右滑--->" + velocityX);
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX/2);
					}
				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX/2);
					}
				} else {
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX / 2);
				}
				return true;
			}
		}
		if (velocityX < -5000) {// 左滑
			if (velocityY < -3000) {// 3.左上滑
				Log.i("tedu", "------回调了: onFling----左上滑--->");
				if (this.GetFishLager() == 0.0 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5) {
					if (area_8 || area_1 || area_2 || area_3) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					startRotate(1, 4, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_LEFT, numX / 2);
						startRotate(2, GESTURE_UP, numX / 2);
					}
				}
				return true;
			} else if (velocityY > 3000) {// 4.左下滑
				Log.i("tedu", "------回调了: onFling----左下滑--->");
				if (this.GetFishLager() == 0.0 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5) {
					if (area_1 || area_8 || area_7 || area_6) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					startRotate(1, 5, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_LEFT, numX / 2);
						startRotate(2, GESTURE_DOWN, numX / 2);
					}
				}
				return true;
			} else {
				Log.i("tedu", "------回调了: onFling----左滑--->" + velocityX);
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX/2);
					}
				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX/2);
					}
				} else {
					startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
				}
				return true;
			}
		}
		if (velocityY > 3000) { // 向下滑
			Log.i("tedu", "------回调了: onFling----下滑--->" + velocityY);
			if (this.GetFishLager() == 0.0 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5) {
				if (area_1 || area_2 || area_3 || area_4) {
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, 60);
				} else {
					startRotate(1, HiGLMonitor.GESTURE_LEFT, 60);
				}
			} else {
				startRotate(1, HiGLMonitor.GESTURE_DOWN, 40);
			}
			return true;
		} else if (velocityY <- 3000) {// 向上滑
			Log.i("tedu", "------回调了: onFling----上滑--->" + velocityY);
			if (this.GetFishLager() == 0.0 || FishEyeActivity.mFrameMode == 2 || FishEyeActivity.mFrameMode == 5) {
				if (area_1 || area_2 || area_3 || area_4) {
					startRotate(1, HiGLMonitor.GESTURE_LEFT, 60);
				} else {
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, 60);
				}
			} else {
				startRotate(1, HiGLMonitor.GESTURE_UP, 40);
			}
			return true;
		}
		return false;
	}

	private void startRotateMode4(final int gesture, final int num, final int no) {
		if (mthreadGesture != null) {
			mthreadGesture.stopThread();
			mthreadGesture = null;
		}
		mthreadGesture = new ThreadGesture();
		mthreadGesture.SetValue(num, gesture, no);
		mthreadGesture.startThread();
	}

	private void handlerFrameMode_4_Rotate(float distanceX, float x, float y) {
		float pxMoniter = screen_width;
		boolean area_one = false;
		boolean area_two = false;
		boolean area_there = false;
		boolean area_four = false;
		if (FishEyeActivity.misFullScreen == 1) { // 竖屏
			// 区域 1
			area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
			// 区域2
			area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
			// 区域3
			area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
			// 区域4
			area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
		} else { // 横屏
			// 区域 1
			area_one = x < screen_width / 2 && y < screen_height / 2;
			// 区域2
			area_two = x > screen_width / 2 && y < screen_height / 2;
			// 区域3
			area_there = x < screen_width / 2 && y > screen_height / 2;
			// 区域4
			area_four = x > screen_width / 2 && y > screen_height / 2;
		}
		int num = (int) Math.abs(distanceX) / 7;
		if (area_one) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 2);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 2);
			}
		} else if (area_two) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 0);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 0);
			}
		} else if (area_there) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 3);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 3);
			}
		} else if (area_four) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 1);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 1);
			}
		}

	}

	public void setCamera(MyCamera mCamera) {
		this.mCamera = mCamera;
	}

	public void setOnTouchListener(OnTouchListener mOnTouchListener) {
		this.mOnTouchListener = mOnTouchListener;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (context instanceof WallMountedActivity) {
			WallMountedActivity act = (WallMountedActivity) context;
			if (mVisible) {
				act.ll_top.setVisibility(View.GONE);
				act.ll_bottom.setVisibility(View.GONE);
//				if (act.lightModel!=0) {
//					act.iv_white_light.setVisibility(View.GONE);
//				}

			} else {
				act.ll_top.setVisibility(View.VISIBLE);
				act.ll_bottom.setVisibility(View.VISIBLE);
//				if (act.lightModel!=0) {
//					act.iv_white_light.setVisibility(View.VISIBLE);
//				}

			}

		}

		if (context instanceof WallMountedPhotoActivity) {
			WallMountedPhotoActivity act = (WallMountedPhotoActivity) context;
			if (mVisible) {
				act.ll_top.animate().translationX(1.0f).translationY(-act.ll_top.getHeight()).start();
			} else {
				act.ll_top.animate().translationX(1.0f).translationY(1.0f).start();
			}

		}

		if (context instanceof WallMountedPlayLocalActivity) {
			WallMountedPlayLocalActivity act = (WallMountedPlayLocalActivity) context;
			if (mVisible) {
				act.ll_top.animate().translationX(1.0f).translationY(-act.ll_top.getHeight()).start();
				act.mLlPlay.animate().translationX(1.0f).translationY(act.mLlPlay.getHeight()).start();
			} else {
				act.ll_top.animate().translationX(1.0f).translationY(1.0f).start();
				act.mLlPlay.animate().translationX(1.0f).translationY(1.0f).start();
			}

		}

		if (context instanceof WallMountedOnlineActivity) {
			WallMountedOnlineActivity act = (WallMountedOnlineActivity) context;
			if (mVisible) {
				act.ll_top.animate().translationX(1.0f).translationY(-act.ll_top.getHeight()).start();
				act.mllPlay.animate().translationX(1.0f).translationY(act.mllPlay.getHeight()).start();
			} else {
				act.ll_top.animate().translationX(1.0f).translationY(1.0f).start();
				act.mllPlay.animate().translationX(1.0f).translationY(1.0f).start();
			}

		}

		if (context instanceof FishEyeActivity) {
			FishEyeActivity act = (FishEyeActivity) context;
			if (FishEyeActivity.misFullScreen == 1) {
				if (act.lightModel != 0) {
					if (act.iv_white_light.getVisibility() == View.VISIBLE) {
						act.iv_white_light.setVisibility(View.GONE);
					} else {
						act.iv_white_light.setVisibility(View.VISIBLE);
					}
				}
				if (act.mIvFullScreen.getVisibility() == View.VISIBLE) {
					act.mIvFullScreen.setVisibility(View.GONE);
				} else {
					act.mIvFullScreen.setVisibility(View.VISIBLE);
				}
			}
			if (FishEyeActivity.misFullScreen == 2) {
				if (act.lightModel != 0) {
					if (act.iv_land_white_light.getVisibility() == View.VISIBLE) {
						act.iv_land_white_light.setVisibility(View.GONE);
					} else {
						act.iv_land_white_light.setVisibility(View.VISIBLE);
					}
				}
				act.ll_land_top.setVisibility(act.ll_land_top.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
			}
		}

		mVisible = !mVisible;
		return false;
	}

	public int mSetPosition = 1;

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		screen_height = context.getWindowManager().getDefaultDisplay().getHeight();
		screen_width = context.getWindowManager().getDefaultDisplay().getWidth();
		if (mCamera.isWallMounted) {
//			return handWallMounted(e);

			if (context instanceof WallMountedActivity) {
				WallMountedActivity act = (WallMountedActivity) context;
				//正在巡航双击时取消巡航
				if (act.mIsCruise) {
					act.setCruise();
				}
			}
			return true;
		}
		if (mCamera.isFishEye()) {
			if (context instanceof FishEyeActivity && mCamera.mInstallMode == 1) {
				FishEyeActivity act = (FishEyeActivity) context;
				if (act.mWallMode == 1) {
					this.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
					act.mWallMode = 0;
					act.setSelectedMode(0);
					//	act.rbtn_circle.setSelected(true);
					act.rbtn_land_circle.setChecked(true);
					if (act.mIsCruise) {
						act.mMonitor.SetCruise(act.mIsCruise = !act.mIsCruise);
						act.iv_live_cruise.setSelected(act.mIsCruise);
					}
					return true;
				}
			}
			if (FishEyeActivity.misFullScreen == 2) {
				float x = e.getRawX();
				float y = e.getRawY();
				int pxMoniter = screen_width;
				centerPoint = screen_width / 2 / 3;// 测试中心区域的值,先随便写一个值
				centerPointX = screen_width / 2;
				centerPointY = screen_height / 2;

				// 区域 1
				boolean area_one = x < screen_width / 2 && y < screen_height / 2;
				// 区域2
				boolean area_two = x > screen_width / 2 && y < screen_height / 2;
				// 区域3
				boolean area_there = x < screen_width / 2 && y > screen_height / 2;
				// 区域4
				boolean area_four = x > screen_width / 2 && y > screen_height / 2;

				if (FishEyeActivity.mFrameMode == 1 || mCamera.mInstallMode == 1) {
					if (mIsZoom == true && GetFishLager() == 0.0) {// 解决圆双击放大,然后两个手指缩小至最小,然后再双击放大的bug
						mIsZoom = false;
					}
					if (GetFishLager() > 0.0 && !mIsZoom) {// 解决两个手指放大后,双击必须要缩小为原图的bug
						this.SetPosition(false, 8);
						return true;
					}
					if (mIsZoom) {
						this.SetPosition(false, mSetPosition);
						mIsZoom = !mIsZoom;
						return true;
					}
					handlerFrameMode2(x, y, pxTopview, centerPoint, area_one, area_two, area_there, area_four);
				} else if (FishEyeActivity.mFrameMode == 4) {
					// handlerFrameMode4(area_one, area_two, area_there, area_four);
				}

			} else {
				float x = e.getRawX();
				float y = e.getRawY();
				int pxMoniter = screen_width;
				centerPoint = screen_width / 2 / 3;// 测试中心区域的值,先随便写一个值
				centerPointX = screen_width / 2;
				centerPointY = (int) (screen_width / 2 + pxTopview);
				// 区域 1
				boolean area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
				// 区域2
				boolean area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
				// 区域3
				boolean area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
				// 区域4
				boolean area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
				if (FishEyeActivity.mFrameMode == 1 || mCamera.mInstallMode == 1) {
					if (mIsZoom == true && GetFishLager() == 0.0) {// 解决圆双击放大,然后两个手指缩小至最小,然后再双击放大的bug
						mIsZoom = false;
					}
					if (GetFishLager() > 0.0 && !mIsZoom) {// 解决两个手指放大后,双击必须要缩小为原图的bug
						this.SetPosition(false, 8);
						return true;
					}
					if (mIsZoom) {
						this.SetPosition(false, mSetPosition);
						mIsZoom = !mIsZoom;
						return true;
					}
					handlerFrameMode1(x, y, pxTopview, centerPoint, area_one, area_two, area_there, area_four);
				} else if (FishEyeActivity.mFrameMode == 4) {
					// handlerFrameMode4(area_one, area_two, area_there, area_four);
				}
			}
			return true;
		}
		return true;
	}

	private void handlerFrameMode2(float x, float y, float pxTopview2, int centerPoint2, boolean area_one, boolean area_two, boolean area_there, boolean area_four) {
		if (area_one) {
			float distanceX = screen_width / 2 - x;
			float distanceY = screen_height / 2 - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {// 根据勾股定理算出来的距离如果小于半径就在区域1里面
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的6模块
					setPostion(6);
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的6模块
					setPostion(7);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		} else if (area_two) {
			float distanceX = x - screen_width / 2;
			float distanceY = screen_height / 2 - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的1模块
					setPostion(1);
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的0模块
					setPostion(0);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		} else if (area_there) {
			float distanceX = (screen_width / 2) - x;
			float distanceY = y - (screen_height / 2);
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的5模块
					setPostion(5);
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的4模块
					setPostion(4);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		} else if (area_four) {
			float distanceX = x - screen_width / 2;
			float distanceY = y - screen_height / 2;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的2模块
					setPostion(2);
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {// 我是真正的3模块
					setPostion(3);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		}

	}

	private boolean handWallMounted(MotionEvent e) {
		float down_X=e.getRawX();
		float down_Y=e.getRawY();
		int averageW=screen_width/5;
		int averageH=screen_height/3;
		boolean one=down_X>0&&down_X<averageW&&down_Y>0&&down_Y<averageH;
		boolean two=down_X>averageW&&down_X<averageW*2&&down_Y>0&&down_Y<averageH;
		boolean three=down_X>averageW*2&&down_X<averageW*3&&down_Y>0&&down_Y<averageH;
		boolean four=down_X>averageW*3&&down_X<averageW*4&&down_Y>0&&down_Y<averageH;
		boolean five=down_X>averageW*4&&down_X<averageW*5&&down_Y>0&&down_Y<averageH;

		boolean six=down_X>0&&down_X<averageW&&down_Y>averageH&&down_Y<averageH*2;
		boolean seven=down_X>averageW&&down_X<averageW*2&&down_Y>averageH&&down_Y<averageH*2;
		boolean eight=down_X>averageW*2&&down_X<averageW*3&&down_Y>averageH&&down_Y<averageH*2;
		boolean nine=down_X>averageW*3&&down_X<averageW*4&&down_Y>averageH&&down_Y<averageH*2;
		boolean ten= down_X>averageW*4&&down_X<averageW*5&&down_Y>averageH&&down_Y<averageH*2;

		boolean eleven=down_X>0&&down_X<averageW&&down_Y>averageH*2&&down_Y<averageH*3;
		boolean twelve=down_X>averageW&&down_X<averageW*2&&down_Y>averageH*2&&down_Y<averageH*3;
		boolean thirteen=down_X>averageW*2&&down_X<averageW*3&&down_Y>averageH*2&&down_Y<averageH*3;
		boolean fourteen=down_X>averageW*3&&down_X<averageW*4&&down_Y>averageH*2&&down_Y<averageH*3;
		boolean fifteen=down_X>averageW*4&&down_X<averageW*5&&down_Y>averageH*2&&down_Y<averageH*3;

		if (this.GetFishLager() > (float) 0) {// 原画面正常的lager是0.0
			this.SetPosition(false, mSetPosition);
			return true;
		}
		if (one) {
			this.SetPosition(true, 1);
			mSetPosition = 1;
			Log.i("tedu", "--区域:--1--");
		}else if(two){
			this.SetPosition(true, 2);
			mSetPosition = 2;
			Log.i("tedu", "--区域:--2--");
		}else if (three) {
			this.SetPosition(true, 3);
			mSetPosition = 3;
			Log.i("tedu", "--区域:--3--");
		}else if (four) {
			this.SetPosition(true, 4);
			mSetPosition = 4;
			Log.i("tedu", "--区域:--4--");
		}else if (five) {
			this.SetPosition(true, 5);
			mSetPosition = 5;
			Log.i("tedu", "--区域:--5--");
		}else if (six) {
			this.SetPosition(true, 6);
			mSetPosition = 6;
			Log.i("tedu", "--区域:--6--");
		}else if (seven) {
			this.SetPosition(true, 7);
			mSetPosition = 7;
			Log.i("tedu", "--区域:--7--");
		}else if (eight) {
			this.SetPosition(true, 8);
			mSetPosition = 8;
			Log.i("tedu", "--区域:--8--");
		}else if (nine) {
			this.SetPosition(true, 9);
			mSetPosition = 9;
			Log.i("tedu", "--区域:--9--");
		}else if (ten) {
			this.SetPosition(true, 10);
			mSetPosition = 10;
			Log.i("tedu", "--区域:--10--");
		}else if (eleven) {
			this.SetPosition(true, 11);
			mSetPosition = 11;
			Log.i("tedu", "--区域:--11--");
		}else if (twelve) {
			this.SetPosition(true, 12);
			mSetPosition = 12;
			Log.i("tedu", "--区域:--12--");
		}else if (thirteen) {
			this.SetPosition(true, 13);
			mSetPosition = 13;
			Log.i("tedu", "--区域:--13--");
		}else if (fourteen) {
			this.SetPosition(true, 14);
			mSetPosition = 14;
			Log.i("tedu", "--区域:--14--");
		}else if (fifteen) {
			this.SetPosition(true, 15);
			mSetPosition = 15;
			Log.i("tedu", "--区域:--15--");
		}
		return true;
	}

	private void handlerFrameMode1(float x, float y, float pxTopview, float centerPoint, boolean area_one, boolean area_two, boolean area_there, boolean area_four) {
		if (area_one) {
			float distanceX = screen_width / 2 - x;
			float distanceY = screen_width / 2 + pxTopview - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_width / 2 && distance > centerPoint) {// 根据勾股定理算出来的距离如果小于半径就在区域1里面
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的6模块
					setPostion(6);
				} else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的6模块
					setPostion(7);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		} else if (area_two) {
			float distanceX = x - screen_width / 2;
			float distanceY = screen_width / 2 + pxTopview - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_width / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的1模块
					setPostion(1);
				} else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的0模块
					setPostion(0);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		} else if (area_there) {
			float distanceX = screen_width / 2 - x;
			float distanceY = y - (screen_width / 2 + pxTopview);
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_width / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的5模块
					setPostion(5);
				} else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的4模块
					setPostion(4);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		} else if (area_four) {
			float distanceX = x - screen_width / 2;
			float distanceY = y - screen_width / 2 - pxTopview;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_width / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的2模块
					setPostion(2);
				} else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// 我是真正的3模块
					setPostion(3);
				}
			} else if (distance < centerPoint) {
				setPostion(8);
			}
		}
	}

	private void setPostion(int position) {
		if (!mIsZoom) {
			this.SetPosition(true, position);
			mIsZoom = !mIsZoom;
			mSetPosition = position;
		}
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	private void startRotate(int No, final int gesture, final int num) {
		Log.i("tedu", "startRotate::" + gesture + "::::" + num);
		if (mthreadGesture_2 != null) {
			mthreadGesture_2.stopThread();
			mthreadGesture_2 = null;
		}
		if (No == 1) {
			if (mthreadGesture != null) {
				mthreadGesture.stopThread();
				mthreadGesture = null;
			}
			mthreadGesture = new ThreadGesture();
			mthreadGesture.SetValue(num, gesture, -1);
			mthreadGesture.startThread();
		} else {
			mthreadGesture_2 = new ThreadGesture();
			mthreadGesture_2.SetValue(num, gesture, -1);
			mthreadGesture_2.startThread();
		}

	}

	public ThreadGesture mthreadGesture = null;
	public ThreadGesture mthreadGesture_2 = null;

	public class ThreadGesture extends HiThread {
		int num;
		int gesture;
		int no;

		public void SetValue(int num, int gesture, int no) {
			this.num = num;
			this.gesture = gesture;
			this.no = no;
		}

		public void run() {
			int i = 0;
			while (isRunning && i < num) {
				if (i < num / 5)
					i++;
				else if (i < num * 2 / 5)
					i += 5;
				else if (i < num * 3 / 5)
					i += 3;
				else if (i < num * 4 / 5)
					i += 1;
				else
					i++;
				try {
					Thread.sleep(20);
					int b = (num * 2 / 3 - i);
					if (b < 4)
						b = 4;
					int j = b / 4;
					for (int a = j; a >= 0; a--) {
						if (no != -1)
							MyLiveViewGLMonitor.this.SetGesture(gesture, no);
						else
							MyLiveViewGLMonitor.this.SetGesture(gesture);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean mIsZoom = false;

	public void showMyToast(final Toast toast, final int cnt) {
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				toast.show();
			}
		}, 0, 3000);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				toast.cancel();
				timer.cancel();
			}
		}, cnt);
	}

}
