# UILayout
基于项目https://github.com/lucasr/android-layout-samples 开发

提供一种Android自定义view的实现方案，可使listview的一整个复杂item基于一个view实现，而且还保持原有的view模块的概念。
实现了点击事件的分发部分。

提供一套UIElement的概念来替换Android的view，最终把所有UIElement合并到一个UIView上。

提供了基本的控件TextUIElement和ImageUIElement以及UIElementGroup，并且实现了基本的LinearLayoutUIElement线性布局，可以基于此构建复杂的布局。

使用上比较简单，从UIView继承实现自己的自定义view，该view需要通过setUIElement绑定一个Element，自定义view的onMeasure和onLayout部分都在该element实现，由此可以在非ui线程初始化"view"，具体可参考示例部分。


---

Based on the development of the project https://github.com/lucasr/android-layout-samples

Provide a scheme of an Android custom view can make the a whole complex of listview item based on a view, but also keep the original view module concept.To implement the click event distribution of parts.

The concept of providing a UIElement to replace Android's view, all UIElement eventually merge onto one UIView.

It provides basic controls TextUIElement and ImageUIElement and UIElementGroup, and to achieve the basic LinearLayoutUIElement linear layout, you can build complex layouts based on this.

The use of relatively simple, inherited from UIView implement your own custom view, the view through a binding setUIElement Element,
Custom view of onMeasure and onLayout parts are realized in the element, which can be in a non-ui thread initialization "view", specifically refer to the Examples section.

