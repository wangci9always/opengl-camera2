package com.example.openglfilter;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.openglfilter.util.CommonUtil;
import com.example.openglfilter.util.OpenGlUtils;
import com.example.openglfilter.widget.RecordButton;


public class MainActivity extends AppCompatActivity implements RecordButton.OnRecordListener, RadioGroup.OnCheckedChangeListener {

    private CameraView cameraView;
    private CheckBox beauty, filter, effect, effect2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPermission();
        //拷贝 模型
        OpenGlUtils.copyAssets2SdCard(this, "lbpcascade_frontalface_improved.xml",
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/lbpcascade_frontalface.xml");
        OpenGlUtils.copyAssets2SdCard(this, "seeta_fa_v1.1.bin",
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/seeta_fa_v1.1.bin");
    }


    private void initView() {
        cameraView = findViewById(R.id.cameraView);
        beauty = findViewById(R.id.open_beauty);
        filter = findViewById(R.id.open_filter);
        effect = findViewById(R.id.open_effect);
        effect2 = findViewById(R.id.open_effect2);
        RecordButton btn_record = findViewById(R.id.btn_record);
        btn_record.setOnRecordListener(this);

        //速度
        RadioGroup rgSpeed = findViewById(R.id.rg_speed);
        rgSpeed.setOnCheckedChangeListener(this);
        beauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraView.openBeauty(isChecked);
            }
        });
        filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraView.openFilter(isChecked);
            }
        });
        effect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraView.openEffect(isChecked);
            }
        });
        effect2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraView.openEffect2(isChecked);
            }
        });
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 1);

        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.btn_extra_slow:
                cameraView.setSpeed(CameraView.Speed.MODE_EXTRA_SLOW);
                break;
            case R.id.btn_slow:
                cameraView.setSpeed(CameraView.Speed.MODE_SLOW);
                break;
            case R.id.btn_normal:
                cameraView.setSpeed(CameraView.Speed.MODE_NORMAL);
                break;
            case R.id.btn_fast:
                cameraView.setSpeed(CameraView.Speed.MODE_FAST);
                break;
            case R.id.btn_extra_fast:
                cameraView.setSpeed(CameraView.Speed.MODE_EXTRA_FAST);
                break;
        }
    }

    @Override
    public void onRecordStart() {
        cameraView.startRecord();
    }

    @Override
    public void onRecordStop() {
        Log.i("tuch", "onRecordStop: ----------------->");
        cameraView.stopRecord();
    }


    public void toSavePic(View view) {
        if (CommonUtil.isFastClick()) {
            Toast.makeText(this, "点击太频繁啦,请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraView.saveScreenPic(true);
    }
}
