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
import android.view.View;

/**
 * 适配View UIElement
 * 
 * @author shenrh
 * 
 */
public class ViewAdapter {

    private View mView;
    private UIElement mUIElement;

    public ViewAdapter(Object object) {
        if (object instanceof View) {
            mView = (View) object;
            mUIElement = null;
        } else if (object instanceof UIElement) {
            mUIElement = (UIElement) object;
            mView = null;
        } else {
            throw new IllegalArgumentException("only View and UIElement.");
        }
    }

    public final Context getContext() {
        if (mView != null) {
            return mView.getContext();
        } else if (mUIElement != null) {
            return mUIElement.getContext();
        }
        return null;
    }

    public void setVisibility(int visibility) {
        if (mView != null) {
            mView.setVisibility(visibility);
        } else if (mUIElement != null) {
            mUIElement.setVisibility(visibility);
        }
    }

    @Override
    public int hashCode() {
        if (mView != null) {
            return mView.hashCode();
        } else if (mUIElement != null) {
            return mUIElement.hashCode();
        }
        return super.hashCode();
    }

    public Object getValue() {
        if (mView != null) {
            return mView;
        } else if (mUIElement != null) {
            return mUIElement;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ViewAdapter && getValue() != null) {
            return getValue().equals(((ViewAdapter) o).getValue());
        }
        return super.equals(o);
    }
}
