/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2014 Lucas Rocha
 *
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

import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import java.util.ArrayList;
import java.util.List;

public abstract class UIElementGroup extends UIElement {
    private final List<UIElement> mElements;
    private boolean mIsChildResponseTouch = false;
    private UIElement mTouchedUIElement;

    public UIElementGroup(UIContext host) {
        this(host, null);
    }

    public UIElementGroup(UIContext host, AttributeSet attrs) {
        super(host, attrs);
        mElements = new ArrayList<UIElement>();
    }

    @Override
    public boolean setContext(UIContext host) {
        boolean changed = super.setContext(host);

        if (mElements != null) {
            for (UIElement element : mElements) {
                element.setContext(host);
            }
        }

        return changed;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mElements != null) {
            for (UIElement element : mElements) {
                if (element instanceof UIElement) {
                    ((UIElement) element).onAttachedToWindow();
                }
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        for (UIElement element : mElements) {
            if (element instanceof UIElement) {
                ((UIElement) element).onDetachedFromWindow();
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();

        for (UIElement element : mElements) {
            if (element.getVisibility() == View.VISIBLE) {
                element.draw(canvas);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void drawableStateChanged() {
        for (UIElement element : mElements) {
            element.drawableStateChanged();
        }
    }

    /**
     * 自动补充一个MarginLayoutParams
     * 
     * @param element
     */
    public void addElement(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp == null) {
            lp = generateDefaultLayoutParams();
        }

        addElement(element, lp);
    }

    public void addElement(UIElement element, LayoutParams lp) {
        if (!checkLayoutParams(lp)) {
            lp = generateLayoutParams(lp);
        }
        element.setParent(this);// 设置父控件
        element.setLayoutParams(lp);
        mElements.add(element);
        requestLayout();
    }

    public void removeElement(UIElement element) {
        mElements.remove(element);
        requestLayout();
    }

    public void removeAllElements() {
        List<UIElement> elements = new ArrayList<UIElement>(mElements);// Collections.copy(dest,src);
        for (UIElement element : elements) {
            removeElement(element);
        }
    }

    public UIElement findElementById(int id) {
        for (UIElement element : mElements) {
            if (element.getId() == id) {
                return element;
            }
        }

        return null;
    }

    protected boolean checkLayoutParams(LayoutParams lp) {
        return (lp != null && lp instanceof MarginLayoutParams);
    }

    protected LayoutParams generateLayoutParams(LayoutParams lp) {
        if (lp == null) {
            return generateDefaultLayoutParams();
        }

        return new MarginLayoutParams(lp.width, lp.height);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    protected void measureElementWithMargins(UIElement element, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
            int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) element.getLayoutParams();

        if (lp == null) {
            throw new RuntimeException("LayoutParams is null! element:" + element);
        }
        final int childWidthMeasureSpec = getElementMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight()
                + lp.leftMargin + lp.rightMargin + widthUsed, lp.width);

        final int childHeightMeasureSpec = getElementMeasureSpec(parentHeightMeasureSpec, getPaddingTop() + getPaddingBottom()
                + lp.topMargin + lp.bottomMargin + heightUsed, lp.height);

        element.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    protected void measureElement(UIElement element, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final LayoutParams lp = element.getLayoutParams();

        if (lp == null) {
            throw new RuntimeException("LayoutParams is null! element:" + element);
        }
        final int childWidthMeasureSpec = getElementMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight() + widthUsed,
                lp.width);

        final int childHeightMeasureSpec = getElementMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + heightUsed, lp.height);

        element.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    protected static int getElementMeasureSpec(int spec, int padding, int childDimension) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);

        int size = Math.max(0, specSize - padding);

        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
        // Parent has imposed an exact size on us
        case MeasureSpec.EXACTLY:
            if (childDimension >= 0) {
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // Child wants to be our size. So be it.
                resultSize = size;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // Child wants to determine its own size. It can't be
                // bigger than us.
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            }
            break;

        // Parent has imposed a maximum size on us
        case MeasureSpec.AT_MOST:
            if (childDimension >= 0) {
                // Child wants a specific size... so be it
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // Child wants to be our size, but our size is not fixed.
                // Constrain child to not be bigger than us.
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // Child wants to determine its own size. It can't be
                // bigger than us.
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            }
            break;

        // Parent asked to see how big we want to be
        case MeasureSpec.UNSPECIFIED:
            if (childDimension >= 0) {
                // Child wants a specific size... let him have it
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // Child wants to be our size... find out how big it should
                // be
                resultSize = 0;
                resultMode = MeasureSpec.UNSPECIFIED;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // Child wants to determine its own size.... find out how
                // big it should be
                resultSize = 0;
                resultMode = View.MeasureSpec.UNSPECIFIED;
            }
            break;
        }

        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    /**
     * 是否有子Element响应了onTouchEvent
     * 
     * @return
     */
    public boolean isChildResponseTouch() {
        return mIsChildResponseTouch;
    }

    public List<UIElement> getAllElement() {
        return mElements;
    }

    /**
     * 改element在group中的起始位置
     * 
     * @param element
     * @return
     */
    public int getElementTop(UIElement element) {
        int top = 0;
        if (getParent() != null) {
            top = getParent().getElementTop(this);
        } else {
            top = element.getTop();
        }

        return top;
    }

    /**
     * 改element在group中的起始位置
     * 
     * @param element
     * @return
     */
    public int getElementLeft(UIElement element) {
        int left = 0;
        if (getParent() != null) {
            left = getParent().getElementLeft(this);
        } else {
            left = element.getLeft();
        }
        return left;
    }

    /**
     * group中包含之前自定义view改造的element时，原view有用onTouchEvent的坐标则需要实现该方法，
     * 自定义view在group中的相对位置
     * 
     * @param element
     * @return
     */
    public float getElementInternalLeft(UIElement element) {
        return 0;
    }

    /**
     * group中包含之前自定义view改造的element时，原view有用onTouchEvent的坐标则需要实现该方法，
     * 自定义view在group中的相对位置
     * 
     * @param element
     * @return
     */
    public float getElementInternalTop(UIElement element) {
        return 0;
    }

    final UIElement getElementByPoint(int x, int y) {
        for (UIElement element : mElements) {
            if (element.getVisibility() == View.VISIBLE) {
                if (element.isTouchOnElement(x, y)) {
                    return element;
                }
            }
        }
        return this;
    }

    /* package */final boolean internalOnclick(int x, int y) {
        for (UIElement element : mElements) {
            if (element.getVisibility() == View.VISIBLE) {
                if (element.isTouchOnElement(x, y)) {
                    if (element.isInterceptClick()) {
                        return element.callOnClick();
                    }
                }
            }
        }
        // 子控件没有响应父控件直接响应
        return callOnClick();
    }

    /* package */final boolean internalTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        for (UIElement element : mElements) {
            if (element.getVisibility() == View.VISIBLE) {
                if (element.isTouchOnElement(x, y)) {
                    mTouchedUIElement = element;
                    if (element.touchEvent(event)) {
                        mIsChildResponseTouch = true;
                        return true;
                    }
                }
            }
        }
        mTouchedUIElement = this;
        mIsChildResponseTouch = false;
        return onTouchEvent(event);
    }

    protected final UIElement getTouchedElement() {
        return mTouchedUIElement;
    }
    
    @Override
    protected void clearTouchState(){
    	for (UIElement element : mElements){
            element.clearTouchState();
    	}
    		
    }

    
    public static int getMeasuredWidthWithMargins(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams margins = (MarginLayoutParams) lp;
            return element.getMeasuredWidth() + margins.leftMargin + margins.rightMargin;
        } else {
            return element.getMeasuredWidth();
        }
    }

    public static int getSuggestedMinimumWidthWithMargins(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams margins = (MarginLayoutParams) lp;
            return element.getSuggestedMinimumWidth() + margins.leftMargin + margins.rightMargin;
        } else {
            return element.getSuggestedMinimumWidth();
        }
    }

    public static int getMeasuredHeightWithMargins(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams margins = (MarginLayoutParams) lp;
            return element.getMeasuredHeight() + margins.topMargin + margins.bottomMargin;
        } else {
            return element.getMeasuredHeight();
        }
    }

    public static int getSuggestedMinimumHeightWithMargins(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams margins = (MarginLayoutParams) lp;
            return element.getSuggestedMinimumHeight() + margins.topMargin + margins.bottomMargin;
        } else {
            return element.getSuggestedMinimumHeight();
        }
    }

    public static int getWidthWithMargins(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams margins = (MarginLayoutParams) lp;
            return element.getWidth() + margins.leftMargin + margins.rightMargin;
        } else {
            return element.getWidth();
        }
    }

    public static int getHeightWithMargins(UIElement element) {
        LayoutParams lp = element.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams margins = (MarginLayoutParams) lp;
            return element.getHeight() + margins.topMargin + margins.bottomMargin;
        } else {
            return element.getHeight();
        }
    }

    public static void layoutElement(UIElement element, int left, int top, int width, int height) {
        LayoutParams lp = element.getLayoutParams();
        int leftWithMargins = left;
        int topWithMargins = top;
        if (lp instanceof MarginLayoutParams) {
            MarginLayoutParams margins = (MarginLayoutParams) lp;
            leftWithMargins += margins.leftMargin;
            topWithMargins += margins.topMargin;
        }
        element.layout(leftWithMargins, topWithMargins, leftWithMargins + width, topWithMargins + height);
    }
}
