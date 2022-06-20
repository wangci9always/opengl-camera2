package com.example.openglfilter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import com.example.openglfilter.face.FaceTracker;
import com.example.openglfilter.util.OpenGlUtils;

import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener, SurfaceTexture.OnFrameAvailableListener, CameraHelper.OnPreviewListener {
    private static final String TAG = "lcx";
    private CameraHelper cameraHelper;
    private CameraView cameraView;
    private SurfaceTexture mCameraTexure;
    RecordFilter recordFilter;
    private MediaRecorder1 mRecorder;
    //    int
    private CameraFilter cameraFilter;
    private SoulFilter soulFilter;
    private SplitFilter splitFilter;
    private StickerFilter stickerFilter;
    private BeautyFilter beautyFilter;
    private ColorFilter colorFilter;
    private BigEyeFilter bigEyeFilter;
    private FaceTracker faceTracker;
    private Context context;
    private int[] textures;
    float[] mtx = new float[16];
    public volatile boolean mReadPixels = false;
    public volatile boolean mBeauty = false;//美艳
    public volatile boolean mFilter = false;//滤镜
    public volatile boolean mEffect = false;//特效
    public volatile boolean mEffect2 = false;//特效2
    private int mPreviewWdith;
    private int mPreviewHeight;

    private int screenSurfaceWid;
    private int screenSurfaceHeight;
    private int screenX;
    private int screenY;

    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
        context = cameraView.getContext();
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
//        打开摄像头
        cameraHelper = new CameraHelper(lifecycleOwner, this, this);

    }

    //textures 主线程    1   EGL线程
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

//surface
        textures = new int[1];
//        1
//        让 SurfaceTexture   与 Gpu  共享一个数据源  0-31
        mCameraTexure.attachToGLContext(textures[0]);
//监听摄像头数据回调，
        mCameraTexure.setOnFrameAvailableListener(this);
        cameraFilter = new CameraFilter(cameraView.getContext());
        Context context = cameraView.getContext();
        recordFilter = new RecordFilter(context);
        soulFilter = new SoulFilter(context);
        splitFilter = new SplitFilter(context);
        stickerFilter = new StickerFilter(context);
        beautyFilter = new BeautyFilter(context);
        colorFilter = new ColorFilter(context);
        bigEyeFilter = new BigEyeFilter(context);


        File file = new File(Environment.getExternalStorageDirectory(), "input.mp4");
        if (file.exists()) {
            file.delete();
        }

        String path = file.getAbsolutePath();
        mRecorder = new MediaRecorder1(cameraView.getContext(), path,
                EGL14.eglGetCurrentContext(),
                1080, 1920);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged 宽: "+width+"---高:"+height);
        faceTracker = new FaceTracker(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/lbpcascade_frontalface.xml", context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/seeta_fa_v1.1.bin",width,height);
        faceTracker.startTrack();
        float scaleX = (float) mPreviewHeight / (float) width;
        float scaleY = (float) mPreviewWdith / (float) height;

        float max = Math.max(scaleX, scaleY);

        screenSurfaceWid = (int) (mPreviewHeight / max);
        screenSurfaceHeight = (int) (mPreviewWdith / max);
        screenX = width - (int) (mPreviewHeight / max);
        screenY = height - (int) (mPreviewWdith / max);
//        width=screenSurfaceWid;
//        height=screenSurfaceHeight;
        recordFilter.setSize(width, height,screenX,screenY);
        cameraFilter.setSize(width, height,screenX,screenY);
        soulFilter.setSize(width, height,screenX,screenY);
        stickerFilter.setSize(width, height,screenX,screenY);
        beautyFilter.setSize(width, height,screenX,screenY);
        splitFilter.setSize(width, height,screenX,screenY);
        colorFilter.setSize(width, height,screenX,screenY);
        bigEyeFilter.setSize(width, height,screenX,screenY);
    }

    //  有数据的时候给
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i(TAG, "线程: " + Thread.currentThread().getName());
//        摄像头的数据  ---》
//        更新摄像头的数据  给了  gpu
        mCameraTexure.updateTexImage();
//        不是数据
        mCameraTexure.getTransformMatrix(mtx);
        cameraFilter.setTransformMatrix(mtx);


//id     FBO所在的图层   纹理  摄像头 有画面      有1  没有  画面       录屏
        int id = cameraFilter.onDraw(textures[0]);
// 加载   新的顶点程序 和片元程序  显示屏幕  id  ----》fbo--》 像素详细
//        显示到屏幕
        if (mBeauty) {
            id = beautyFilter.onDraw(id);
            bigEyeFilter.setFace(faceTracker.mFace);
            id = bigEyeFilter.onDraw(id);
        }
        if (mFilter) {
            id = colorFilter.onDraw(id);
        }
        if (mEffect) {
            id = soulFilter.onDraw(id);
        }
        if (mEffect2) {
            id = splitFilter.onDraw(id);
        }
        stickerFilter.setFace(faceTracker.mFace);
        id = stickerFilter.onDraw(id);
//        是一样的
        id = recordFilter.onDraw(id);


//        拿到了fbo的引用   ---》  编码视频 、直播、推流等
        mRecorder.fireFrame(id, mCameraTexure.getTimestamp());
        if (mReadPixels) {
            OpenGlUtils.saveToLocal(cameraView.getContext(), OpenGlUtils.createBitmapFromGLSurface(0, 0, cameraFilter.mWidth, cameraFilter.mHeight), OpenGlUtils.getResultImgFile(".jpg").getPath());
            mReadPixels = false;
        }
    }

    @Override
    public void onUpdated(Preview.PreviewOutput output) {
//        摄像头预览到的数据 在这里
        mCameraTexure = output.getSurfaceTexture();
        mPreviewWdith=output.getTextureSize().getWidth();
        mPreviewHeight=output.getTextureSize().getHeight();
    }

    //当有数据 过来的时候
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//一帧 一帧回调时
        cameraView.requestRender();
    }

    public void startRecord(float speed) {
        try {
            mRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        mRecorder.stop();
    }

    public void readPixels(boolean save) {
        mReadPixels = save;
    }

    public void openBeauty(boolean beauty) {
        mBeauty = beauty;
    }

    public void openFilter(boolean filter) {
        mFilter = filter;
    }

    public void openEffect(boolean effect) {
        mEffect = effect;
    }

    public void openEffect2(boolean effect) {
        mEffect2 = effect;
    }

    @Override
    public void onPreviewFrame(byte[] data, int len,int width,int height) {
        if (faceTracker != null && (mBeauty)) faceTracker.detector(data,width,height);
    }
}
