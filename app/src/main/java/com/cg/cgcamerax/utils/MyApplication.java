package com.cg.cgcamerax.utils;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

/**
 * @ProjectName: CGCameraX
 * @CreateDate: 2020/8/27 15:31
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class MyApplication extends Application implements CameraXConfig.Provider {

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
