package com.example.myopenglesdemo2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    private final static String TAG = "Demo1";
    private GLSurfaceView surfaceView = null;

    private static final String vertexShaderResouorce =
            "attribute vec3 vPosition;" +
                    "void main() {" +
                    "   gl_Position = vec4(vPosition.x, vPosition.y, vPosition.z, 1.0);" +
                    "}";

    private final float[] vertexCoords = new float[] {
            0.0f, 0.5f, 0.0f,   //顶点
            -0.5f, -0.5f, 0.0f, //左下
            0.5f, -0.5f, 0.0f   //右下
    };

    private static final String fragmentShaderResource =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}";

    private final float color[] = {
            1.0f, 0.0f, 0.0f, 1.0f
            };

    private int vertexProgram;

    private FloatBuffer fragmentFloatBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.glSurfaceView);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
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
        //设置清空屏幕后的背景色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        //构建顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderResouorce);

        //构建片段着色器
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderResource);

        //构建着色器程序，并将顶点着色器和片段着色器链接进来
        vertexProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(vertexProgram, vertexShader);
        GLES20.glAttachShader(vertexProgram,fragmentShader);

        int[] link = new int[1];
        GLES20.glLinkProgram(vertexProgram);
        GLES20.glGetProgramiv(vertexProgram, GLES20.GL_LINK_STATUS, link, 0);
        if(link[0] == 0) {

        }

        //顶点着色器和片段着色器链接到着色器程序后就无用了
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        //转换为需要的顶点数据格式
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length*4);
        buffer.order(ByteOrder.nativeOrder());
        fragmentFloatBuffer = buffer.asFloatBuffer();
        fragmentFloatBuffer.put(vertexCoords);
        fragmentFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //设置背景色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        //清空屏幕，擦除屏幕上所有的颜色，用 glClearColor 定义的颜色填充
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //在当前 EGL 环境激活着色器程序
        GLES20.glUseProgram(vertexProgram);

        //获取顶点着色器的 vPosition 成员句柄
        int positionHandle = GLES20.glGetAttribLocation(vertexProgram, "vPosition");
        //启用句柄
        GLES20.glEnableVertexAttribArray(positionHandle);
        //设置顶点坐标数据
//        GLES20.glVertexAttribIPointer(positionHandle, 3, GLES20.GL_FLOAT, 3 * 4, fragmentFloatBuffer);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, fragmentFloatBuffer);

        //获取片元着色器的 vColor 成员句柄
        int colorHandle = GLES20.glGetUniformLocation(vertexProgram, "vColor");
        //设置颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteProgram(vertexProgram);
    }

    public int loadShader(int shaderType, String source){
        int shader = GLES20.glCreateShader(shaderType);
        if(0 != shader){
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0){
                Log.d(TAG, "loadShader: Could not compile shader:"+shaderType);
                Log.d(TAG, "loadShader: "+ GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
}