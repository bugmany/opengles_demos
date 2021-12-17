package com.example.opengles_video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, GLSurfaceView.Renderer {

    private GLSurfaceView mSurfaceView = null;
    private int mTextureid = 0;
    private SurfaceTexture mTexture = null;

    private String mCameraId;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraSession;
    private CaptureRequest mCaptureRequest;
    private CaptureRequest.Builder mCaptureBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraManager = (CameraManager)this.getSystemService(CAMERA_SERVICE);
        for(String camid : mCameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(camid);
            if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK)) {
                continue;
            }
            mCameraId = camid;
        }

        mSurfaceView = findViewById(R.id.glSurfaceView);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int[] textureid = new int[1];
        GLES20.glGenTextures(1, textureid, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureid[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        mTextureid = textureid[0];
        mTexture = new SurfaceTexture(mTextureid);
        mTexture.setOnFrameAvailableListener(this);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        mTexture.updateTexImage();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    public void openCamera() {
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        mCameraManager.openCamera(mCameraId, mCameraStateCB, null);
    }

    public CameraDevice.StateCallback mCameraStateCB = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;

            SurfaceTexture surfaceTexture =
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };

    public CameraCaptureSession.StateCallback mCameraCaptureCB = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }
    }
}