package com.ramotion.garlandview.example.tail;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ramotion.garlandview.example.R;


public class AlphaScalePageTransformer implements TailLayoutManager.PageTransformer{

    private static final float INACTIVE_SCALE = 0.7f;
    private static final float INACTIVE_ALPHA = 0.5f;

    private static final int OFFSET_MAX = 300;

    @Override
    public void transformPage(@NonNull View page, float position) {
        final LinearLayout ll = (LinearLayout) page.findViewById(R.id.linear_layout);

        final int childCount = ll.getChildCount();
        if (childCount == 0) {
            return;
        }

        final float alpha;
        final float scale;
        if (position < -1) {
            alpha = INACTIVE_ALPHA;
            scale = INACTIVE_SCALE;
        } else if (position <= 1) {
            alpha = INACTIVE_ALPHA + (1 - INACTIVE_ALPHA) * (1 - Math.abs(position));
            scale = INACTIVE_SCALE + (1 - INACTIVE_SCALE) * (1 - Math.abs(position));
        } else {
            alpha = INACTIVE_ALPHA;
            scale = INACTIVE_SCALE;
        }

        final int childHeight = ll.getChildAt(0).getHeight();
        final float yOffset = (childHeight - childHeight * scale) / 2;

        for (int i = 0, cnt = ll.getChildCount(); i < cnt; i++) {
            final View child = ll.getChildAt(i);
            ViewCompat.setAlpha(child, alpha);
            ViewCompat.setScaleX(child, scale);
            ViewCompat.setScaleY(child, scale);

            final float j = Math.max(-2, 1 - i);
            ViewCompat.setTranslationY(child, j * yOffset);
        }

        offsetChildren(ll, position);
    }

    private void offsetChildren(@NonNull LinearLayout ll, float position) {
        if (ll.getChildCount() < 2) {
            return;
        }

        if (position < -1 || position > 1) {
            for (int i = 1, cnt = ll.getChildCount(); i < cnt; i++) {
                ViewCompat.setX(ll.getChildAt(i), 0);
            }
            return;
        }

        float floorDiff = position - (float) Math.floor(position);
        if (floorDiff == 0f) {
            floorDiff = 1f;
        }

        float sign;
        final float childX = ViewCompat.getX(ll.getChildAt(1));
        if (floorDiff > 0.5f) {
            if (childX < -10){
                sign = -1;
            } else {
                sign = 1;
            }
        } else {
            if (childX > 10) {
                sign = 1;
            } else {
                sign = -1;
            }
        }

        Log.d("D", "p: " + position + ", floorDiff: " + floorDiff + ", sign = " + sign);

        for (int i = 1, cnt = ll.getChildCount(); i < cnt; i++) {
            final View child = ll.getChildAt(i);

            final float childOffset;
            if (floorDiff > 0.5f) {
                childOffset = (1f - floorDiff) * OFFSET_MAX * i;
            } else {
                childOffset = floorDiff * OFFSET_MAX * i;
            }

            ViewCompat.setX(child, sign * childOffset);
        }
    }

}
