package com.cg.cgcamerax;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cg.cgcamerax.utils.DecodeTools;
import com.cg.cgcamerax.utils.StringUtil;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ProjectName: CGCameraX
 * @CreateDate: 2020/8/27 11:53
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private PreviewView previewView;
    private AppCompatImageView iv_img;
    private ImageCapture mImageCapture;
    private ImageAnalysis mImageAnalysis;
    private Executor executor = Executors.newFixedThreadPool(1);
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ReentrantLock analysisLock = new ReentrantLock();
    private boolean mIsNextAnalysis = true;
    private String mQrText;
    private AppCompatTextView tv_cameraChange;
    private int cameraType = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax_preview);
        previewView = findViewById(R.id.preview);
        tv_cameraChange = findViewById(R.id.tv_cameraChange);
        // 进行相机画面预览之前，设置想要的实现模式
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        iv_img = findViewById(R.id.iv_img);
        initListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO}, 1000);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        }

    }

    private void initListener() {
        tv_cameraChange.setOnClickListener(this);
    }

    private void initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                // 绑定预览
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        initImageCapture();
        initImageAnalysis();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initCamera();
            }
        }
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();


        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraType)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, mImageAnalysis, mImageCapture);
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
    }

    private void initImageAnalysis() {

        mImageAnalysis = new ImageAnalysis.Builder()
                // 分辨率
                .setTargetResolution(new Size(1280, 720))
                // 仅将最新图像传送到分析仪，并在到达图像时将其丢弃。
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        mImageAnalysis.setAnalyzer(executor, image -> {
            if ((image.getFormat() == ImageFormat.YUV_420_888
                    || image.getFormat() == ImageFormat.YUV_422_888
                    || image.getFormat() == ImageFormat.YUV_444_888)
                    && image.getPlanes().length == 3) {

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, image.getWidth(), image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), false);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    Result result = DecodeTools.getDefaultMultiFormatReader().decode(binaryBitmap);

                    //该种方式只解析一次二维码
                    if (result != null && (StringUtil.isEmpty(mQrText) || !mQrText.equals(result.getText()))) {
                        mQrText = result.getText();
                        Log.e("CG",result.toString());
                        showToast(result.getText());
                        mIsNextAnalysis = false;
                        //得到二维码结果后，可进行后续处置
                        // ......
                    }


                    //该种方式会一直解析预览窗口中的二维码
//                    if (result != null) {
//                        Log.e("CG",result.toString());
//                        showToast(result.getText());
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mIsNextAnalysis) {
                image.close();
            }
        });
    }

    private void showToast(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PreviewActivity.this,s,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initImageCapture() {
        // 构建图像捕获用例
        mImageCapture = new ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        // 旋转监听
        OrientationEventListener orientationEventListener = new OrientationEventListener((Context) this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;

                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                mImageCapture.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cameraChange){
            changeCamera();
        }
    }

    private void changeCamera() {
        if (cameraType == CameraSelector.LENS_FACING_FRONT){
            cameraType = CameraSelector.LENS_FACING_BACK;
        }else {
            cameraType = CameraSelector.LENS_FACING_FRONT;
        }
        initCamera();
    }
}
