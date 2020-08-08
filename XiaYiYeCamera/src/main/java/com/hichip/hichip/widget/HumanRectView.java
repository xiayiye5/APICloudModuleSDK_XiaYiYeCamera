package com.hichip.hichip.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hichip.thecamhi.bean.HumanRect;

import java.util.List;


/**
 * created by Deaven
 * on date  2019/8/16 9:23
 */
public class HumanRectView extends View {

    private Paint mPaint;
    private int layout_width; //布局的宽
    private int layout_height;//布局的高
    //最多三个框
    private int point1_x, point1_y, point1_x1, point1_y1;
    private int point2_x, point2_y, point2_x1, point2_y1;
    private int point3_x, point3_y, point3_x1, point3_y1;

    public HumanRectView(Context context) {
        super(context);
    }

    public HumanRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#ffff00"));           //设置画笔颜色
        mPaint.setStyle(Paint.Style.STROKE);  //设置画笔模式为描边
        mPaint.setStrokeWidth(3f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        layout_width = w;
        layout_height = h;
        super.onSizeChanged(w, h, old_w, old_h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        //框一
        canvas.drawRect(point1_x, point1_y, point1_x1, point1_y1, mPaint);
        //框二
        canvas.drawRect(point2_x, point2_y, point2_x1, point2_y1, mPaint);
        //框三
        canvas.drawRect(point3_x, point3_y, point3_x1, point3_y1, mPaint);
    }


    public void refreshRect(List<HumanRect> humanRects, int VideoQuality) {
        cleanRect();
        Log.e("==tedu-rectView", "refreshRect: " + humanRects.toString());
        if (humanRects.size() >= 1) {
            point1_x = humanRects.get(0).getX() * layout_width / humanRects.get(0).getMonitor_width();
            point1_y = humanRects.get(0).getY() * layout_height / humanRects.get(0).getMonitor_height();

            point1_x1 = point1_x +
                    (humanRects.get(0).getRect_width() * layout_width) / humanRects.get(0).getMonitor_width();

            point1_y1 = point1_y +
                    (humanRects.get(0).getRect_height() * layout_width) / humanRects.get(0).getMonitor_width();
        }

        if (humanRects.size() >= 2) {

            point2_x = humanRects.get(1).getX() * layout_width / humanRects.get(1).getMonitor_width();
            point2_y = humanRects.get(1).getY() * layout_height / humanRects.get(1).getMonitor_height();

            point2_x1 = point2_x +
                    (humanRects.get(1).getRect_width() * layout_width) / humanRects.get(1).getMonitor_width();

            point2_y1 = point2_y +
                    (humanRects.get(1).getRect_height() * layout_width) / humanRects.get(1).getMonitor_width();

        }

        if (humanRects.size() >= 3) {

            point3_x = humanRects.get(2).getX() * layout_width / humanRects.get(2).getMonitor_width();
            point3_y = humanRects.get(2).getY() * layout_height / humanRects.get(2).getMonitor_height();

            point3_x1 = point3_x +
                    (humanRects.get(2).getRect_width() * layout_width) / humanRects.get(2).getMonitor_width();

            point3_y1 = point3_y +
                    (humanRects.get(2).getRect_height() * layout_width) / humanRects.get(2).getMonitor_width();

        }


        postInvalidate();
    }

    public void cleanRect() {


        point1_x = 0;
        point1_y = 0;
        point1_x1 = 0;
        point1_y1 = 0;

        point2_x = 0;
        point2_y = 0;
        point2_x1 = 0;
        point2_y1 = 0;

        point3_x = 0;
        point3_y = 0;
        point3_x1 = 0;
        point3_y1 = 0;

        postInvalidate();
    }

}
