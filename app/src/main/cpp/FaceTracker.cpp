//
// Created by Administrator on 2022/6/19.
//
/**
 *
 * https://github.com/seetaface/SeetaFaceEngine
 *
 * 中科人脸检测开源库seeta:
人脸检测 FaceDetection
人脸校准 FaceAlignment
人脸识别 FaceIdentification
 */
#include "FaceTracker.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "native-lib", __VA_ARGS__)

FaceTracker::FaceTracker(const char *model, const char *seeta) {
    //人脸检测
    Ptr <CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    //人脸追踪
    Ptr <CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    DetectionBasedTracker::Parameters detectorParams;
    //追踪器
    tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, detectorParams);
    //初始化seeta
    faceAlignment = makePtr<seeta::FaceAlignment>(seeta);
}

void FaceTracker::detector(Mat src, vector <Rect2f> &rects) {

    vector <Rect> faces;
    //检测人脸
    tracker->process(src);
    //拿到人脸坐标信息
    tracker->getObjects(faces);


    if (faces.size()) {
        LOGD(" face num :%d", faces.size());
        Rect face = faces[0];//这里只处理一个人脸，多人脸逻辑没处理
        rects.push_back(Rect2f(face.x, face.y, face.width, face.height));

//根据前面的opencv人脸结果 做人脸关键点的定位
        seeta::ImageData imageData(src.cols, src.rows);//imageData就是图像数据
        imageData.data = src.data;
        seeta::FaceInfo faceInfo;//要送去检测的人脸信息
        seeta::Rect bbox;//定义个人脸框的信息对象,并将opencv采集出来的数据送给seeta的对象
        //给这个对象赋值
        bbox.x = face.x;
        bbox.y = face.y;
        bbox.width = face.width;
        bbox.height = face.height;
//把人脸区域的图像给faceInfo对象
        faceInfo.bbox = bbox;

        //定义seeta 可以检测五个坐标点（左眼中心、右眼中心、鼻尖、左嘴角和右嘴角。）https://zhuanlan.zhihu.com/p/88438246?utm_source=cn.ticktick.task
        seeta::FacialLandmark points[5];
        //数据交给seeta采集出人脸特征值
        faceAlignment->PointDetectLandmarks(imageData, faceInfo, points);

        for (int i = 0; i < 5; ++i) {
            //保存关键点坐标值
            rects.push_back(Rect2f(points[i].x, points[i].y, 0, 0));
        }

    }

}

void FaceTracker::startTracking() {
    tracker->run();
}

void FaceTracker::stopTracking() {
    tracker->stop();
}
