/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shenrh.canvas;

import static java.lang.Math.max;

import android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

/**
 * 对应到View
 * 
 * @author shenrh
 * 
 */
@SuppressLint("WrongCall")
public class UIElement implements Drawable.Callback{
	
    protected UIContext mContext;

    private int mId;
    protected Object mTag = null;

    protected int mMeasuredWidth;
    protected int mMeasuredHeight;

    private Rect mBounds = new Rect();
    private Rect mPadding = new Rect();

    private LayoutParams mLayoutParams;

    private int mVisibility = View.VISIBLE;

    private boolean mClickable = true;
    private boolean mEnabled = true;
    private boolean mRequestLayout = false;

    private Drawable mBackGround;
    private boolean mIsOnTouch = false;// 当前是否处于touch状态
    private UIElement mLastTouchedUIElement;// 上一个被触摸的Element，在group的情况下可能会move走
    private UIElementGroup mLastTouchedUIElementGroup; // 上一个被触摸的ElementGroup

    private ListenerInfo mListenerInfo;
    private int mTouchSlop;
    
    //Drawable State定义汇总
    private static int[][] VIEW_STATE_SETS;
    
    static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    static final int VIEW_STATE_SELECTED = 1 << 1;
    static final int VIEW_STATE_FOCUSED = 1 << 2;
    static final int VIEW_STATE_ENABLED = 1 << 3;
    static final int VIEW_STATE_PRESSED = 1 << 4;
    static final int VIEW_STATE_ACTIVATED = 1 << 5;
    static final int VIEW_STATE_ACCELERATED = 1 << 6;
    static final int VIEW_STATE_HOVERED = 1 << 7;
    static final int VIEW_STATE_DRAG_CAN_ACCEPT = 1 << 8;
    static final int VIEW_STATE_DRAG_HOVERED = 1 << 9;
    
    static final int[] VIEW_STATE_IDS = new int[] {
       R.attr.state_window_focused,    VIEW_STATE_WINDOW_FOCUSED,
       R.attr.state_selected,          VIEW_STATE_SELECTED,
       R.attr.state_focused,           VIEW_STATE_FOCUSED,
       R.attr.state_enabled,           VIEW_STATE_ENABLED,
       R.attr.state_pressed,           VIEW_STATE_PRESSED,
       R.attr.state_activated,         VIEW_STATE_ACTIVATED,
       R.attr.state_accelerated,       VIEW_STATE_ACCELERATED,
       R.attr.state_hovered,           VIEW_STATE_HOVERED,
       R.attr.state_drag_can_accept,   VIEW_STATE_DRAG_CAN_ACCEPT,
       R.attr.state_drag_hovered,      VIEW_STATE_DRAG_HOVERED
    };
    
    static final int[] VIEW_DRAWABLE_STATES = {
    		R.attr.state_pressed, R.attr.state_focused, R.attr.state_selected, R.attr.state_window_focused, R.attr.state_enabled
    };
    
    static {

        int[] orderedIds = new int[VIEW_STATE_IDS.length];
        for (int i = 0; i < VIEW_DRAWABLE_STATES.length; i++) {
            int viewState = VIEW_DRAWABLE_STATES[i];
            for (int j = 0; j<VIEW_STATE_IDS.length; j += 2) {
                if (VIEW_STATE_IDS[j] == viewState) {
                    orderedIds[i * 2] = viewState;
                    orderedIds[i * 2 + 1] = VIEW_STATE_IDS[j + 1];
                }
            }
        }
        final int NUM_BITS = VIEW_STATE_IDS.length / 2;
        VIEW_STATE_SETS = new int[1 << NUM_BITS][];
        for (int i = 0; i < VIEW_STATE_SETS.length; i++) {
            int numBits = Integer.bitCount(i);
            int[] set = new int[numBits];
            int pos = 0;
            for (int j = 0; j < orderedIds.length; j += 2) {
                if ((i & orderedIds[j+1]) != 0) {
                    set[pos++] = orderedIds[j];
                }
            }
            VIEW_STATE_SETS[i] = set;
        }
    }

    public UIElement(UIContext host) {
        this(host, null);
    }

    public UIElement(UIContext host, AttributeSet attrs) {
        setContext(host);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    protected void onAttachedToWindow() {
    }

    protected void onDetachedFromWindow() {
    }

    public boolean setContext(UIContext context) {
        if (mContext == context) {
            return false;
        }

        if (mContext != null) {
            onDetachedFromWindow();
        }

        mContext = context;

        if (mContext != null) {
            onAttachedToWindow();
        }

        return true;
    }

    public boolean isAttachedToWindow() {
        return (mContext != null);
    }

    public int getId() {
        return mId;
    }

    protected void setMeasuredDimension(int width, int height) {
        mMeasuredWidth = width;
        mMeasuredHeight = height;
    }

    public int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    public int getPaddingLeft() {
        return mPadding.left;
    }

    public int getPaddingTop() {
        return mPadding.top;
    }

    public int getPaddingRight() {
        return mPadding.right;
    }

    public int getPaddingBottom() {
        return mPadding.bottom;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPadding.left = left;
        mPadding.top = top;
        mPadding.right = right;
        mPadding.bottom = bottom;

        requestLayout();
    }

    public int getVisibility() {
        return mVisibility;
    }

    public void setVisibility(int visibility) {
        if (mVisibility == visibility) {
            return;
        }

        mVisibility = visibility;

        requestLayout();
        invalidate();
    }

    public final void draw(Canvas canvas) {

    	if (mBackGround != null) {
            mBackGround.setBounds(mBounds);
            mBackGround.draw(canvas);
        }

        final int saveCount = canvas.getSaveCount();
        canvas.save();

        canvas.clipRect(mBounds);
        canvas.translate(mBounds.left, mBounds.top);

        onDraw(canvas);

        canvas.restoreToCount(saveCount);
    }

    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void layout(int left, int top, int right, int bottom) {
        mBounds.left = left;
        mBounds.top = top;
        mBounds.right = right;
        mBounds.bottom = bottom;

        onLayout(left, top, right, bottom);
    }

    public int getLeft() {
        return mBounds.left;
    }

    public int getTop() {
        return mBounds.top;
    }

    public int getRight() {
        return mBounds.right;
    }

    public int getBottom() {
        return mBounds.bottom;
    }

    public int getWidth() {
        return mBounds.right - mBounds.left;
    }

    public int getHeight() {
        return mBounds.bottom - mBounds.top;
    }

    public void setLayoutParams(LayoutParams lp) {
        if (mLayoutParams == lp) {
            return;
        }

        mLayoutParams = lp;
        requestLayout();
    }

    public LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public void onFinishInflate() {
    }

    public Context getContext() {
    	return mContext == null ? null : mContext.getContext();
    }

    public UIContext getUIContext() {
        return mContext;
    }

    public Resources getResources() {
    	return mContext == null ? null : mContext.getResources();
    }

    public void requestLayout() {
        if (mContext == null) {
            return;
        }
        mContext.requestLayout();
    }

    public void invalidate() {
        if (mContext == null) {
            return;
        }
        mContext.invalidate(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom);
    }

    public void invalidate(int l, int t, int r, int b) {
        if (mContext == null) {
            return;
        }
        mContext.invalidate(l, t, r, b);
    }

    /**
     * 目前只支持两层包含关系 LinearLayoutUIElement UIElementGroup UIElement
     * 
     * @return
     */
    public int getElementTop() {
        int top = 0;

        if (getParent() != null) {
            if (getParent().getParent() != null && getParent().getParent() instanceof LinearLayoutUIElement) {
                top = getParent().getElementTop(this);
            } else {
                top = getParent().getTop();
            }
        }
        return top;
    }

    /**
     * 目前只支持两层包含关系 LinearLayoutUIElement UIElementGroup UIElement
     * 
     * @return
     */
    public int getElementLeft() {
        int left = 0;
        if (getParent() != null) {
            if (getParent().getParent() != null && getParent().getParent() instanceof LinearLayoutUIElement) {
                left = getParent().getElementLeft(this);
            } else {
                left = getParent().getLeft();
            }
        }
        return left;
    }

    /**
     * x y是在elementgroup中的位置，不同于Android view相对于自己的位置
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean isTouchOnElement(int x, int y) {
        int mtop = 0;
        int mleft = 0;
        int mbottom = 0;
        int mright = 0;
        if (mLayoutParams instanceof MarginLayoutParams) {
            mtop = ((MarginLayoutParams) mLayoutParams).topMargin;
            mleft = ((MarginLayoutParams) mLayoutParams).leftMargin;
            mbottom = ((MarginLayoutParams) mLayoutParams).bottomMargin;
            mright = ((MarginLayoutParams) mLayoutParams).rightMargin;
        }

        return x - getElementLeft() >= mBounds.left - mleft && y - getElementTop() >= mBounds.top - mtop
                && x - getElementLeft() <= mBounds.right + mbottom && y - getElementTop() <= mBounds.bottom + mright;
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public void setBackground(Drawable background) {
    	if(mBackGround == background){
    		return;
    	}
    	
    	if(mBackGround == null)
    		mRequestLayout = true;
    	
    	updateBackground(background);
    	
    	if(mRequestLayout || mBackGround.getMinimumHeight() != mBackGround.getMinimumWidth()
    			|| mBackGround.getMinimumWidth() != mBackGround.getMinimumWidth()){
        	requestLayout();
    	}
    	invalidate();
	
    }

	public void setBackgroundResource(int resid) {
        Drawable d = null;
        if (resid != 0) {
            d = mContext.getResources().getDrawable(resid);
        }
        setBackground(d);
    }

    public void setBackgroundColor(int color) {
        setBackground(new ColorDrawable(color));
    }
    
	private void updateBackground(Drawable background) {
		if (mBackGround != null){
			mBackGround.setCallback(null);
			mContext.unscheduleDrawable(mBackGround);
		}
			
		mBackGround = background;
		
		if(background != null){
			background.setCallback(this);
	   		if(background.isStateful()){
			   background.setState(getDrawableState());
	   		}
	   		
	   		background.setVisible(getVisibility() == View.VISIBLE, true);
	   	}
	}
    
	//结合mIsOnTouch处理点击事件的背景变换效果

    private void refreshTouchState(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            if (mIsOnTouch) {
                mIsOnTouch = false;
                drawableStateChanged();
                invalidate();
            }
            break;

        case MotionEvent.ACTION_DOWN:
            mIsOnTouch = true;
            drawableStateChanged();
            invalidate();
            break;

        case MotionEvent.ACTION_MOVE:
 	  	   	break;

        }
    }

    protected void clearTouchState() {
        if (mClickable) {
            mIsOnTouch = false;
            drawableStateChanged();
            invalidate();
        }
    }

    /**
     * 在响应touch事件后该值才有意义
     * 
     * @return
     */
    protected UIElement getTouchedElement() {
        return this;
    }

    private boolean dispatchTouchEvent(MotionEvent event) {
        if (this instanceof UIElementGroup) {
            return ((UIElementGroup) this).internalTouchEvent(event);
        } else {
            return onTouchEvent(event);
        }
    }

    private int downEventX = 0, downEventY = 0;

    private void dispatchClick(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            downEventX = (int) event.getX();
            downEventY = (int) event.getY();
            break;

        case MotionEvent.ACTION_MOVE:
            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (Math.abs(downEventX - event.getX()) < mTouchSlop && Math.abs(downEventY - event.getY()) < mTouchSlop) {
                dispatchClick(downEventX, downEventY);
            }
            break;
        }
    }

    private boolean mIntercept = false;

    protected boolean isInterceptClick() {
        return mIntercept;
    }

    /**
     * 是否拦截响应父element的onclick事件,默认不拦截
     * 
     * @param intercept
     */
    public void setInterceptClick(boolean intercept) {
        mIntercept = intercept;
    }

    private boolean dispatchClick(int x, int y) {
        if (this instanceof UIElementGroup) {
            return ((UIElementGroup) this).internalOnclick(x, y);
        } else {
            if (!isChild()) {// 作为子控件的情况已经在父控件分发的情况下处理过了，避免触发两次
                return callOnClick();
            }
        }
        return false;
    }

    public final boolean touchEvent(MotionEvent event) {
        boolean status = dispatchTouchEvent(event);
        if (status) {
            dispatchClick(event);

            if (mLastTouchedUIElement != getTouchedElement()) {
                if (mLastTouchedUIElement != null) {
                    mLastTouchedUIElement.clearTouchState();
                }
                mLastTouchedUIElement = getTouchedElement();
            }
            if (this instanceof UIElementGroup) {
                if (((UIElementGroup) this).isChildResponseTouch()) {
                    // 子element已经处理
                } else {
                    refreshTouchState(event);
                }
            } else {
                refreshTouchState(event);
            }
        }
        return status;
    }

    /**
     * 子类可以复写该方法获取Touch事件，event坐标是针对整个view的，如果你的element有嵌套那就需要获取相对坐标,
     * {@link #getRelativeX(float x)} {@link #getRelativeY(float y)}
     * 
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        return isClickable();
    }

    public float getRelativeX(float x) {
        return x - getElementLeft() - getElementInternalLeft();
    }

    public float getRelativeY(float y) {
        return y - getElementTop() - getElementInternalTop();
    }

    /**
     * 自己父group中的相对位置
     * 
     * @return
     */
    public float getElementInternalLeft() {
        if (mParent != null && mParent.getAllElement() != null && mParent.getAllElement().size() > 1) {
            return mParent.getElementInternalLeft(this);
        }
        return 0;
    }

    public float getElementInternalTop() {
        if (mParent != null && mParent.getAllElement() != null && mParent.getAllElement().size() > 1) {
            return mParent.getElementInternalTop(this);
        }
        return 0;
    }

    public interface OnClickListener {
        void onClick(UIElement uiElement);
    }

    private static class ListenerInfo {
        public OnClickListener mOnClickListener;
    }

    private ListenerInfo getListenerInfo() {
        if (mListenerInfo != null) {
            return mListenerInfo;
        }
        mListenerInfo = new ListenerInfo();
        return mListenerInfo;
    }

    public boolean callOnClick() {
        ListenerInfo li = mListenerInfo;
        if (li != null && li.mOnClickListener != null) {
            li.mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

    public void setOnClickListener(OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        setInterceptClick(true);
        getListenerInfo().mOnClickListener = l;
    }

    /**
     * 父Element
     */
    private UIElementGroup mParent;

    void setParent(UIElementGroup parent) {
        mParent = parent;
    }

    public boolean isChild() {
        return mParent != null;
    }

    public UIElementGroup getParent() {
        return mParent;
    }

    public int getSuggestedMinimumWidth() {
        return (mBackGround == null) ? getMeasuredWidth() : max(getMeasuredWidth(), mBackGround.getMinimumWidth());
    }

    public int getSuggestedMinimumHeight() {
        return (mBackGround == null) ? getMeasuredHeight() : max(getMeasuredHeight(), mBackGround.getMinimumHeight());
    }

    public Drawable getBackground() {
        return mBackGround;
    }

    // TODO
    public boolean performLongClick() {
        boolean handled = false;

        return handled;
    }

    // TODO
    public final int getScrollX() {
        return 0;
    }

    // TODO
    public final int getScrollY() {
        return 0;
    }

    protected void onDraw(Canvas canvas) {
    }

    protected void onLayout(int left, int top, int right, int bottom) {
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }

    /*
     * 设置setEnabled后的回调，可重写该方法
     */
    public void drawableStateChanged() {
        if (!isAttachedToWindow()) {
            return;
        }

        if (mBackGround != null && mBackGround.isStateful()) {
            mBackGround.setState(getDrawableState());
        }
    }
    
    // 把Drawable的State与Touch事件结合，处理点击事件背景变化
    // @author dalong6
    public int[] getDrawableState(){
    	int[] drawableState;
    	int viewStateIndex = 0;
    	
    	if(mIsOnTouch)
    		viewStateIndex |= VIEW_STATE_PRESSED;
    	else
    		viewStateIndex |= VIEW_STATE_ENABLED;
    	
    	drawableState = VIEW_STATE_SETS[viewStateIndex];
    	
		return drawableState;
    	
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }
        drawableStateChanged();
        invalidate();
        // 目前只取消了点击事件相应
        // 因为Element没有长按机制，故未实现View中的取消长按相应
        if (!enabled) {
            setClickable(false);
        }
    }

    public Object getTag() {
        return mTag;
    }

    public void setTag(final Object tag) {
        mTag = tag;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

	@Override
	public void invalidateDrawable(Drawable who) {
        if (!isAttachedToWindow()) {
            return;
        }

        if (mBackGround == who) {
            mContext.invalidate();
        } else {
            mContext.invalidateDrawable(who);
        }
		
	}

	@Override
	public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (!isAttachedToWindow()) {
            return;
        }
        mContext.scheduleDrawable(who, what, when);
	}

	@Override
	public void unscheduleDrawable(Drawable who, Runnable what) {
        if (!isAttachedToWindow()) {
            return;
        }
        mContext.unscheduleDrawable(who, what);
		
	}
    


}
