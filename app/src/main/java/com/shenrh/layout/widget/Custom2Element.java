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

import android.util.AttributeSet;
import android.view.MotionEvent;

import com.shenrh.canvas.ImageUIElement;
import com.shenrh.canvas.UIContext;


public class Custom2Element extends ImageUIElement {

    public Custom2Element(UIContext host) {
        this(host, null);
    }

    public Custom2Element(UIContext host, AttributeSet attrs) {
        super(host, attrs);
    }
    
}
