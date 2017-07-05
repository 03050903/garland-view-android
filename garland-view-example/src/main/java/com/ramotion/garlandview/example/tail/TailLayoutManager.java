package com.ramotion.garlandview.example.tail;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

public class TailLayoutManager extends RecyclerView.LayoutManager
        implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    public interface PageTransformer {
        void transformPage(@NonNull View page, float position);
    }

    private static final int SIDE_OFFSET = 60;
    private static final int VIEW_DISTANCE = 0;

    private final SparseArray<View> mViewCache = new SparseArray<>();

    private final int mSideOffset; // TODO: remove - center first view
    private final int mViewDistance;

    private PageTransformer mPageTransformer;

    public TailLayoutManager(@NonNull Context context) {
        super();

        final float density = context.getResources().getDisplayMetrics().density;
        mViewDistance = (int) (VIEW_DISTANCE * density);
        mSideOffset = (int) (SIDE_OFFSET * density);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return getChildCount() != 0;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        final int anchorPos = getAnchorPosition();

        detachAndScrapAttachedViews(recycler);
        layoutViews(anchorPos, recycler, state);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }

        int scrolled;

        if (dx < 0) {
            final View view = getChildAt(0);
            final int viewPos = getPosition(view);
            final int viewLeft = getDecoratedLeft(view);
            final int view0Left = viewLeft - viewPos * getDecoratedMeasuredWidth(view);
            final int view0LeftScrolled = view0Left - dx;

            final int border = mSideOffset;
            if (view0LeftScrolled <= border) {
                scrolled = dx;
            } else {
                scrolled = -(border - Math.abs(view0Left));
            }
        } else {
            final View view = getChildAt(childCount - 1);
            final int viewPos = getPosition(view);
            final int viewLeft = getDecoratedLeft(view);
            final int viewNLeft = viewLeft + (getItemCount() - 1 - viewPos) * getDecoratedMeasuredWidth(view);
            final int viewNLeftScrolled = viewNLeft - dx;

            final int border = mSideOffset;
            if (viewNLeftScrolled >= border) {
                scrolled = dx;
            } else {
                scrolled = viewNLeft - border;
            }
        }

        offsetChildrenHorizontal(-scrolled);

        layoutViews(getAnchorPosition(), recycler, state);

        return scrolled;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;
        return new PointF(direction, 0);
    }

    public void setPageTransformer(PageTransformer transformer) {
        mPageTransformer = transformer;
    }

    public int getSideOffset() {
        return mSideOffset;
    }

    private int getAnchorPosition() {
        if (getChildCount() == 0) {
            return 0;
        } else {
            return getPosition(getChildAt(0));
        }
    }

    private void layoutViews(int anchorPos, RecyclerView.Recycler recycler, RecyclerView.State state) {
        mViewCache.clear();

        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            View view = getChildAt(i);
            mViewCache.put(getPosition(view), view);
        }

        for (int i = 0, cnt = mViewCache.size(); i < cnt; i++) {
            detachView(mViewCache.valueAt(i));
        }

        fill(anchorPos, recycler, state);

        for (int i = 0, cnt = mViewCache.size(); i < cnt; i++) {
            recycler.recycleView(mViewCache.valueAt(i));
        }

        applyTransformation();
    }

    private void fill(int anchorPos, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int itemCount = getItemCount();
        final int width = getWidth();
        final int viewWidth = width - mSideOffset * 2;

        int viewLeft = mSideOffset;
        int pos = anchorPos;

        boolean fillRight = true;
        while (fillRight && pos < itemCount) {
            View view = mViewCache.get(pos);
            if (view != null) {
                attachView(view);
                mViewCache.remove(pos);
            } else {
                view = recycler.getViewForPosition(pos);
                addView(view);
                measureChildWithMargins(view, mSideOffset * 2, 0);
                layoutDecorated(view, viewLeft, 0, viewLeft + viewWidth, getDecoratedMeasuredHeight(view));
            }

            viewLeft = getDecoratedRight(view) + mViewDistance;
            fillRight = viewLeft < width;
            pos++;
        }
    }

    private void applyTransformation() {
        if (mPageTransformer == null) {
            return;
        }

        final int viewWidth = getWidth() - mSideOffset * 2;
        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            final View view = getChildAt(i);
            final int viewLeft = getDecoratedLeft(view);
            final float position = ((float) (viewLeft - mSideOffset)) / viewWidth;

            mPageTransformer.transformPage(view, position);
        }
    }

}
