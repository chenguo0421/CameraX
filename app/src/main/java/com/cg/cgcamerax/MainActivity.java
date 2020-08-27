package com.cg.cgcamerax;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatTextView tv_1;
    private AppCompatTextView tv_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();

    }

    private void initListener() {
        tv_1.setOnClickListener(this);
        tv_2.setOnClickListener(this);
    }

    private void initView() {
        tv_1 = findViewById(R.id.tv_1);
        tv_2 = findViewById(R.id.tv_2);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_1){
            //拍照录像
            startActivity(new Intent(this,CGCameraActivity.class));
        }else if (v.getId() == R.id.tv_2){
            //取帧数据分析
            startActivity(new Intent(this,PreviewActivity.class));
        }
    }
}