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

import android.content.Context;
import android.renderscript.Element;
import android.util.AttributeSet;

import com.shenrh.canvas.ImageUIElement;
import com.shenrh.canvas.UIView;
import com.shenrh.layout.R;

public class Custom2ElementView extends UIView {
    public Custom2ElementView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Custom2ElementView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Custom2Element element = new Custom2Element(this);
        element.setClickable(true);
        setUIElement(element);

        ((Custom2Element) getUIElement()).setImageResource(R.drawable.webp);
    }
}
