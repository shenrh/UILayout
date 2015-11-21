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

import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.BoringLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Scroller;
import android.widget.TextView.BufferType;

public class TextUIElement extends UIElement {
    private static final String LOGTAG = "TextUIElement";

    private CharSequence mText;

    private ColorStateList mTextColor;
    private int mCurTextColor;

    private int mMaxLines = Integer.MAX_VALUE;
    private int mOldMaxLines = Integer.MAX_VALUE;

    private float mLineSpacingMult = 1.0f;
    private float mLineSpacingAdd = 0.0f;
    private boolean mIncludeFontPadding = true;
    private Layout.Alignment mLayoutAlignment = Layout.Alignment.ALIGN_NORMAL;

    private Layout mLayout;
    private BoringLayout mSavedLayout;

    private final TextPaint mTextPaint;
    private TextUtils.TruncateAt mEllipsize;
    private BoringLayout.Metrics mBoring;

    private int mGravity = Gravity.TOP | Gravity.START;
    private BufferType mBufferType = BufferType.NORMAL;
    
    private Editor mEditor;
    private Editable.Factory mEditableFactory = Editable.Factory.getInstance();
    private Spannable.Factory mSpannableFactory = Spannable.Factory.getInstance();
    private static final InputFilter[] NO_FILTERS = new InputFilter[0];
    private static final Spanned EMPTY_SPANNED = new SpannedString("");
    private InputFilter[] mFilters = NO_FILTERS;

    int mHighlightColor = 0x6633B5E5;
    // private Path mHighlightPath;
    // private final Paint mHighlightPaint;
    // private boolean mHighlightPathBogus = true;

    private Scroller mScroller;

    private static final BoringLayout.Metrics UNKNOWN_BORING = new BoringLayout.Metrics();

    class Drawables {
        final Rect mCompoundRect = new Rect();
        Drawable mDrawableTop, mDrawableBottom, mDrawableLeft, mDrawableRight, mDrawableStart, mDrawableEnd;
        int mDrawableSizeTop, mDrawableSizeBottom, mDrawableSizeLeft, mDrawableSizeRight, mDrawableSizeStart, mDrawableSizeEnd;
        int mDrawableWidthTop, mDrawableWidthBottom, mDrawableHeightLeft, mDrawableHeightRight, mDrawableHeightStart, mDrawableHeightEnd;
        int mDrawablePadding;
    }

    private Drawables mDrawables;

    public TextUIElement(UIContext host) {
        this(host, null);
    }

    public TextUIElement(UIContext host, AttributeSet attrs) {
        super(host, attrs);

        mText = "";
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = getResources().getDisplayMetrics().density;

        setTextColor(ColorStateList.valueOf(0xFF000000));
    }

    private int getDesiredWidth() {
        final int lineCount = mLayout.getLineCount();
        final CharSequence text = mLayout.getText();

        // If any line was wrapped, we can't use it. but it's
        // ok for the last line not to have a newline.
        for (int i = 0; i < lineCount - 1; i++) {
            if (text.charAt(mLayout.getLineEnd(i) - 1) != '\n') {
                return -1;
            }
        }

        float maxWidth = 0;
        for (int i = 0; i < lineCount; i++) {
            maxWidth = Math.max(maxWidth, mLayout.getLineWidth(i));
        }

        return (int) FloatMath.ceil(maxWidth);
    }

    private int getDesiredHeight() {
        if (mLayout == null) {
            return 0;
        }

        final int padding = getPaddingTop() + getPaddingBottom();
        final int refLine = Math.min(mMaxLines, mLayout.getLineCount());

        return mLayout.getLineTop(refLine) + padding;
    }

    private void makeNewLayout(int wantWidth, BoringLayout.Metrics boring, int ellipsisWidth, boolean bringIntoView) {
        if (wantWidth < 0) {
            wantWidth = 0;
        }

        mOldMaxLines = mMaxLines;
        boolean shouldEllipsize = (mEllipsize != null);

        mLayout = makeSingleLayout(wantWidth, boring, ellipsisWidth, mLayoutAlignment, shouldEllipsize, mEllipsize, bringIntoView);
    }

    private Layout makeSingleLayout(int wantWidth, BoringLayout.Metrics boring, int ellipsisWidth, Layout.Alignment alignment,
            boolean shouldEllipsize, TruncateAt effectiveEllipsize, boolean useSaved) {
        Layout result;

        if (boring == UNKNOWN_BORING) {
            boring = BoringLayout.isBoring(mText, mTextPaint, mBoring);
            if (boring != null) {
                mBoring = boring;
            }
        }

        if (boring != null) {
            // Layout is smaller than target width, no ellipsize defined.
            if (boring.width <= wantWidth && (effectiveEllipsize == null || boring.width <= ellipsisWidth)) {
                if (mSavedLayout != null) {
                    result = mSavedLayout.replaceOrMake(mText, mTextPaint, wantWidth, alignment, mLineSpacingMult, mLineSpacingAdd, boring,
                            mIncludeFontPadding);
                } else {
                    result = BoringLayout.make(mText, mTextPaint, wantWidth, alignment, mLineSpacingMult, mLineSpacingAdd, boring,
                            mIncludeFontPadding);
                }

                if (useSaved) {
                    mSavedLayout = (BoringLayout) result;
                }

                // Layout is smaller than target width, ellipsize is not
                // necessary.
            } else if (shouldEllipsize && boring.width <= wantWidth) {
                if (useSaved && mSavedLayout != null) {
                    result = mSavedLayout.replaceOrMake(mText, mTextPaint, wantWidth, alignment, mLineSpacingMult, mLineSpacingAdd, boring,
                            mIncludeFontPadding, effectiveEllipsize, ellipsisWidth);
                } else {
                    result = BoringLayout.make(mText, mTextPaint, wantWidth, alignment, mLineSpacingMult, mLineSpacingAdd, boring,
                            mIncludeFontPadding, effectiveEllipsize, ellipsisWidth);
                }

                // Should ellipsize, layout is bigger than target width.
            } else if (shouldEllipsize) {
                result = StaticLayoutWithMaxLines.create(mText, 0, mText.length(), mTextPaint, wantWidth, alignment, mLineSpacingMult,
                        mLineSpacingAdd, mIncludeFontPadding, effectiveEllipsize, ellipsisWidth, mMaxLines);

                // No ellipsize, just truncate text.
            } else {
                result = new StaticLayout(mText, mTextPaint, wantWidth, alignment, mLineSpacingMult, mLineSpacingAdd, mIncludeFontPadding);
            }

            // Layout is not Boring and should ellipsize.
        } else if (shouldEllipsize) {
            result = StaticLayoutWithMaxLines.create(mText, 0, mText.length(), mTextPaint, wantWidth, alignment, mLineSpacingMult,
                    mLineSpacingAdd, mIncludeFontPadding, effectiveEllipsize, ellipsisWidth, mMaxLines);

            // Layout is not boring and should not ellipsize
        } else {
            result = new StaticLayout(mText, mTextPaint, wantWidth, alignment, mLineSpacingMult, mLineSpacingAdd, mIncludeFontPadding);
        }

        return result;
    }

    private void nullLayouts() {
        if (mLayout instanceof BoringLayout && mSavedLayout == null) {
            mSavedLayout = (BoringLayout) mLayout;
        }

        mLayout = null;
    }

    private void checkForRelayout() {
        if (mLayout == null) {
            return;
        }

        final LayoutParams lp = getLayoutParams();

        // If we have a fixed width, we can just swap in a new text layout
        // if the text height stays the same or if the view height is fixed.
        if (lp.width != LayoutParams.WRAP_CONTENT) {
            // Static width, so try making a new text layout.

            final int oldHeight = mLayout.getHeight();
            final int oldWidth = mLayout.getWidth();

            // No need to bring the text into view, since the size is not
            // changing (unless we do the requestLayout(), in which case it
            // will happen when measuring).
            makeNewLayout(oldWidth, UNKNOWN_BORING, oldWidth, false);

            // In a fixed-height view, so use our new text layout.
            if (lp.height != ViewGroup.LayoutParams.WRAP_CONTENT && lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                invalidate();
                return;
            }

            // Dynamic height, but height has stayed the same,
            // so use our new text layout.
            if (mLayout.getHeight() == oldHeight) {
                invalidate();
                return;
            }

            // We lose: the height has changed and we have a dynamic height.
            // Request a new view layout using our new text layout.
            requestLayout();
            invalidate();
        } else {
            // Dynamic width, so we have no choice but to request a new
            // view layout with a new text layout.
            recreateLayout();
        }
    }

    private void recreateLayout() {
        if (mLayout == null) {
            return;
        }

        nullLayouts();
        requestLayout();
        invalidate();
    }

    private void updateTextColors() {
        int color = mTextColor.getColorForState(mContext.getDrawableState(), 0);
        if (color != mCurTextColor) {
            mCurTextColor = color;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLayout == null) {
            return;
        }

        final int saveCount = canvas.getSaveCount();
        canvas.save();

        mTextPaint.setColor(mCurTextColor);

        float clipLeft = getPaddingLeft();
        float clipTop = getPaddingTop();
        float clipRight = getRight() - getPaddingRight();
        float clipBottom = getBottom() - getPaddingBottom();
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);

        // TODO
        int xoff = 0;
        int yoff = 0;
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(mText.toString(), 0, mText.length(), bounds);
        if ((mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
            xoff = (int) ((clipRight - clipLeft - bounds.width()) / 2);
        }

        if ((mGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.CENTER_VERTICAL) {
            yoff = (int) ((clipBottom - clipTop - bounds.height()) / 2);
        }
        canvas.translate(getPaddingLeft() + xoff, getPaddingTop() + yoff);
        mLayout.draw(canvas);

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();

        int width;
        int height;

        BoringLayout.Metrics boring = UNKNOWN_BORING;

        int desiredWidth = -1;
        boolean fromExisting = false;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            if (mLayout != null && mEllipsize == null) {
                desiredWidth = getDesiredWidth();
            }

            if (desiredWidth < 0) {
                boring = BoringLayout.isBoring(mText, mTextPaint, mBoring);
                if (boring != null) {
                    mBoring = boring;
                }
            } else {
                fromExisting = true;
            }

            if (boring == null || boring == UNKNOWN_BORING) {
                if (desiredWidth < 0) {
                    desiredWidth = (int) FloatMath.ceil(Layout.getDesiredWidth(mText, mTextPaint));
                }

                width = desiredWidth;
            } else {
                width = boring.width;
            }

            width += paddingLeft + paddingRight;

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        int unpaddedWidth = width - paddingLeft - paddingRight;

        if (mLayout == null) {
            makeNewLayout(unpaddedWidth, boring, unpaddedWidth, false);
        } else {
            final boolean layoutChanged = (mLayout.getWidth() != unpaddedWidth) || (mLayout.getEllipsizedWidth() != unpaddedWidth);

            final boolean widthChanged = (mEllipsize == null) && (unpaddedWidth > mLayout.getWidth())
                    && (mLayout instanceof BoringLayout || (fromExisting && desiredWidth >= 0 && desiredWidth <= unpaddedWidth));

            final boolean maxChanged = (mMaxLines != mOldMaxLines);

            if (layoutChanged || maxChanged) {
                if (!maxChanged && widthChanged) {
                    mLayout.increaseWidthTo(unpaddedWidth);
                } else {
                    makeNewLayout(unpaddedWidth, boring, unpaddedWidth, false);
                }
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight();

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(heightSize, height);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void drawableStateChanged() {
        if (mTextColor != null && mTextColor.isStateful()) {
            updateTextColors();
        }
    }

    public void setRawTextSize(float size) {
        if (mTextPaint.getTextSize() == size) {
            return;
        }

        mTextPaint.setTextSize(size);
        recreateLayout();
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    public void setTextSize(int unit, float size) {
        final Resources res = getResources();

        final float textSize = TypedValue.applyDimension(unit, size, res.getDisplayMetrics());
        if (mTextPaint.getTextSize() == textSize) {
            return;
        }

        mTextPaint.setTextSize(textSize);
        recreateLayout();
    }

    public void setTextAlignment(Layout.Alignment alignment) {
        if (mLayoutAlignment == alignment) {
            return;
        }

        mLayoutAlignment = alignment;
        recreateLayout();
    }

    public void setTextColor(int color) {
        mTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

    public void setTextColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }

        mTextColor = colors;
        updateTextColors();
    }

    public void setEllipsize(TruncateAt ellipsize) {
        if (mEllipsize == ellipsize) {
            return;
        }

        mEllipsize = ellipsize;
        recreateLayout();
    }

    public void setSingleLine() {
        // 凑合用了
        setMaxLines(1);
    }

    public void setMaxLines(int maxLines) {
        if (mMaxLines == maxLines) {
            return;
        }

        mMaxLines = maxLines;

        requestLayout();
        invalidate();
    }

    public int getMaxLines() {
        return mMaxLines;
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        mTextPaint.setShadowLayer(radius, dx, dy, color);

        // TODO
        // mShadowRadius = radius;
        // mShadowDx = dx;
        // mShadowDy = dy;

        invalidate();
    }
    
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        recreateLayout();
    }

    public CharSequence getText() {
        return mText;
    }

    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            text = "";
        }

        if (TextUtils.equals(mText, text)) {
            return;
        }
        
        //BufferType部分
//        if (type == BufferType.EDITABLE || getKeyListener() != null) {
//            if (mEditor == null){
//            	mEditor = new Editor(getContext());
//            }
//            Editable t = mEditableFactory.newEditable(text);
//            text = t;
//            setFilters(t, mFilters);
//            InputMethodManager imm = InputMethodManager.peekInstance();
//            if (imm != null) imm.restartInput(this);
//        } else if (type == BufferType.SPANNABLE || mMovement != null) {
//            text = mSpannableFactory.newSpannable(text);
//        } else if (!(text instanceof CharWrapper)) {
//            text = TextUtils.stringOrSpannedString(text);
//        }

        mText = text;
        mBufferType = type;// TODO
        checkForRelayout();
    }
    
//    public final KeyListener getKeyListener() {
//    	 return mEditor == null ? null : mEditor.mKeyListener;
//    }

    public void setText(CharSequence text) {
        setText(text, mBufferType);
    }

    public TextPaint getPaint() {
        return mTextPaint;
    }

    public final Layout getLayout() {
        return mLayout;
    }

    public int getLineCount() {
        return mLayout != null ? mLayout.getLineCount() : 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isClickable();
    }

    /**
     * TODO
     * 
     * @param gravity
     */
    public void setGravity(int gravity) {
        if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.START;
        }
        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.TOP;
        }

        // boolean newLayout = false;
        //
        // if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) !=
        // (mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)) {
        // newLayout = true;
        // }

        if (gravity != mGravity) {
            invalidate();
            // mLayoutAlignment = null;
        }

        mGravity = gravity;
    }

    public int getGravity() {
        return mGravity;
    }

    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        final Resources resources = getContext().getResources();
        setCompoundDrawablesWithIntrinsicBounds(left != 0 ? resources.getDrawable(left) : null, top != 0 ? resources.getDrawable(top)
                : null, right != 0 ? resources.getDrawable(right) : null, bottom != 0 ? resources.getDrawable(bottom) : null);
    }

    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {

        if (left != null) {
            left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());
        }
        if (right != null) {
            right.setBounds(0, 0, right.getIntrinsicWidth(), right.getIntrinsicHeight());
        }
        if (top != null) {
            top.setBounds(0, 0, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        }
        if (bottom != null) {
            bottom.setBounds(0, 0, bottom.getIntrinsicWidth(), bottom.getIntrinsicHeight());
        }
        setCompoundDrawables(left, top, right, bottom);
    }

    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        Drawables dr = mDrawables;

        final boolean drawables = left != null || top != null || right != null || bottom != null;

        if (!drawables) {
            // Clearing drawables... can we free the data structure?
            if (dr != null) {
                if (dr.mDrawablePadding == 0) {
                    mDrawables = null;
                } else {
                    // We need to retain the last set padding, so just clear
                    // out all of the fields in the existing structure.
                    if (dr.mDrawableLeft != null)
                        dr.mDrawableLeft.setCallback(null);
                    dr.mDrawableLeft = null;
                    if (dr.mDrawableTop != null)
                        dr.mDrawableTop.setCallback(null);
                    dr.mDrawableTop = null;
                    if (dr.mDrawableRight != null)
                        dr.mDrawableRight.setCallback(null);
                    dr.mDrawableRight = null;
                    if (dr.mDrawableBottom != null)
                        dr.mDrawableBottom.setCallback(null);
                    dr.mDrawableBottom = null;
                    dr.mDrawableSizeLeft = dr.mDrawableHeightLeft = 0;
                    dr.mDrawableSizeRight = dr.mDrawableHeightRight = 0;
                    dr.mDrawableSizeTop = dr.mDrawableWidthTop = 0;
                    dr.mDrawableSizeBottom = dr.mDrawableWidthBottom = 0;
                }
            }
        } else {
            if (dr == null) {
                mDrawables = dr = new Drawables();
            }

            if (dr.mDrawableLeft != left && dr.mDrawableLeft != null) {
                dr.mDrawableLeft.setCallback(null);
            }
            dr.mDrawableLeft = left;

            if (dr.mDrawableTop != top && dr.mDrawableTop != null) {
                dr.mDrawableTop.setCallback(null);
            }
            dr.mDrawableTop = top;

            if (dr.mDrawableRight != right && dr.mDrawableRight != null) {
                dr.mDrawableRight.setCallback(null);
            }
            dr.mDrawableRight = right;

            if (dr.mDrawableBottom != bottom && dr.mDrawableBottom != null) {
                dr.mDrawableBottom.setCallback(null);
            }
            dr.mDrawableBottom = bottom;

            final Rect compoundRect = dr.mCompoundRect;
            // int[] state;// TODO
            // state = getDrawableState();

            if (left != null) {
                // left.setState(state);
                left.copyBounds(compoundRect);
                // left.setCallback(this);
                dr.mDrawableSizeLeft = compoundRect.width();
                dr.mDrawableHeightLeft = compoundRect.height();
            } else {
                dr.mDrawableSizeLeft = dr.mDrawableHeightLeft = 0;
            }

            if (right != null) {
                // right.setState(state);
                right.copyBounds(compoundRect);
                // right.setCallback(this);
                dr.mDrawableSizeRight = compoundRect.width();
                dr.mDrawableHeightRight = compoundRect.height();
            } else {
                dr.mDrawableSizeRight = dr.mDrawableHeightRight = 0;
            }

            if (top != null) {
                // top.setState(state);
                top.copyBounds(compoundRect);
                // top.setCallback(this);
                dr.mDrawableSizeTop = compoundRect.height();
                dr.mDrawableWidthTop = compoundRect.width();
            } else {
                dr.mDrawableSizeTop = dr.mDrawableWidthTop = 0;
            }

            if (bottom != null) {
                // bottom.setState(state);
                bottom.copyBounds(compoundRect);
                // bottom.setCallback(this);
                dr.mDrawableSizeBottom = compoundRect.height();
                dr.mDrawableWidthBottom = compoundRect.width();
            } else {
                dr.mDrawableSizeBottom = dr.mDrawableWidthBottom = 0;
            }
        }

        invalidate();
        requestLayout();
    }

    public void setCompoundDrawablePadding(int pad) {
        Drawables dr = mDrawables;
        if (pad == 0) {
            if (dr != null) {
                dr.mDrawablePadding = pad;
            }
        } else {
            if (dr == null) {
                mDrawables = dr = new Drawables();
            }
            dr.mDrawablePadding = pad;
        }

        invalidate();
        requestLayout();
    }

    public void setHighlightColor(int color) {// TODO
        if (mHighlightColor != color) {
            mHighlightColor = color;
            invalidate();
        }
    }

    public int getHighlightColor() {
        return mHighlightColor;
    }

    public void setScroller(Scroller s) {// TODO
        mScroller = s;
    }

    @Override
    public boolean performLongClick() {
        boolean handled = false;
        //
        // if (super.performLongClick()) {
        // handled = true;
        // }
        //
        // if (mEditor != null) {
        // handled |= mEditor.performLongClick(handled);
        // }
        //
        // if (handled) {
        // performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        // if (mEditor != null) mEditor.mDiscardNextActionUp = true;
        // }

        return handled;
    }

    /**
     * Returns the total left padding of the view, including the left Drawable
     * if any.
     */
    public int getTotalPaddingLeft() {
        return getCompoundPaddingLeft();
    }

    /**
     * Returns the total right padding of the view, including the right Drawable
     * if any.
     */
    public int getTotalPaddingRight() {
        return getCompoundPaddingRight();
    }

    /**
     * Returns the total top padding of the view, including the top Drawable if
     * any, the extra space to keep more than maxLines from showing, and the
     * vertical offset for gravity, if any.
     */
    public int getTotalPaddingTop() {
        // return getExtendedPaddingTop() + getVerticalOffset(true);
        return getCompoundPaddingTop();
    }

    /**
     * Returns the total bottom padding of the view, including the bottom
     * Drawable if any, the extra space to keep more than maxLines from showing,
     * and the vertical offset for gravity, if any.
     */
    public int getTotalPaddingBottom() {
        // return getExtendedPaddingBottom() + getBottomVerticalOffset(true);
        return getCompoundPaddingBottom();
    }

    /**
     * Returns the top padding of the view, plus space for the top Drawable if
     * any.
     */
    public int getCompoundPaddingTop() {
        final Drawables dr = mDrawables;
        if (dr == null || dr.mDrawableTop == null) {
            return getPaddingTop();// TODO mPaddingTop
        } else {
            return getPaddingTop() + dr.mDrawablePadding + dr.mDrawableSizeTop;
        }
    }

    /**
     * Returns the bottom padding of the view, plus space for the bottom
     * Drawable if any.
     */
    public int getCompoundPaddingBottom() {
        final Drawables dr = mDrawables;
        if (dr == null || dr.mDrawableBottom == null) {
            return getPaddingBottom();
        } else {
            return getPaddingBottom() + dr.mDrawablePadding + dr.mDrawableSizeBottom;
        }
    }

    /**
     * Returns the left padding of the view, plus space for the left Drawable if
     * any.
     */
    public int getCompoundPaddingLeft() {
        final Drawables dr = mDrawables;
        if (dr == null || dr.mDrawableLeft == null) {
            return getPaddingLeft();
        } else {
            return getPaddingLeft() + dr.mDrawablePadding + dr.mDrawableSizeLeft;
        }
    }

    /**
     * Returns the right padding of the view, plus space for the right Drawable
     * if any.
     */
    public int getCompoundPaddingRight() {
        final Drawables dr = mDrawables;
        if (dr == null || dr.mDrawableRight == null) {
            return getPaddingRight();
        } else {
            return getPaddingRight() + dr.mDrawablePadding + dr.mDrawableSizeRight;
        }
    }

    // TODO
    public void setLineSpacing(float add, float mult) {

    }
}