package ntou.project.djidrone.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import java.util.jar.Attributes;

import ntou.project.djidrone.MobileActivity;

//TODO not done yet
public class MyScrollView extends ScrollView {

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        super.dispatchTouchEvent(ev);
        return MobileActivity.gestureDetector.onTouchEvent(ev) | super.dispatchTouchEvent(ev);
    }
}
