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
import android.content.res.ColorStateList;
import android.widget.TextView;

/**
 * 适配TextView TextUIElement
 * 
 * @author shenrh
 * 
 */
public class TextViewAdapter {

    private TextView mTextView;
    private TextUIElement mTextUIElement;

    private TextViewAdapter(Object object) {
        if (object instanceof TextView) {
            mTextView = (TextView) object;
            mTextUIElement = null;
        } else if (object instanceof TextUIElement) {
            mTextUIElement = (TextUIElement) object;
            mTextView = null;
        } else {
            throw new IllegalArgumentException("only TextView and TextUIElement.");
        }
    }

    public static TextViewAdapter get(Object object) {
        if (object != null) {
            return new TextViewAdapter(object);
        }
        return null;
    }

    public final Context getContext() {
        if (mTextView != null) {
            return mTextView.getContext();
        } else if (mTextUIElement != null) {
            return mTextUIElement.getContext();
        }
        return null;
    }

    public void setTextColor(int color) {
        if (mTextView != null) {
            mTextView.setTextColor(color);
        } else if (mTextUIElement != null) {
            mTextUIElement.setTextColor(color);
        }
    }

    public void setTextColor(ColorStateList colors) {
        if (mTextView != null) {
            mTextView.setTextColor(colors);
        } else if (mTextUIElement != null) {
            mTextUIElement.setTextColor(colors);
        }
    }

    public final void setText(CharSequence text) {
        if (mTextView != null) {
            mTextView.setText(text);
        } else if (mTextUIElement != null) {
            mTextUIElement.setText(text);
        }
    }

    public final CharSequence getText() {
        if (mTextView != null) {
            return mTextView.getText();
        } else if (mTextUIElement != null) {
            return mTextUIElement.getText();
        }
        return null;
    }

    public void setMaxLines(int maxlines) {
        if (mTextView != null) {
            mTextView.setMaxLines(maxlines);
        } else if (mTextUIElement != null) {
            mTextUIElement.setMaxLines(maxlines);
        }
    }

    public void setSingleLine() {
        if (mTextView != null) {
            mTextView.setSingleLine();
        } else if (mTextUIElement != null) {
            mTextUIElement.setSingleLine();
        }
    }

    public int getMaxLines() {
        if (mTextView != null) {
            return mTextView.getMaxLines();
        } else if (mTextUIElement != null) {
            return mTextUIElement.getMaxLines();
        }
        return -1;
    }

    public void setVisibility(int visibility) {
        if (mTextView != null) {
            mTextView.setVisibility(visibility);
        } else if (mTextUIElement != null) {
            mTextUIElement.setVisibility(visibility);
        }
    }

    public void setTextSize(float size) {
        if (mTextView != null) {
            mTextView.setTextSize(size);
        } else if (mTextUIElement != null) {
            mTextUIElement.setTextSize(size);
        }
    }

    public void setTextSize(int unit, float size) {
        if (mTextView != null) {
            mTextView.setTextSize(unit, size);
        } else if (mTextUIElement != null) {
            mTextUIElement.setTextSize(unit, size);
        }
    }

    @Override
    public int hashCode() {
        if (mTextView != null) {
            return mTextView.hashCode();
        } else if (mTextUIElement != null) {
            return mTextUIElement.hashCode();
        }
        return super.hashCode();
    }

    public Object getValue() {
        if (mTextView != null) {
            return mTextView;
        } else if (mTextUIElement != null) {
            return mTextUIElement;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TextViewAdapter && getValue() != null) {
            return getValue().equals(((TextViewAdapter) o).getValue());
        }
        return super.equals(o);
    }
}
