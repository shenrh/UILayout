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
package com.shenrh.layout;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.shenrh.layout.R;
import com.shenrh.layout.widget.CustomElement;
import com.shenrh.layout.widget.CustomElementView;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    private CustomElementView mCustomElementView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCustomElementView = (CustomElementView) findViewById(R.id.tweetview);

        mTextView = (TextView) findViewById(R.id.android_text);
    }

    public void onClick(View view) {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        mTextView.setText(format.format(d1));
        ((CustomElement) mCustomElementView.getUIElement()).chageTime();
    }
}
