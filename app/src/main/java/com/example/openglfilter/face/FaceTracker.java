package com.example.openglfilter.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class FaceTracker {
    static {
        System.loadLibrary("native-lib");
    }

    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private final int msg_what = 100;
    private long self;
    //结果
    public Face mFace;

    public FaceTracker(String model, String seeta, final int width, final int height) {
        self = native_init(model, seeta);
        mHandlerThread = new HandlerThread("track");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //子线程 耗时再久 也不会对其他地方 (如：opengl绘制线程) 产生影响
                synchronized (FaceTracker.this) {
                    mFace = (Face) native_detector(self, (byte[]) msg.obj,
                            1, msg.arg2, msg.arg1);
                    if(null!=mFace)
                        Log.i("TAG", "handleMessage: "+mFace.toString());;
                }
            }
        };
    }

    public void detector(byte[] data, int width, int height) {
        //把积压的 11号任务移除掉
        mHandler.removeMessages(msg_what);
        //加入新的11号任务
        Message message = mHandler.obtainMessage(msg_what);
        message.obj = data;
        message.arg1 = width;
        message.arg2 = height;
        mHandler.sendMessage(message);
    }

    public void startTrack() {
        native_start(self);
    }

    //传入模型文件， 初始化人脸识别追踪器和人眼定位器
    public native long native_init(String model, String seeta);

    //开始追踪
    public native void native_start(long self);

    //停止追踪
    public native void native_stop(long self);

    //检测人脸
    public native Face native_detector(long self, byte[] data, int cameraId, int width, int
            height);
}
