package com.example.yuvopengldemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private YuvPlayer yuvPlayer = null;
    private String dstFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkSelfPermission(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 200);
        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        String src_path = Environment.getExternalStorageDirectory().getPath();
        String srcFile = src_path + "/video1_640_272.yuv";
        String dst_path = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            dst_path = getExternalFilesDir(null).getAbsolutePath();
        } else {
            dst_path = Environment.getExternalStorageDirectory().getPath();
        }
        dstFile = dst_path + "/video1_640_272.yuv";

        yuvPlayer = new YuvPlayer(this, null);
        yuvPlayer.setFilePath(dstFile);
    }

    private void checkSelfPermission(String[] permissions, int requestCode) {
        List<String> temp = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                temp.add(permission);
            }
        }

        if (ContextCompat.checkSelfPermission(this, String.valueOf(temp.toArray())) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, permissions, requestCode);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
