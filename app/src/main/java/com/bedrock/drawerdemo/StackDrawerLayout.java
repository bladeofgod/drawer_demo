package com.bedrock.drawerdemo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;


/**
 * @author 李佳奇
 *
 */
public class StackDrawerLayout extends FrameLayout {

    private View backView;

    private View drawerView;
    private View a_view;
    private View a_view_1,a_view_2,a_view_3;

    private View b_view;
    private View b_view_1,b_view_2;

    //在 setContentView 完成后会调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        backView = getChildAt(0);
        drawerView = getChildAt(1);
        a_view = drawerView.findViewById(R.id.view_a);
        a_view_1 = drawerView.findViewById(R.id.tv_a_1);
        a_view_2 = drawerView.findViewById(R.id.tv_a_2);
        a_view_3 = drawerView.findViewById(R.id.tv_a_3);

        b_view = drawerView.findViewById(R.id.view_b);
        b_view_1 = drawerView.findViewById(R.id.tv_b_1);
        b_view_2 = drawerView.findViewById(R.id.tv_b_2);
    }

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
        initVar(context);
    }

    public StackDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    //滑动速度跟踪器
    private VelocityTracker velocityTracker;
    private int maxVelocity;


    private void initVar(Context context){
        this.mContext = context;

        init(context);
        //View Configuration 获取当前view的一些配置信息
        //最大 fling 速度
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        //ViewDragHelper是针对 ViewGroup 中的拖拽和重新定位 views
        // 操作时提供了一系列非常有用的方法和状态追踪。基本上使用在自定义ViewGroup处理拖拽中！
        ///
        //ViewDragHelper create(ViewGroup forParent, Callback cb)；一个静态的创建方法，
        //参数1：出入的是相应的ViewGroup
        //参数2：是一个回掉（其实这个回掉你可以自己在外面实现，后面在细说）
        //shouldInterceptTouchEvent(MotionEvent ev) 处理事件分发的（主要是将ViewGroup的事件分发，
        // 委托给ViewDragHelper进行处理）
        //参数1：MotionEvent ev 主要是ViewGroup的事件
        //processTouchEvent(MotionEvent event) 处理相应TouchEvent的方法，这里要注意一个问题，处理相应的TouchEvent的时候要将结果返回为true，消费本次事件！否则将无法使用ViewDragHelper处理相应的拖拽事件！
        this.dragHelper = ViewDragHelper.create(this,mCallback);
    }


    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        //tryCaptureView(View child, int pointerId) 这是一个抽象类，必须去实现
        // ，也只有在这个方法返回true的时候下面的方法才会生效；
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == drawerView;
        }

        //可用于制作回弹等滑动操作
        //参数1：拖拽的View
        //参数2：距离顶部的距离
        //参数3：变化量
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if(child == drawerView){
                return reAdjustTop(top);
            }
            return top;
        }

        //getViewHorizontalDragRange和getViewVerticalDragRange
        //这两个方法返回大于0时，才会走onInterceptTouchEvent，换言之，才能交给dragHelper处理
        @Override
        public int getViewVerticalDragRange(View child) {
            if (child == drawerView) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }
            return super.getViewVerticalDragRange(child);
        }

        //onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
        // 当你拖动的View位置发生改变的时候回调
        //
        //参数1：你当前拖动的这个View
        //参数2：距离左边的距离
        //参数3：距离右边的距离
        //参数4：x轴的变化量
        //参数5：y轴的变化量

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == drawerView) {
                //这里将top转为 drawer的高度
                int height = screenHeight - top;
                changeDrawerViewHeight(height);
                dispatchDragViewEvent(top);
            }
            invalidate();
        }

        //当View停止拖拽的时候调用的方法，一般在这个方法中重置一些参数，比如回弹什么的。。。
        //
        //参数1：你拖拽的这个View
        //参数2：x轴的速率
        //参数3：y轴的速率

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if(releasedChild == drawerView){
                int height = releasedChild.getHeight();
                if(mPreStatus == DrawerStatus.Close &&  yvel < -Y_VELOCITY){
                    //drawer 关闭时，如果向上滑动速度大于 设置的临界值
                    //打开
                    middle();
                }else if(mPreStatus == DrawerStatus.Middle && yvel < -Y_VELOCITY){
                    open();
                }else if(mPreStatus == DrawerStatus.Middle && yvel > Y_VELOCITY){
                    //快速向下滑动 关闭
                    close();
                }else if(mPreStatus == DrawerStatus.Open && yvel > Y_VELOCITY){
                    //open -> middle
                    middle();
                }else if(height >= closeHeight && height <= middleHeight){
                    //滑动到关闭 - 中间 的位置
                    if(height < closeToMiddleHalf){
                        //没过 则关闭
                        close();
                    }else{
                        middle();
                    }
                }else if(height >= middleHeight && height < openHeight){
                    //滑动到  中间 -  打开 的位置
                    if(height > middleToOpenHalf){
                        //
                        open();
                    }else{
                        middle();
                    }
                }
            }
        }


        //onViewCaptured(View capturedChild, int activePointerId)捕获View的时候调用的方法
        //
        //参数1：捕获的View（也就是你拖动的这个View）
        //参数2：这个参数我也不知道什么意思API中写的一个什么指针，这里没有到也没有注意

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }


        //onViewDragStateChanged(int state) 当状态改变的时候回调，返回相应的状态（这里有三种状态）
        //
        //STATE_IDLE 闲置状态
        //STATE_DRAGGING 正在拖动
        //STATE_SETTLING 放置到某个位置


        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }


    };

    /**
     * 上一个非拖拽状态
     */
    private DrawerStatus mPreStatus;

    /**
     * @param top drawerView的 margin top
     */
    private void dispatchDragViewEvent(int top){
        if(mListener != null){
            mListener.onDragging(top);
        }
        if(status != DrawerStatus.Dragging){
            mPreStatus = status;
        }

        // 更新状态, 执行回调
        DrawerStatus preStatus = status;
        status = updateStatus(top);

        if(status != preStatus){
            //drawer 从一个固定状态 到 另一个固定状态
            if(status == DrawerStatus.Close){
                if(mListener != null){
                    mListener.onClose();
                }
            }else if(status == DrawerStatus.Open){
                if(mListener != null){
                    mListener.onOpen();
                }
            }else if(status == DrawerStatus.Middle){
                if(mListener != null){
                    mListener.onMiddle();
                }
            }
        }


    }

    private DrawerStatus updateStatus(int top){
        //将top 转为 height
        int height = screenHeight - top;
        if(height == closeHeight){
            return DrawerStatus.Close;
        }else if(height == openHeight){
            return DrawerStatus.Open;
        }else if(height == middleHeight){
            return DrawerStatus.Middle;
        }
        return DrawerStatus.Dragging;
    }

    private int reAdjustTop(int top){
        int openTop = screenHeight - openHeight;
        int middleTop = screenHeight = middleHeight;
        int closeTop = screenHeight - closeHeight;
        if(top <= openHeight && top > middleTop){
            return openTop;
        }else if(top > closeTop){
            return middleTop;
        }else{
            return closeTop;
        }
    }

    /**
     * 初始化状态使用
     */
    private boolean mConfigurationChangedFlag = true;

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfigurationChangedFlag = true;
    }

    private float middleTopPercent = 0.4f;

    //各状态下的margin top
    private int closeHeight;
    private int openHeight;
    private int middleHeight;

    //关闭状态与中间状态一半的位置
    private int closeToMiddleHalf;
    //中间状态到打开状态的一半位置
    private int middleToOpenHalf;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mConfigurationChangedFlag){
            mConfigurationChangedFlag = false;
            Log.i("drawer", "getMeasuredHeight :" +getMeasuredHeight());
            Log.i("drawer", "drawerView getMeasuredHeight :" +drawerView.getMeasuredHeight());

            //页面初始 只显示 A1
            closeHeight =  a_view_1.getMeasuredHeight();
            //全打开
            openHeight = getMeasuredHeight();
            //触发 滚动顶部的临界值
            middleHeight = a_view.getMeasuredHeight();

            closeToMiddleHalf = a_view.getMeasuredHeight() / 2;
            middleToOpenHalf = (getMeasuredHeight() - a_view.getMeasuredHeight()) / 2
                                     + a_view_1.getMeasuredHeight();

            switch (status){
                case Close:
                    close(false);
                    break;
                case Open:
                    open(false);
                    break;
                case Middle:
                    middle(false);
                    break;
            }


        }
    }

    public void close(){
        close(true);
    }

    private void close(boolean isSmooth){
        if(isSmooth){
            animateHandler(closeHeight);
        }else{
            changeDrawerViewHeight(closeHeight);
            status = DrawerStatus.Close;
        }
    }

    public void middle(){
        middle(true);
    }

    private void middle(boolean isSmooth){
        if(isSmooth){
            animateHandler(middleHeight);
        }else{
            changeDrawerViewHeight(middleHeight);
            status = DrawerStatus.Middle;
        }
    }


    public void open(){
        open(true);
    }

    private void open(boolean isSmooth){
        if(isSmooth){
            animateHandler(openHeight);
        }else{
            changeDrawerViewHeight(openHeight);
            status = DrawerStatus.Open;
        }
    }

    private void changeDrawerViewHeight(int height){
        LayoutParams lp = (LayoutParams)drawerView.getLayoutParams();
        lp.height = height;
        drawerView.setLayoutParams(lp);
    }


    /**
     * @param openHeight
     *
     *      * Animate the view <code>child</code> to the given (left, top) position.
     *      * If this method returns true, the caller should invoke {@link #(boolean)}
     *      * on each subsequent frame to continue the motion until it returns false. If this method
     *      * returns false there is no further work to do to complete the movement.
     *  smoothSlideViewTo 返回true时，它的callback方法会被回调
     *  false 则不会
     */
    private void animateHandler(int openHeight){
        int top = screenHeight - openHeight;
        if(dragHelper.smoothSlideViewTo(drawerView, 0, top)){
            //依赖上一帧动画的的执行时间
            //上一帧动画绘制结束后，再postInvalidate
            ViewCompat.postInvalidateOnAnimation(this);

        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 2. 持续平滑动画 (高频率调用)
        if(dragHelper == null) return;
        if (dragHelper.continueSettling(true)) {
            //  如果返回true, 动画还需要继续执行
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private int screenHeight;

    //初始化窗口数据
    private void init(final Context context){
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //获取显示的区域的大小
                ((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                if(screenHeight == 0){
                    screenHeight = r.bottom;
                }
            }
        });
    }

    private float xDistance, yDistance, xLast, yLast;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //是否消费
        boolean interceptOr = dragHelper.shouldInterceptTouchEvent(ev);

        final VelocityTracker verTracker = velocityTracker;

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;
                //xDistance < yDistance ：表示向下滑动偏垂直向下,而不是偏左右；
                //这个时候就拦截事件，自己处理
                if(interceptOr && xDistance < yDistance * 0.7f){
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                default:break;
        }


        return dragHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        //返回true 表示 自己消费，具体如何消费由上面的方法处理
        return true;
    }
}



















