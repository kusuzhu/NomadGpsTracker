package com.nomad_gps.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nomad_gps.R;

/**
 * Created by kuandroid on 7/17/15.
 */
public class NumPicker extends RelativeLayout implements View.OnLongClickListener, View.OnTouchListener {

    private String title;
    private int maxValue,minValue,curValue;
    private TextView tvValue,tvTitle;
    private View up,down;
    private Handler handler;
    private Runnable runnable;
    private boolean toCount=false;
    private boolean onLong=false;

    public NumPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SettingView, 0, 0);
        try {
            //get the text and colors specified using the names in attrs.xml
            maxValue = a.getInteger(R.styleable.NumPicker_value_max, 1);
            minValue = a.getInteger(R.styleable.NumPicker_value_min,1);
            curValue = 1;
        } finally {
            a.recycle();
        }
        inflate(context,R.layout.picker_view,this);
        tvTitle = (TextView)findViewById(R.id.title);
        tvValue = (TextView)findViewById(R.id.value);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);

        up.setOnLongClickListener(this);
        down.setOnLongClickListener(this);
        up.setOnTouchListener(this);
        down.setOnTouchListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        onLong=true;
        toCount=true;
        handler = new Handler();
        switch(v.getId()){
            case R.id.up:
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (curValue < maxValue && toCount) {
                            tvValue.setText(++curValue + "");
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                break;
            case R.id.down:
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (curValue > minValue && toCount) {
                            tvValue.setText(--curValue + "");
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                break;
        }
        handler.postDelayed(runnable,100);
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.onTouchEvent(event);
        // We're only interested in when the button is released.
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // We're only interested in anything if our speak button is currently pressed.
            toCount=false;
            switch(v.getId()){
                case R.id.up:
                    if (curValue<maxValue){
                        tvValue.setText(++curValue+"");
                    }
                    break;
                case R.id.down:
                    if (curValue>minValue){
                        tvValue.setText(--curValue+"");
                    }
                    break;
            }
        }
        return false;
    }

    public int getCurValue(){return curValue;}

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setTitle(String title){
        this.title=title;
        tvTitle.setText(title);
    }
    public void setCurValue(int curValue){
        this.curValue=curValue;
        tvValue.setText(curValue+"");
    }


}
