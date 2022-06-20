package com.example.openglfilter.face;

import java.util.Arrays;

public class Face {
    //左眼中心、右眼中心、鼻尖、左嘴角和右嘴角。
    public float[] landmarks;
    // 保存人脸的宽、高
    public int faceWidth;
    public int faceHeight;

    //送去检测图片的宽、高
    public int width;
    public int height;

    Face(int faceWidth, int faceHeight, int width, int height, float[] landmarks) {
        this.width = width;
        this.height = height;
        this.faceWidth = faceWidth;
        this.faceHeight = faceHeight;
        this.landmarks = landmarks;
    }

    @Override
    public String toString() {
        return "Face{" +
                "landmarks=" + Arrays.toString(landmarks) +
                ", width=" + width +
                ", height=" + height +
                ", faceWidth=" + faceWidth +
                ", faceHeight=" + faceHeight +
                '}';
    }
}
