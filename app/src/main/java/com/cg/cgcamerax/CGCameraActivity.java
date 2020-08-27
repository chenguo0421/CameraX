package com.cg.cgcamerax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.CameraView;
import androidx.core.app.ActivityCompat;


import com.cg.cgcamerax.utils.FileUtils;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @ProjectName: CGCameraX
 * @CreateDate: 2020/8/27 10:02
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class CGCameraActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraView camera_view;
    private AppCompatTextView tv_take;
    private AppCompatTextView tv_recorder;
    private Executor executor = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);
        camera_view = findViewById(R.id.camera_view);
        tv_take = findViewById(R.id.tv_take);
        tv_recorder = findViewById(R.id.tv_recorder);
        initListener();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO}, 1000);
            return;
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            camera_view.bindToLifecycle(this);
            //我测试时，试过在拍照前切换 camera_view.setCaptureMode(CameraView.CaptureMode.IMAGE);
            //在录像前切换 camera_view.setCaptureMode(CameraView.CaptureMode.VIDEO);
            //分别切换的时候无法正常录像，于是试用了   camera_view.setCaptureMode(CameraView.CaptureMode.MIXED);
            //API解释说该种模式不一定支持所有设备
            camera_view.setCaptureMode(CameraView.CaptureMode.MIXED);
        }
    }

    private void initListener() {
        tv_take.setOnClickListener(this);
        tv_recorder.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                camera_view.bindToLifecycle(this);
                //我测试时，试过在拍照前切换 camera_view.setCaptureMode(CameraView.CaptureMode.IMAGE);
                //在录像前切换 camera_view.setCaptureMode(CameraView.CaptureMode.VIDEO);
                //分别切换的时候无法正常录像，于是试用了   camera_view.setCaptureMode(CameraView.CaptureMode.MIXED);
                //API解释说该种模式不一定支持所有设备
                camera_view.setCaptureMode(CameraView.CaptureMode.MIXED);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_take){
            takePhoto();
        }else if (v.getId() == R.id.tv_recorder){
            recorder();
        }
    }

    private void takePhoto() {
        //方式一：直接返回图片的代理对象
//        camera_view.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
//            @Override
//            public void onCaptureSuccess(@NonNull ImageProxy image) {
//                super.onCaptureSuccess(image);
//                showToast("拍照成功,image width = " + image.getWidth() + " , image height = " + image.getHeight());
//            }
//
//            @Override
//            public void onError(@NonNull ImageCaptureException exception) {
//                super.onError(exception);
//                showToast("拍照失败");
//            }
//        });

        //方式二：返回图片路径
        File file = FileUtils.getImageCacheFile(this);
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();
        camera_view.takePicture(options, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                showToast("拍照成功,image path = " + file.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                showToast("拍照失败");
            }
        });

    }

    private void showToast(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CGCameraActivity.this,s,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void recorder() {
        if (camera_view.isRecording()){//停止录像
            showToast("停止录像");
            camera_view.stopRecording();
        }else {//开始录像
            showToast("开始录像");
            camera_view.startRecording(FileUtils.getVideoCacheFile(this), executor, new VideoCapture.OnVideoSavedCallback() {
                @Override
                public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                    showToast("录像已保存 outputFileResults = " + outputFileResults.getSavedUri().getPath());
                }

                @Override
                public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                    showToast("录像出错, message = " + message);
                }
            });
        }
    }
}
