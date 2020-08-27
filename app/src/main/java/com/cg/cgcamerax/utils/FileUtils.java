package com.cg.cgcamerax.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @ProjectName: CGCameraX
 * @CreateDate: 2020/8/27 10:45
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FileUtils {
    /**
     * Android 10之后只允许在自己应用内创建目录
     * @param context
     * @return
     */
    public static String getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT>=29){
                //Android10之后
                sdDir = context.getExternalFilesDir(null);
            }else {
                sdDir = Environment.getExternalStorageDirectory();// 获取SD卡根目录
            }
        } else {
            sdDir = Environment.getRootDirectory();// 获取跟目录
        }
        return sdDir.toString();
    }


    /**
     * 获取图片缓存目录
     * @since V1.0
     */
    public static File getImageCacheDirPath(Context context) {
        File SDFolder;
        try {
            SDFolder = new File(getSDPath(context) , File.separator + getAppName(context) + File.separator + "data" + File.separator + "ImageCache");
            if (!SDFolder.exists()) {
                boolean ret = SDFolder.mkdirs();
                if (ret) {
                    Log.d("FileUtils", "SDFolder.mkdir success");
                }
            }
            return SDFolder;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取录像缓存目录
     * @since V1.0
     */
    public static File getImageCacheFile(Context context) {
        File dirFile = getImageCacheDirPath(context);
        File file = new File(dirFile, DataUtils.getTimestamp("-") + ".jpg");
        if (file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 获取录像缓存目录
     * @since V1.0
     */
    public static File getVideoCacheDirPath(Context context) {
        File SDFolder;
        try {
            SDFolder = new File(getSDPath(context) , File.separator + getAppName(context) + File.separator + "data" + File.separator + "VideoCache");
            if (!SDFolder.exists()) {
                boolean ret = SDFolder.mkdirs();
                if (ret) {
                    Log.d("FileUtils", "SDFolder.mkdir success");
                }
            }
            return SDFolder;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取录像缓存目录
     * @since V1.0
     */
    public static File getVideoCacheFile(Context context) {
        File dirFile = getVideoCacheDirPath(context);
        File file = new File(dirFile, DataUtils.getTimestamp("-") + ".mp4");
        if (file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    /**
     * 获取acitivty所在的应用名称
     * @param context 上下文
     * @return
     */
    public static String getAppName(Context context)
    {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = context.getApplicationInfo();
        String appName = pm.getApplicationLabel(appInfo).toString();  // 获取当前游戏名称

        return appName;
    }

    /**
     * 保存bitmap到文件
     * @param file
     * @param bitmap
     * @return
     */
    public static String saveBitmap(File file, Bitmap bitmap) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
