package com.zzc.superscript;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;

/**
 * 新功能推荐标签
 * <p/>
 * Created by zczhang on 16/5/10.
 */
public class SuperScript extends View {
    private static final String TAG = "NewFuncRecView";

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 推荐语
     */
    private String mTip;

    /**
     * 需要显示推荐标签的目标视图
     */
    private View mTargetView;

    /**
     * 默认文本色
     */
    private static final String DEFAULT_TEXT_COLOR = "#FFFFFF";

    /**
     * 默认背景色
     */
    private static final String DEFAULT_BG_COLOR = "#FD5359";

    /**
     * 默认显示文本
     */
    private static final String DEFAULT_TEXT = "测试";

    /**
     * 默认渲染模式
     */
    private static final int DEFAILT_RENDER_MODEL = RenderModel.RENDER_MODEL_BY_RATIO;

    /**
     * 角标配置
     */
    private SuperScriptConfig mConfig;

    private int mTargetViewWidth;

    private int mTargetViewHeight;

    private Paint mTextPaint;
    private Paint mBgPaint;
    private Path mTextPath;
    private Path mBgPath;

    private int mTextHeight;
    private int mTextPadding;
    private float mTextXOffset;//文字的偏移
    private int mTextSize = 11;//文字大小
    private float mHorizontalRatio = 0.55f;
    private boolean isShow = true;

    public SuperScript(Context context) {
        super(context);
    }

    public SuperScript(final Context context, AttributeSet attrs) {
        super(context, attrs);
//        post(new Runnable() {
//            @Override
//            public void run() {
//                mTargetView = (View) getParent();
//                mTip = DEFAULT_TEXT;
//                initParam(context);
//            }
//        });
    }

    public SuperScript(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SuperScript(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 为目标视图设置推荐标签
     *
     * @param context
     * @param targetView
     * @param tip
     */
    public void setTarget(Context context, View targetView, String tip) {
        this.mContext = context;
        this.mTargetView = targetView;
        this.mTip = tip;
        mTargetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeOnGlobalLayoutListener(mTargetView, this);
                if (mTargetView.getWidth() != 0 && mTargetView.getHeight() != 0) {
                    initParam(mContext);
                }
            }
        });
    }

    /**
     * 初始化参数
     */
    private void initParam(Context context) {
        mTargetViewWidth = mTargetView.getWidth();
        mTargetViewHeight = mTargetView.getHeight();
        int screenWidth = getScreenWidth(context);
        int numPerLine = screenWidth / mTargetViewWidth;
        //根据target视图的宽和高,使用不同的文字尺寸
        if (numPerLine == 4) {
            mTextSize = 9;
            mHorizontalRatio = 0.50f;
        } else {
            mTextSize = 11;
            mHorizontalRatio = 0.55f;
        }
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.parseColor(DEFAULT_BG_COLOR));
        mTextHeight = dip2px(mContext, mTextSize);
        mTextPadding = dip2px(mContext, 7);
        mTextPaint.setTextSize(mTextHeight);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setStyle(Paint.Style.FILL);
        mBgPaint = new Paint();
        mBgPaint.setColor(Color.parseColor(DEFAULT_TEXT_COLOR));
        mBgPaint.setStyle(Paint.Style.FILL);

        mBgPath = createBgPath();
        mTextPath = createTextPath();
        createTextOffset();

        if (mTargetView instanceof ViewGroup) {
            if (getParent() == null) {
                ViewGroup targetView = ((ViewGroup) mTargetView);
                int childSize = targetView.getChildCount();
                //如果最上层View不是NewFunctionRecommendView类型的,才为当前布局添加NewFunctionRecommendView视图
                if (!(targetView.getChildAt(childSize - 1) instanceof SuperScript)) {
                    targetView.addView(this);
                }
            }
        } else {
            Log.e(TAG, "target view is must be a ViewGroup!");
        }
    }

    public void hide() {
        isShow = false;
        if (mTargetView != null && mTargetView instanceof ViewGroup) {
            ViewGroup targetView = ((ViewGroup) mTargetView);
            targetView.removeView(this);
        }
    }

    //计算文字位移
    private void createTextOffset() {
        float textWidth = mTextPaint.measureText(mTip.trim());
        float sideLength = (mTargetViewWidth * (1 - mHorizontalRatio)) - mTextPadding;
        float totalWidth = (float) Math.sqrt(sideLength * sideLength * 2);
        mTextXOffset = Float.parseFloat(decimalDivide(String.valueOf(totalWidth - textWidth), String.valueOf(2), 5));
    }

    //文字路径
    private Path createTextPath() {
        Path path = new Path();
        int startPosX = (int) (mTargetViewWidth * mHorizontalRatio + mTextPadding);
        int startPosY = 0;
        path.moveTo(startPosX, startPosY);

        path.lineTo(mTargetViewWidth, mTargetViewWidth - startPosX);

        return path;
    }

    //创建背景矩形
    private Path createBgPath() {
        Path path = new Path();
        int startPosX = (int) (mTargetViewWidth * mHorizontalRatio);
        int startPosY = 0;
        path.moveTo(startPosX, startPosY);
        int secondPosX = (int) (startPosX + Math.sqrt((mTextHeight + 2 * mTextPadding) * (mTextHeight + 2 * mTextPadding)));
        int secondPosY = 0;
        path.lineTo(secondPosX, secondPosY);
        int thirdPosX = mTargetViewWidth;
        int thirdPosY = mTargetViewWidth - secondPosX;
        path.lineTo(thirdPosX, thirdPosY);
        int fourthPosX = mTargetViewWidth;
        int fourthPosY = (int) (mTargetViewWidth * (1 - mHorizontalRatio));
        path.lineTo(fourthPosX, fourthPosY);
        path.close();
        return path;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mTargetViewWidth, mTargetViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isShow) {
            canvas.drawPath(mBgPath, mBgPaint);

            canvas.drawTextOnPath(mTip, mTextPath, mTextXOffset, 0, mTextPaint);
            canvas.save();
        }
    }

    //获取屏幕宽度
    private int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

    //单位转换
    private int dip2px(Context context, int dpValue) {
        if (context == null) {
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //数字除法
    private String decimalDivide(String s1, String s2, int decimalNum) {
        if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2)) {
            return "params error";
        }
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal b2 = new BigDecimal(s2);
        String bigDecimal = b1.divide(b2, decimalNum, BigDecimal.ROUND_HALF_EVEN).toString();
        return bigDecimal;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    private static class SuperScriptConfig {
        private String text;//推荐语
        private String bgColor;//背景色
        private String textColor;//文本色
        private int textSize;//文本尺寸
        private int model;//显示模式

        /**
         * 角标配置信息
         *
         * @param text      角标内容
         * @param bgColor   背景色
         * @param textColor 文本色
         * @param textSize  文本尺寸
         * @param model     渲染模式
         */
        public SuperScriptConfig(String text, String bgColor, String textColor, int textSize, @RenderModel.RenderModelTemplate int model) {
            this.text = text;
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.model = model;
        }

        public String getText() {
            return text;
        }

        public String getBgColor() {
            return bgColor;
        }

        public String getTextColor() {
            return textColor;
        }

        public int getTextSize() {
            return textSize;
        }

        public int getModel() {
            return model;
        }
    }

    /**
     * 渲染模式
     */
    public static class RenderModel {
        /**
         * 按照宿主View的比例渲染
         * <p/>
         * 如宽度的0.5处
         */
        public static final int RENDER_MODEL_BY_RATIO = 0;
        /**
         * 按照提示文本长度计算渲染位置
         * <p/>
         * 如"欢迎试用"四个字的长度
         */
        public static final int RENDER_MODEL_BY_CONTENT = 1;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RENDER_MODEL_BY_RATIO, RENDER_MODEL_BY_CONTENT})
        public @interface RenderModelTemplate {

        }
    }

}
