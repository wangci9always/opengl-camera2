package com.example.openglfilter.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class OpenGlUtils {
    private static final SimpleDateFormat DateTime_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
    private static final String RESULT_IMG_DIR = "OpenGLCamera";

    public static String readRawShaderFile(Context context, int shareId) {
        InputStream is = context.getResources().openRawResource(shareId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        StringBuffer sb = new StringBuffer();
        try {

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static int loadProgram(String mVertexShader, String mFragShader) {
        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        GLES20.glShaderSource(vshader, mVertexShader);

        GLES20.glCompileShader(vshader);

        int[] status = new int[1];

        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("load vertex raw error :" + GLES20.glGetShaderInfoLog(vshader));
        }


        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        GLES20.glShaderSource(fshader, mFragShader);

        GLES20.glCompileShader(fshader);


        GLES20.glGetShaderiv(fshader, GLES20.GL_SHADER_COMPILER, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("load fragment raw error :" + GLES20.glGetShaderInfoLog(fshader));
        }


        int programeId = GLES20.glCreateProgram();

        GLES20.glAttachShader(programeId, vshader);
        GLES20.glAttachShader(programeId, fshader);

        GLES20.glLinkProgram(programeId);

        GLES20.glGetProgramiv(programeId, GLES20.GL_LINK_STATUS, status, 0);


        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("link program:" + GLES20.glGetProgramInfoLog(programeId));
        }

        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);

        return programeId;

    }

    public static void glGenTextures(int[] textures) {
        GLES20.glGenTextures(textures.length, textures, 0);


        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);


            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);


            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        }
    }


    public static void copyAssets2SdCard(Context context, String src, String dst) {
        try {

            File file = new File(dst);
            if (!file.exists()) {
                InputStream is = context.getAssets().open(src);
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Bitmap createBitmapFromGLSurface(int x, int y, int w, int h) {
        ByteBuffer buffer = ByteBuffer.allocate(w * h * 4);
        GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Matrix matrix = new Matrix();
        matrix.setRotate(180);
        matrix.postScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);
    }
    public static void saveToLocal(final Context context, Bitmap bitmap, final String imgPath) {
        File file = new File(imgPath);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                Log.i("lcx", "saveToLocal: "+imgPath);
                if(null!=context&&context instanceof Activity){
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"截图成功，保存路径："+imgPath,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static File getResultImgFile(final String ext) {
        final File dir = new File(Environment.getExternalStorageDirectory(), RESULT_IMG_DIR);
        Log.d("lcx", "path=" + dir.toString());
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, getDateTimeString() + ext);
        }
        return null;
    }

    private static String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return DateTime_FORMAT.format(now.getTime());
    }
}
