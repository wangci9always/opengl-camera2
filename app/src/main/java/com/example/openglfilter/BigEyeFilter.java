package com.example.openglfilter;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BigEyeFilter extends AbstractFboFilter{
    private final int left_eye;
    private final int right_eye;
    private final FloatBuffer left;
    private final FloatBuffer right;
    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.bigeye_frag);
        left_eye = GLES20.glGetUniformLocation(program, "left_eye");
        right_eye = GLES20.glGetUniformLocation(program, "right_eye");


        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Override
    public void beforeDraw() {
        super.beforeDraw();

        /**
         * 传递眼睛的坐标 给GLSL
         */
        if(null!=mFace){
            float[] landmarks = mFace.landmarks;
            //左眼的x 、y  opengl : 0-1
            float x = landmarks[2] / mFace.width;
            float y = landmarks[3] / mFace.height;

            left.clear();
            left.put(x);
            left.put(y);
            left.position(0);
            GLES20.glUniform2fv(left_eye, 1, left);

            //右眼的x、y
            x = landmarks[4] / mFace.width;
            y = landmarks[5] / mFace.height;
            right.clear();
            right.put(x);
            right.put(y);
            right.position(0);
            GLES20.glUniform2fv(right_eye, 1, right);
        }

    }
}
