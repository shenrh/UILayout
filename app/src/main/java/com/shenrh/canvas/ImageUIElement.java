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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView.ScaleType;

public class ImageUIElement extends UIElement {
    private static final String LOGTAG = "ImageUIElement";

    private Drawable mDrawable;
    private Bitmap mBitmap;
    private int mResourceId;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private final Matrix mMatrix;
    private Matrix mDrawMatrix;

    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();

    private ScaleType mScaleType;
    private int mLevel;
    
    Animation mCurrentAnimation = null;
    Transformation mTransformation = new Transformation();

    public ImageUIElement(UIContext host) {
        this(host, null);
    }

    public ImageUIElement(UIContext host, AttributeSet attrs) {
        super(host, attrs);
        mMatrix = new Matrix();
        mScaleType = ScaleType.FIT_CENTER;
    }

    private void configureBounds() {
        if (mDrawable == null) {
            return;
        }

        final int dwidth = mDrawableWidth;
        final int dheight = mDrawableHeight;

        final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

        boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

        if (dwidth <= 0 || dheight <= 0 || mScaleType == ScaleType.FIT_XY) {
            // If the drawable has no intrinsic size, or we're told to
            // scaletofit, then we just fill our entire view.
            mDrawable.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            mDrawable.setBounds(0, 0, dwidth, dheight);

            if (mScaleType == ScaleType.MATRIX) {
                // Use the specified matrix as-is.
                if (mMatrix.isIdentity()) {
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
                mDrawMatrix = null;
            } else if (mScaleType == ScaleType.CENTER) {
                // Center bitmap in view, no scaling.
                mDrawMatrix = mMatrix;
                mDrawMatrix.setTranslate((int) ((vwidth - dwidth) * 0.5f + 0.5f), (int) ((vheight - dheight) * 0.5f + 0.5f));
            } else if (mScaleType == ScaleType.CENTER_CROP) {
                mDrawMatrix = mMatrix;

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            } else if (mScaleType == ScaleType.CENTER_INSIDE) {
                mDrawMatrix = mMatrix;

                final float scale;
                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth, (float) vheight / (float) dheight);
                }

                float dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
                float dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else {
                // Generate the required transform.
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);

                mDrawMatrix = mMatrix;
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(mScaleType));
            }
        }
    }

    private static final Matrix.ScaleToFit[] sS2FArray = { Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER,
            Matrix.ScaleToFit.END };

    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        return sS2FArray[st.ordinal() - 1];
    }

    private void updateDrawable(Drawable d) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            mContext.unscheduleDrawable(mDrawable);
        }

        mDrawable = d;

        if (d != null) {
            d.setCallback(this);

            if (d.isStateful()) {
                d.setState(getDrawableState());
            }

            d.setLevel(mLevel);
            d.setVisible(getVisibility() == View.VISIBLE, true);

            mDrawableWidth = d.getIntrinsicWidth();
            mDrawableHeight = d.getIntrinsicHeight();

            configureBounds();
        } else {
            mDrawableWidth = mDrawableHeight = -1;
        }
    }

    private void resizeFromDrawable() {
        if (mDrawable == null) {
            return;
        }

        int width = mDrawable.getIntrinsicWidth();
        if (width < 0) {
            width = mDrawableWidth;
        }

        int height = mDrawable.getIntrinsicHeight();
        if (height < 0) {
            height = mDrawableHeight;
        }

        if (width != mDrawableWidth || height != mDrawableHeight) {
            mDrawableWidth = width;
            mDrawableHeight = height;

            requestLayout();
        }
    }

    private void resolveUri() {
        if (mDrawable != null) {
            return;
        }

        Drawable d = null;

        if (mResourceId != 0) {
            try {
                final Resources res = getResources();
                if (res == null) {
                    return;
                }

                d = res.getDrawable(mResourceId);
            } catch (Exception e) {
                UILog.d(LOGTAG, "Unable to find resource: " + mResourceId, e);
            }
        } else {
            return;
        }

        updateDrawable(d);
    }

    private void setDrawableVisible(boolean visible) {
        if (mDrawable != null) {
            mDrawable.setVisible(visible, false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable == null) {
            return;
        }
        // 加入动画实现
        // TODO 可正常运行，但无动画效果，需继续调试
        if(mCurrentAnimation != null)
        {
            long curTime = SystemClock.uptimeMillis();
            Paint mPaint = new Paint();
            drawableToBitmap(mDrawable);
            if(!mCurrentAnimation.isInitialized())
            	mCurrentAnimation.initialize(mDrawableWidth, mDrawableHeight, getParent().getWidth(),getParent().getHeight());
        	boolean more = mCurrentAnimation.getTransformation(curTime, mTransformation);
        	if(more){
//        		Matrix m = canvas.getMatrix();
        		canvas.setMatrix(mTransformation.getMatrix());
        		canvas.drawBitmap(mBitmap, getLeft(), getTop(), mPaint);
//        		canvas.setMatrix(m);
        		invalidate();
        	}
        	else{
        		mCurrentAnimation = null;
        	    invalidate();
        	}
        }

        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return;
        }

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        if (mDrawMatrix == null && paddingLeft == 0 && paddingTop == 0) {
            mDrawable.draw(canvas);
        } else {
            final int saveCount = canvas.getSaveCount();
            canvas.save();

            canvas.translate(paddingLeft, paddingTop);

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
        
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        resolveUri();

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            if (mDrawable == null) {
                width = 0;
                mDrawableWidth = -1;
            } else {
                width = Math.max(1, mDrawableWidth) + getPaddingLeft() + getPaddingRight();
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            if (mDrawable == null) {
                height = 0;
                mDrawableHeight = -1;
            } else {
                height = Math.max(1, mDrawableHeight) + getPaddingTop() + getPaddingBottom();
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(heightSize, height);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(int left, int top, int right, int bottom) {
        configureBounds();
    }

    @Override
    public void drawableStateChanged() {
        if (!isAttachedToWindow()) {
            return;
        }

        if (mDrawable != null && mDrawable.isStateful()) {
            mDrawable.setState(getDrawableState());
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        setDrawableVisible(visibility == View.VISIBLE);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if (!isAttachedToWindow()) {
            return;
        }

        if (mDrawable == who) {
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

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setDrawableVisible(getVisibility() == View.VISIBLE);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setDrawableVisible(false);
    }

    public void setImageLevel(int level) {
        mLevel = level;

        if (mDrawable != null) {
            mDrawable.setLevel(level);
            resizeFromDrawable();
        }
    }

    public void setImageResource(int resourceId) {
        if (mResourceId == resourceId) {
            return;
        }

        updateDrawable(null);
        mResourceId = resourceId;

        final int oldWidth = mDrawableWidth;
        final int oldHeight = mDrawableHeight;

        resolveUri();

        if (oldWidth != mDrawableWidth || oldHeight != mDrawableHeight) {
            requestLayout();
        }
        invalidate();
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setImageDrawable(Drawable drawable) {
        if (mDrawable == drawable) {
            return;
        }

        mResourceId = 0;

        final int oldWidth = mDrawableWidth;
        final int oldHeight = mDrawableHeight;

        updateDrawable(drawable);

        if (oldWidth != mDrawableWidth || oldHeight != mDrawableHeight) {
            requestLayout();
        }
        invalidate();
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (!isAttachedToWindow()) {
            return;
        }
        setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }

        if (mScaleType == scaleType) {
            return;
        }

        mScaleType = scaleType;

        requestLayout();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isClickable();
    }
    
    private void setAnimation(Animation animation) {
    	mCurrentAnimation = animation;
    	if (animation != null) {
    	animation.reset();
        }
    }
    
    public void startAnimation(Animation animation) {
    	animation.setStartTime(animation.START_ON_FIRST_FRAME);
    	setAnimation(animation);
    	invalidate();
    }
    
    private void drawableToBitmap(Drawable drawable)
    {
       BitmapDrawable bd = (BitmapDrawable) drawable;
       mBitmap = bd.getBitmap();
    }
    
    
}