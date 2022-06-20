package com.example.openglfilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.openglfilter.util.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class StickerFilter extends AbstractFboFilter {
    private int[] mTextureId;
    private final Bitmap mBitmap;
    private final Bitmap mBitmap2;
    //贴图纹理
    float[] TEXTURE = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };
    FloatBuffer textureBuffer; // 纹理坐标

    public StickerFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.base_frag);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_3d_fore);
        mBitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.erduo_000);
        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.clear();
        textureBuffer.put(TEXTURE);
    }


    @Override
    public void setSize(int width, int height,int x,int y) {
        super.setSize(width, height,x,y);
        mTextureId = new int[2];
        OpenGlUtils.glGenTextures(mTextureId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[1]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap2, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }

    @Override
    public int onDraw(int texture) {
        super.onDraw(texture);
        drawSticker();
        if (null != mFace)
            drawSticker2();
        return frameTextures[0];
    }

    private void drawSticker2() {
        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);

        //GLES20.glBlendFunc设置贴图模式
        // 1：src 源图因子 ： 要画的是源  (耳朵)
        // 2: dst : 已经画好的是目标  (从其他filter来的图像)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float x = mFace.landmarks[0];
        float y = mFace.landmarks[1];
//Face{landmarks=[418.0, 410.0, 556.9173, 579.1615, 797.8345, 569.90515, 689.6033, 708.6788, 597.9933, 838.4592, 783.7029, 826.33887],
// width=1200, height=1600, faceWidth=498, faceHeight=498}
        //这里的坐标是相对于 传入opencv识别的图像的像素，需要转换为在屏幕的位置
        x = x / mFace.width * mWidth;
        y = y / mFace.height * mHeight;
        Log.i("TAG", "drawSticker2: x:"+x+"---y:"+y);
//            要绘制的位置和大小，贴纸是画在耳朵上的，直接锁定人脸坐标就可以
        GLES20.glViewport((int) x, (int) y - mBitmap.getHeight(), (int) ((float) mFace.faceWidth / mFace.width * mWidth), mBitmap.getHeight());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);

        GLES20.glUseProgram(program);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[1]);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);

    }

    private void drawSticker() {
        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);

        //GLES20.glBlendFunc设置贴图模式
        // 1：src 源图因子 ： 要画的是源  (耳朵)
        // 2: dst : 已经画好的是目标  (从其他filter来的图像)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glViewport(0, 0, mBitmap.getWidth() / 4, mBitmap.getHeight() / 4);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);

        GLES20.glUseProgram(program);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLES20.glUniform1i(vTexture, 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void release() {
        super.release();
        mBitmap.recycle();
    }
}
