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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 适配ImageView ImageUIElement
 * 
 * @author shenrh
 * 
 */
public class ImageViewAdapter {

    private ImageView mImageView;
    private ImageUIElement mImageUIElement;

    private ImageViewAdapter(Object object) {
        if (object instanceof ImageView) {
            mImageView = (ImageView) object;
            mImageUIElement = null;
        } else if (object instanceof ImageUIElement) {
            mImageUIElement = (ImageUIElement) object;
            mImageView = null;
        } else {
            throw new IllegalArgumentException("only ImageView and ImageUIElement.");
        }
    }

    public static ImageViewAdapter get(Object object) {
        if (object != null) {
            return new ImageViewAdapter(object);
        }
        return null;
    }

    public final Context getContext() {
        if (mImageView != null) {
            return mImageView.getContext();
        } else if (mImageUIElement != null) {
            return mImageUIElement.getContext();
        }
        return null;
    }

    public void setVisibility(int visibility) {
        if (mImageView != null) {
            mImageView.setVisibility(visibility);
        } else if (mImageUIElement != null) {
            mImageUIElement.setVisibility(visibility);
        }
    }

    public void setImageResource(int resId) {
        if (mImageView != null) {
            mImageView.setImageResource(resId);
        } else if (mImageUIElement != null) {
            mImageUIElement.setImageResource(resId);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        if (mImageView != null) {
            mImageView.setImageDrawable(drawable);
        } else if (mImageUIElement != null) {
            mImageUIElement.setImageDrawable(drawable);
        }
    }

    public void setImageBitmap(Bitmap bm) {
        if (mImageView != null) {
            mImageView.setImageBitmap(bm);
        } else if (mImageUIElement != null) {
            mImageUIElement.setImageBitmap(bm);
        }
    }

    public void setScaleType(ScaleType scaleType) {
        if (mImageView != null) {
            mImageView.setScaleType(scaleType);
        } else if (mImageUIElement != null) {
            mImageUIElement.setScaleType(scaleType);
        }
    }

    @Override
    public int hashCode() {
        if (mImageView != null) {
            return mImageView.hashCode();
        } else if (mImageUIElement != null) {
            return mImageUIElement.hashCode();
        }
        return super.hashCode();
    }

    public Object getValue() {
        if (mImageView != null) {
            return mImageView;
        } else if (mImageUIElement != null) {
            return mImageUIElement;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageViewAdapter && getValue() != null) {
            return getValue().equals(((ImageViewAdapter) o).getValue());
        }
        return super.equals(o);
    }
}
