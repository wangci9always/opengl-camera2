package com.example.openglfilter;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import com.example.openglfilter.util.ImageUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

public class CameraHelper {
    private static String TAG = "CameraHelper";
    private HandlerThread handlerThread;
    private CameraX.LensFacing currentFacing = CameraX.LensFacing.FRONT;
    private Preview.OnPreviewOutputUpdateListener listener;
    private ImageAnalysis.Analyzer analyzer;
    private OnPreviewListener onPreviewListener;
    private ReentrantLock lock = new ReentrantLock();
    private byte[] y;
    private byte[] u;
    private byte[] v;
    private MediaCodec mediaCodec;
    private byte[] nv21;
    byte[] nv21_rotated;
    byte[] nv12;

    public CameraHelper(LifecycleOwner lifecycleOwner, Preview.OnPreviewOutputUpdateListener listener, OnPreviewListener onPreviewListener) {
        this.listener = listener;
        this.onPreviewListener = onPreviewListener;
        analyzer = new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image, int rotationDegrees) {
                handleData(image, rotationDegrees);
            }
        };
        handlerThread = new HandlerThread("Analyze-thread");
        handlerThread.start();
        CameraX.bindToLifecycle(lifecycleOwner, getPreView(), getImageAnalysis());

//        直播camerax  打开的
    }

    /**
     * 处理相机输出数据
     */
    private void handleData(ImageProxy image, int rotationDegrees) {
        Log.i(TAG, "analyze: " + image.getWidth() + "  height " + image.getHeight());
        lock.lock();
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        // 重复使用同一批byte数组，减少gc频率
        if (y == null) {
//            初始化y v  u
            y = new byte[planes[0].getBuffer().limit() - planes[0].getBuffer().position()];
            u = new byte[planes[1].getBuffer().limit() - planes[1].getBuffer().position()];
            v = new byte[planes[2].getBuffer().limit() - planes[2].getBuffer().position()];
        }

        if (image.getPlanes()[0].getBuffer().remaining() == y.length) {
            planes[0].getBuffer().get(y);
            planes[1].getBuffer().get(u);
            planes[2].getBuffer().get(v);
            int stride = planes[0].getRowStride();
            Size size = new Size(image.getWidth(), image.getHeight());
            int width = size.getHeight();
            int heigth = image.getWidth();
            Log.i(TAG, "analyze: " + width + "  heigth " + heigth);
            if (nv21 == null) {
                nv21 = new byte[heigth * width * 3 / 2];
                nv21_rotated = new byte[heigth * width * 3 / 2];
            }
            ImageUtil.yuvToNv21(y, u, v, nv21, heigth, width);
//            ImageUtil.nv21_rotate_to_90(nv21, nv21_rotated, heigth, width);
//            byte[] temp = ImageUtil.nv21toNV12(nv21, nv12);
            if (null != onPreviewListener) {
                onPreviewListener.onPreviewFrame(nv21, nv21.length,width,heigth);
            }
        }

        lock.unlock();
    }

    private Preview getPreView() {
        // 分辨率并不是最终的分辨率，CameraX会自动根据设备的支持情况，结合你的参数，设置一个最为接近的分辨率
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetResolution(new Size(1200, 1600))
                .setLensFacing(currentFacing) //前置或者后置摄像头
                .build();
//        要不  得到他的数据
        Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(listener);
        return preview;
    }

    private ImageAnalysis getImageAnalysis() {
        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setCallbackHandler(new Handler(handlerThread.getLooper()))
                .setLensFacing(currentFacing)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetResolution(new Size(1200, 1600))
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer(analyzer);
        return imageAnalysis;
    }

    public interface OnPreviewListener {
        void onPreviewFrame(byte[] data, int len,int width,int height);
    }
}
