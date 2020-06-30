package ntou.project.djidrone.listener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(getClass().getName(),"tapDown");
        return super.onDown(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final int FLING_MIN_DISTANCE = 50, FLING_MIN_VELOCITY = 100;
        Log.d(getClass().getName(), "" + velocityX + velocityY);
        Log.d(getClass().getName(), "onFling");
        return super.onFling(e1, e2, velocityX, velocityY);
    }
}
