package com.nomad_gps.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nomad_gps.R;

/**
 * Created by kuandroid on 7/17/15.
 */
public class SettingView extends RelativeLayout {

    private String title,desc;
    private TextView tvTitle,tvDesc;

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SettingView, 0, 0);
        try {
            //get the text and colors specified using the names in attrs.xml
            title = a.getString(R.styleable.SettingView_setting_title);
            desc = a.getString(R.styleable.SettingView_setting_desc);
        } finally {
            a.recycle();
        }

        inflate(context,R.layout.setting_view,this);
        this.tvTitle = (TextView)findViewById(R.id.title);
        this.tvDesc = (TextView)findViewById(R.id.desc);
        tvTitle.setText(title);
        tvDesc.setText(desc);
    }
}
