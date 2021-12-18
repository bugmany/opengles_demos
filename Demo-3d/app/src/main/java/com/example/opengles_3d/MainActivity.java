package com.example.opengles_3d;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private final static String TAG = "Demo-3d";

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "  vColor = aColor;"+
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    //立方体模型
    //          反面
    //     4----------7
    //   / |         /|
    // 0---|-------3  |
    // |   |       |  |
    // |   5-------|--6
    // | /         |/
    // 1-----------2
    //     正面
    //立方体的八个顶点
    final float cubePositions[] = {
            -1.0f, 1.0f,  1.0f,       //正面左上0
            -1.0f, -1.0f, 1.0f,       //   左下1
            1.0f,  -1.0f, 1.0f,       //   右下2
            1.0f,  1.0f,  1.0f,       //   右上3
            -1.0f, 1.0f,  -1.0f,      //反面左上4
            -1.0f, -1.0f, -1.0f,      //   左下5
            1.0f,  -1.0f, -1.0f,      //   右下6
            1.0f,  1.0f,  -1.0f,      //   右上7
    };

    //立方体的六个面，每个面有两个三角形组成
    final short cubeIndex[]={
            0,7,3,            //上面
            0,4,7,            //
            0,3,2,            //正面
            0,2,1,            //
            6,5,1,            //下面
            6,1,2,            //
            6,7,4,            //后面
            6,4,5,            //
            0,1,5,            //左面
            0,5,4,            //
            6,3,7,            //右面
            6,2,3,            //
    };

    //八个顶点颜色：r,g,b,a
    float cubeColor[] = {
            1f,0f,0f,1f,        //红色
            0f,1f,0f,1f,        //绿色
            0f,0f,1f,1f,        //蓝色
            1f,1f,0f,1f,        //黄色
            1f,0f,1f,1f,
            0f,1f,1f,1f,
            0f,0f,0f,1f,        //黑色
            1f,1f,1f,1f,        //白色
    };

    private int mPositionHandle;
    private int mColorHandle;
    private int mProgram;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer colorBuffer;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mMatrixHandler;

    final int COORDS_PER_VERTEX = 3;
    //顶点个数
    private final int vertexCount = cubePositions.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    private GLSurfaceView mSurfaceView = null;

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

        //将坐标数据、颜色数据分别写入到独自的FloatBuffer中，将索引数据写入到ShortBuffer中
        ByteBuffer bb = ByteBuffer.allocateDirect(cubePositions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        ByteBuffer cc= ByteBuffer.allocateDirect(cubeIndex.length * 2);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer=cc.asShortBuffer();
        indexBuffer.put(cubeIndex);
        indexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(cubeColor.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(cubeColor);
        colorBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //将顶点着色器和片元着色器加入到程序中，并链接程序
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {

        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 5.0f, 5.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //写入变换矩阵、顶点坐标数据及颜色数据
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler= GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 2, cubeColor, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle,4, GLES20.GL_FLOAT,false, 0, colorBuffer);
        //索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeIndex.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }

    public static int loadShader(int shaderType, String source){
        int shader= GLES20.glCreateShader(shaderType);
        if(0 != shader){
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);
            int[] compiled=new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compiled,0);
            if(compiled[0] == 0){
//                glError(1,"Could not compile shader: "+shaderType);
//                glError(1,"GLES20 Error:"+ GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
}