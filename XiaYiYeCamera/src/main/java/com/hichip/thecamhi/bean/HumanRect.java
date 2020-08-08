package com.hichip.thecamhi.bean;

public class HumanRect {
    private int x;
    private int y;
    private int rect_width;
    private int rect_height;
    private int monitor_width;
    private int monitor_height;


    /**
     * @param x              坐标点 横坐标
     * @param y              坐标点 纵坐标
     * @param rect_width     矩形宽度
     * @param rect_height    矩形高度
     * @param monitor_width  monitor 宽度
     * @param monitor_height monitor 高度
     */
    public HumanRect(int x, int y, int rect_width, int rect_height, int monitor_width, int monitor_height) {
        this.x = x;
        this.y = y;
        this.rect_width = rect_width;
        this.rect_height = rect_height;
        this.monitor_width = monitor_width;
        this.monitor_height = monitor_height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRect_width() {
        return rect_width;
    }

    public void setRect_width(int rect_width) {
        this.rect_width = rect_width;
    }

    public int getRect_height() {
        return rect_height;
    }

    public void setRect_height(int rect_height) {
        this.rect_height = rect_height;
    }

    public int getMonitor_width() {
        return monitor_width;
    }

    public void setMonitor_width(int monitor_width) {
        this.monitor_width = monitor_width;
    }

    public int getMonitor_height() {
        return monitor_height;
    }

    public void setMonitor_height(int monitor_height) {
        this.monitor_height = monitor_height;
    }



    @Override
    public String toString() {
        return "HumanRect{" +
                "x=" + x +
                ", y=" + y +
                ", rect_width=" + rect_width +
                ", rect_height=" + rect_height +
                ", monitor_width=" + monitor_width +
                ", monitor_height=" + monitor_height +
                '}';
    }
}
