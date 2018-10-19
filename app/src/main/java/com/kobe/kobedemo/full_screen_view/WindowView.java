package com.kobe.kobedemo.full_screen_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.kobe.kobedemo.R;


public class WindowView extends RelativeLayout implements OnClickListener {
    private RelativeLayout layout;
    private OnViewTouch onViewTouch;

    public WindowView(Context context,AttributeSet attrs,int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        init(context);
    }

    public WindowView(Context context,AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    public WindowView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.clockview,this);
        layout = (RelativeLayout) view.findViewById(R.id.clockView);
        layout.setOnClickListener(this);
    }

    public void setOnViewTouch(OnViewTouch onViewTouch) {
        this.onViewTouch = onViewTouch;
    }

    @Override
    public void onClick(View view) {
        if(view == layout) {
            if(onViewTouch != null) {
                onViewTouch.onTouch();
            }
        }
    }

    public interface OnViewTouch {
        public void onTouch();
    }


}
