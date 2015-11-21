package com.shenrh.canvas;

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

import com.shenrh.canvas.UIContext;
import com.shenrh.canvas.UIElement;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;

/**
 * 仿ProgressBar的Element
 * 
 */
public class ProgressBarElement extends UIElement {

    private static final int MAX_LEVEL = 10000;
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;

    int mMinWidth;
    int mMaxWidth;
    int mMinHeight;
    int mMaxHeight;

    private int mProgress;
    private int mSecondaryProgress;
    private int mMax;

    private int mBehavior;
    private int mDuration;
    private boolean mIndeterminate;
    private boolean mOnlyIndeterminate;
    private Transformation mTransformation;
    private AlphaAnimation mAnimation;
    private boolean mHasAnimation;

    private Drawable mIndeterminateDrawable;
    private Drawable mProgressDrawable;
    private Drawable mCurrentDrawable;

    public ProgressBarElement(UIContext host) {
        this(host, null);

    }

    public ProgressBarElement(UIContext host, AttributeSet attrs) {
        super(host, attrs);
        initProgessBar();

    }

    private void initProgessBar() {
        mMax = 100;
        mProgress = 0;
        mSecondaryProgress = 0;
        mIndeterminate = false;
        mOnlyIndeterminate = false;
        mDuration = 4000;
        mBehavior = AlphaAnimation.RESTART;
        mMinWidth = 24;
        mMaxWidth = 48;
        mMinHeight = 24;
        mMaxHeight = 48;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTrack(canvas);
    }

    /**
     * Draws the progress bar track.
     */
    private void drawTrack(Canvas canvas) {
        final Drawable d = mCurrentDrawable;
        if (d != null) {
            // Translate canvas so a indeterminate circular progress bar with
            // padding
            // rotates properly in its animation
            final int saveCount = canvas.save();

            // 只设置从左至右的情况，不考虑
            canvas.translate(getPaddingLeft(), getPaddingTop());

            // final long time = getDrawingTime();
            // if (mHasAnimation) {
            // mAnimation.getTransformation(time, mTransformation);
            // final float scale = mTransformation.getAlpha();
            // try {
            // mInDrawing = true;
            // d.setLevel((int) (scale * MAX_LEVEL));
            // } finally {
            // mInDrawing = false;
            // }
            // postInvalidateOnAnimation();
            // }
            //
            // d.draw(canvas);
            // canvas.restoreToCount(saveCount);
            //
            // if (mShouldStartAnimationDrawable && d instanceof Animatable) {
            // ((Animatable) d).start();
            // mShouldStartAnimationDrawable = false;
            // }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onLayout(int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawableStateChanged() {
        // TODO Auto-generated method stub

    }

}
