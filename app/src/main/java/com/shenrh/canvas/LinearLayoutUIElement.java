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

import java.util.List;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

/**
 * 线性布局
 * 
 * @author shenrh
 * 
 */
public class LinearLayoutUIElement extends UIElementGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int mOrientation = VERTICAL;

    public LinearLayoutUIElement(UIContext host) {
        this(host, null);
    }

    public LinearLayoutUIElement(UIContext host, AttributeSet attrs) {
        super(host, attrs);
    }

    /**
     * TODO 目前只实现VERTICAL
     * 
     * @param orientation
     */
    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public int getElementTop(UIElement element) {
        if (mOrientation == VERTICAL) {
            for (UIElement e : getAllElement()) {
                if (element.equals(e)) {
                    return e.getTop();
                }
            }
        }
        return 0;
    }

    @Override
    public int getElementLeft(UIElement element) {
        if (mOrientation == HORIZONTAL) {
            for (UIElement e : getAllElement()) {
                if (element.equals(e)) {
                    return e.getLeft();
                }
            }
        }
        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        List<UIElement> mElements = getAllElement();
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthUsed = 0;
        int heightUsed = 0;
        for (UIElement element : mElements) {
            if (element.getVisibility() != View.VISIBLE) {
                continue;
            }
            measureElementWithMargins(element, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
            heightUsed += getMeasuredHeightWithMargins(element);
        }

        int heightSize = heightUsed + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(int left, int top, int right, int bottom) {
        List<UIElement> mElements = getAllElement();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        int currentTop = paddingTop;
        final int contentLeft = paddingLeft;
        final int contentWidth = right - left - contentLeft - getPaddingRight();

        for (UIElement element : mElements) {
            if (element.getVisibility() != View.VISIBLE) {
                continue;
            }
            layoutElement(element, contentLeft, currentTop, contentWidth, element.getMeasuredHeight());
            currentTop += element.getMeasuredHeight();
        }
    }
}
