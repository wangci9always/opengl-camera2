package com.example.openglfilter;

import android.content.Context;

public class ColorFilter extends AbstractFboFilter{
    public ColorFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.camera_frag5);
    }

    public int onDraw(int texture) {
        // fbo-> new texture  美颜 ps    ps   ps ---》美颜  步骤   ps
        super.onDraw(texture);
        return frameTextures[0];
    }
}
