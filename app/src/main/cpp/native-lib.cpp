//
// Created by Administrator on 2022/6/19.
//
#include <jni.h>
#include <string>
#include "FaceTracker.h"
#include "include/opencv2/core/types.hpp"

#define CLASS_NAME_FACE_TRACKER "com/example/openglfilter/face/FaceTracker"
#define CLASS_NAME_FACE "com/example/openglfilter/face/Face"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "native-lib", __VA_ARGS__)
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT void JNICALL
native_start(JNIEnv
             *env,
             jobject thiz, jlong
             self) {
    if (self == 0) {
        return;
    }
    FaceTracker *me = (FaceTracker *) self;
    me->

            startTracking();

}

JNIEXPORT jobject
JNICALL
native_detector(JNIEnv *env, jobject
thiz,
                jlong self, jbyteArray
                data_,
                jint camera_id, jint
                width,
                jint height
) {
    if (self == 0) {
        return
                NULL;
    }

    jbyte *data = env->GetByteArrayElements(reinterpret_cast<jbyteArray>(data_), NULL);

    FaceTracker *faceTracker = reinterpret_cast<FaceTracker *>(self);

    Mat src(height + height / 2, width, CV_8UC1, data);
//颜色格式的转换 nv21->RGBA
//将 nv21的yuv数据转成了rgba
    cvtColor(src, src, COLOR_YUV2RGBA_I420
    );
//    imwrite("/sdcard/src.jpg",src);
// 正在写的过程 退出了，导致文件丢失数据

    if (camera_id == 1) {
//前置摄像头，需要逆时针旋转90度
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE
        );
//水平翻转 镜像
        flip(src, src,
             1);
    } else {
//顺时针旋转90度
        rotate(src, src, ROTATE_90_CLOCKWISE
        );
    }


    Mat gray;
//灰色
    cvtColor(src, gray, COLOR_RGBA2GRAY
    );
//增强对比度 (直方图均衡)
    equalizeHist(gray, gray
    );
    vector<Rect2f> rects;
//送去定位
    faceTracker->
            detector(gray, rects
    );

    env->
            ReleaseByteArrayElements(data_, data,
                                     0);

    int w = src.cols;
    int h = src.rows;
    src.release();
    int ret = rects.size();
    LOGD(" ret :%d", ret);
    if (ret) {
        jclass clazz = env->FindClass(CLASS_NAME_FACE);
        jmethodID costruct = env->GetMethodID(clazz, "<init>", "(IIII[F)V");
        int size = ret * 2;
//创建java 的float 数组
        jfloatArray floatArray = env->NewFloatArray(size);
        for (
                int i = 0, j = 0;
                i < size;
                j++) {
            float f[2] = {rects[j].x, rects[j].y};
            env->
                    SetFloatArrayRegion(floatArray, i,
                                        2, f);//输出数组
            i += 2;
        }
        Rect2f faceRect = rects[0];
        int width = faceRect.width;
        int height = faceRect.height;
        jobject face = env->NewObject(clazz, costruct, width, height, w, h,
                                      floatArray);
        return face;
    }
    return 0;
}
JNIEXPORT void JNICALL
native_stop(JNIEnv
            *env,
            jobject thiz, jlong
            self) {
    if (self == 0) {
        return;
    }
    FaceTracker *me = (FaceTracker *) self;
    me->

            stopTracking();

    delete
            me;
}
JNIEXPORT jlong
JNICALL
native_init(JNIEnv *env, jobject thiz,
            jstring model_, jstring
            seeta_) {

    const char *model = env->GetStringUTFChars(model_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);

    FaceTracker *faceTracker = new FaceTracker(model, seeta);
    env->
            ReleaseStringUTFChars(model_, model
    );
    env->
            ReleaseStringUTFChars(seeta_, seeta
    );
    return reinterpret_cast
            <jlong>(faceTracker);
}

#ifdef __cplusplus
}
#endif
static JNINativeMethod gMethod[] = {
        {"native_init",     "(Ljava/lang/String;Ljava/lang/String;)J", (void *) (native_init)},
        {"native_start",    "(J)V",                                    (void *) (native_start)},
        {"native_stop",     "(J)V",                                    (void *) (native_stop)},
        {"native_detector", "(J[BIII)Lcom/example/openglfilter/face/Face;", (void *) (native_detector)},
};

static int
RegisterNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *methods, int methodNum) {
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, methodNum) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static void UnregisterNativeMethods(JNIEnv *env, const char *className) {
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        return;
    }
    if (env != NULL) {
        env->UnregisterNatives(clazz);
    }
}

extern "C" jint JNI_OnLoad(JavaVM *jvm, void *p) {
    jint jniRet = JNI_ERR;
    JNIEnv *env = NULL;
    if (jvm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return jniRet;
    }

    jint regRet = RegisterNativeMethods(env, CLASS_NAME_FACE_TRACKER, gMethod,
                                        sizeof(gMethod) /
                                        sizeof(gMethod[0]));
    if (regRet != JNI_TRUE) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

extern "C" void JNI_OnUnload(JavaVM *jvm, void *p) {
    JNIEnv *env = NULL;
    if (jvm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    UnregisterNativeMethods(env, CLASS_NAME_FACE_TRACKER);
}


