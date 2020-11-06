package com.mei.guide.load;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;


/**
 * 加载等待
 */
public class LoadingView extends AppCompatImageView {

    private LoadingDrawable mLoadingDrawable;

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mLoadingDrawable = new LoadingDrawable(getContext());
        setImageDrawable(mLoadingDrawable);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mLoadingDrawable.show(View.VISIBLE == visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLoadingDrawable.show(false);
    }

}
