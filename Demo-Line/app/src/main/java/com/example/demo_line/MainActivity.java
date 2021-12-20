package com.example.demo_line;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer{
    private final static String TAG = "Demo-line";

    private static String vertexShader =
            "attribute vec4 vPosition;" +
            "attribute vec4 aColor;"    +
            "varying vec4 vColor;" +
            "void main() {" +
            "    gl_Position = vPosition;" +
            "    gl_PointSize = 10.0;" +
            "    vColor = aColor;" +
            "}";

    private static String fragmentShader =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "    gl_FragColor = vColor;" +
            "}";

    private GLSurfaceView mSurfaceView = null;

    private float[] vertexCoords = new float[]{
            0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
    };

    private float[] vertexColor = new float[] {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    private short[] vertexIndex = new short[] {
            0, 1, 2
    };

    private FloatBuffer coordsBuffer = null;
    private FloatBuffer colorBuffer = null;
    private int mProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private int loadShader(int type, String shaderRes) {
        int shader = GLES20.glCreateShader(type);
        if(shader != 0) {
            GLES20.glShaderSource(shader, shaderRes);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0) {
                Log.d(TAG, "loadShader: compile failed");
                GLES20.glDeleteShader(shader);
            }
        }
        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);

//        String vertexShaderRes = getAssets().open("vertexShader.glsl").toString();
        int vShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);

//        String fragmentShaderRes = getAssets().open("fregmentShader.glsl").toString();
        int fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);

        int[] linked = new int[1];
        GLES20.glLinkProgram(mProgram);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0);
        if(linked[0] == 0) {

        }
        GLES20.glUseProgram(mProgram);

        GLES20.glDeleteShader(vShader);
        GLES20.glDeleteShader(fShader);

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        coordsBuffer = bb.asFloatBuffer();
        coordsBuffer.put(vertexCoords);
        coordsBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(vertexColor.length * 4);
        cc.order(ByteOrder.nativeOrder());
        colorBuffer = cc.asFloatBuffer();
        colorBuffer.put(vertexColor);
        colorBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, coordsBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, vertexColor, 0);
//        GLES20.glVertexAttribPointer(1, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
//        GLES20.glEnableVertexAttribArray(1);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 3);

//        GLES20.glLineWidth(10);
//        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, 3);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
