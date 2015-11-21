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
package com.shenrh.layout.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

import com.shenrh.canvas.ImageUIElement;
import com.shenrh.canvas.TextUIElement;
import com.shenrh.canvas.UIContext;
import com.shenrh.canvas.UIElement;
import com.shenrh.canvas.UIElementGroup;
import com.shenrh.layout.R;

public class CustomElement extends UIElementGroup {
    private ImageUIElement mProfileImage;
    private TextUIElement mAuthorText;
    private TextUIElement mMessageText;
    private ImageUIElement mPostImage;

    public CustomElement(UIContext host) {
        this(host, null);
    }

    public CustomElement(UIContext host, AttributeSet attrs) {
        super(host, attrs);

        final Resources res = getResources();

        int padding = res.getDimensionPixelOffset(R.dimen.tweet_padding);
        setPadding(padding, padding, padding, padding);

        mAuthorText = new TextUIElement(host);
        mAuthorText.setTextColor(getResources().getColor(R.color.tweet_author_text_color));
        mAuthorText.setTextSize(getResources().getDimensionPixelOffset(R.dimen.tweet_author_text_size));
        mMessageText = new TextUIElement(host);
        mMessageText.setTextColor(getResources().getColor(R.color.tweet_message_text_color));
        mMessageText.setTextSize(getResources().getDimensionPixelOffset(R.dimen.tweet_message_text_size));
        mProfileImage = new ImageUIElement(host);
        mProfileImage.setLayoutParams(new LayoutParams(getResources().getDimensionPixelOffset(R.dimen.tweet_profile_image_size),
                getResources().getDimensionPixelOffset(R.dimen.tweet_profile_image_size)));
        mPostImage = new ImageUIElement(host);
        mPostImage.setLayoutParams(new LayoutParams(100, 100));
        mPostImage.setBackgroundColor(0x3000ff00);
        addElement(mAuthorText);
        addElement(mMessageText);//, new MarginLayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)
        addElement(mProfileImage);
        addElement(mPostImage);

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(UIElement uiElement) {
                 System.out.println("CustomElement group is click.");
                chageTime();
            }
        });

        mPostImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(UIElement uiElement) {
                 System.out.println("CustomElement mPostImage is click.");
            }
        });
    }

    @Override
    public boolean setContext(UIContext host) {
        return super.setContext(host);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int widthUsed = 0;
        int heightUsed = 0;

        measureElementWithMargins(mProfileImage, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
        widthUsed += getMeasuredWidthWithMargins(mProfileImage);

        measureElementWithMargins(mAuthorText, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
        heightUsed += getMeasuredHeightWithMargins(mAuthorText);

        measureElementWithMargins(mMessageText, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
        heightUsed += getMeasuredHeightWithMargins(mMessageText);

        measureElementWithMargins(mPostImage, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
        heightUsed += getMeasuredHeightWithMargins(mPostImage);

        int heightSize = heightUsed + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthSize, heightSize);
       // System.out.println("onMeasure " + widthSize + "X" + heightSize);
    }

    @Override
    public void onLayout(int l, int t, int r, int b) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        int currentTop = paddingTop;

        layoutElement(mProfileImage, paddingLeft, currentTop, mProfileImage.getMeasuredWidth(), mProfileImage.getMeasuredHeight());

        final int contentLeft = getWidthWithMargins(mProfileImage) + paddingLeft;
        final int contentWidth = r - l - contentLeft - getPaddingRight();

        layoutElement(mAuthorText, contentLeft, currentTop, contentWidth, mAuthorText.getMeasuredHeight());
        currentTop += getHeightWithMargins(mAuthorText);

        layoutElement(mMessageText, contentLeft, currentTop, contentWidth, mMessageText.getMeasuredHeight());
        currentTop += getHeightWithMargins(mMessageText);

        layoutElement(mPostImage, paddingLeft, currentTop, mPostImage.getMeasuredWidth(), mPostImage.getMeasuredHeight());
        currentTop += getHeightWithMargins(mPostImage);

        //System.out.println("onLayout " + mProfileImage.getMeasuredWidth() + "X" + mProfileImage.getMeasuredHeight());
    }

    public void setData() {
        mAuthorText.setText("Hello");
        chageTime();
        mProfileImage.setImageResource(R.drawable.ic_launcher);
        mPostImage.setImageResource(R.drawable.tweet_reply);
    }

    public void chageTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);

        mMessageText.setText("time:" + format.format(d1));
    }

}
