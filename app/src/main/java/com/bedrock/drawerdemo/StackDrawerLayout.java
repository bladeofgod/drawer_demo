package com.bedrock.drawerdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;


/**
 * @author 李佳奇
 * @date 2020.1.29
 *
 *仿高德地图缩进式抽屉
 *
 * 抽屉概述：
 *    抽屉view内部有两层view，这里简称A、B
 *
 *    A view 内部分为（以交互逻辑划分）三块，分别在抽屉位置：底部、中部和顶部显示
 *    这里暂标注为 A1、A2、A3
 *    底部显示A1、中部显示A1A2、顶部（全展开）显示A1A2A3
 *
 *    B view 内部分为两块  B1、B2
 *
 *    中部显示B1 、 顶部 显示B1B2
 *
 */
public class StackDrawerLayout extends FrameLayout {

    private View backView;

    private View drawerView;
    private View a_view;
    private View a_view_1,a_view_2,a_view_3;

    private View b_view;
    private View b_view_1,b_view_2;

    private Context mContext;

    private final static int Y_VELOCITY = 600;


    private ViewDragHelper dragHelper;

    private DrawerStatus status = DrawerStatus.Close;

    public DrawerStatus getStatus() {
        return status;
    }

    public static enum DrawerStatus{
        Close,Open,Middle,Dragging,
    }

    private OnDragStatusChangeListener mListener;

    public void setOnDragStatusChangeListener(OnDragStatusChangeListener listener) {
        this.mListener = listener;
    }

    /**
     * 状态监听
     */
    public interface OnDragStatusChangeListener {
        void onClose();

        void onOpen();

        void onMiddle();

        void onDragging(int top);
    }



    public StackDrawerLayout(@NonNull Context context) {
        super(context);
    }

    public StackDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StackDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}



















