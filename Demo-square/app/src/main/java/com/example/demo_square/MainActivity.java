package com.example.demo_square;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    private static final String TAG = "Demo-Square";

    private static final int COORDS_PER_VERTEX = 3;

    private GLSurfaceView mSurfaceView = null;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;"+
            "attribute vec4 aColor;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_Position = vMatrix * vPosition;" +
            "  vColor = aColor;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    //顶点坐标
    private float triangleCoords[] = {
            -0.5f,  0.5f,   0.0f,     // top left
            -0.5f,  -0.5f,  0.0f,     // bottom left
            0.5f,   -0.5f,  0.0f,      // bottom right
            0.5f,   0.5f,   0.0f       // top right
    };

    //圆形顶点坐标
    private float circularCoords[];

    //设置颜色，依次为红绿蓝和透明通道
    private float color[] = {
            0.0f, 0.0f, 1.0f, 1.0f, // top right
            0.0f, 1.0f, 0.0f, 1.0f, // bottom left
            0.0f, 0.0f, 1.0f, 1.0f, // top left
            1.0f, 0.0f, 0.0f, 1.0f, // bottom right
    };

    //顶点索引
    private short index[]={
            0,1,2,
            0,2,3
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //变换矩阵
    private final float[] mMVPMatrix = new float[16];

    //位置
    private int aPoisitionLocation;
    //颜色
    private int aColorLocation;
    //变换矩阵
    private int uMatrixLocation;

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

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //开启深度测试，绘制平面图时不需要开启，否则看不到图像
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.0f, 0.5f, 0.5f, 1.0f);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int[] shaderId = {vertexShader, fragmentShader};
        int link = linkProgram(shaderId);
        if(link == 0) {

        }

        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        //获取变换矩阵vMatrix成员句柄
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //获取顶点着色器的vPosition成员句柄
        aPoisitionLocation = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //获取片元着色器的vColor成员的句柄
        aColorLocation = GLES20.glGetAttribLocation(mProgram, "aColor");

        //矩形
//        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
//        bb.order(ByteOrder.nativeOrder());
//        vertexBuffer = bb.asFloatBuffer();
//        vertexBuffer.put(triangleCoords);
//        vertexBuffer.position(0);

        createPositions(1,6);  //60 - 圆形，6 - 六边形

        //圆
        ByteBuffer bb = ByteBuffer.allocateDirect(circularCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(circularCoords);
        vertexBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 4);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置绘制窗口
        GLES20.glViewport(0, 0, width, height);

        //计算宽高比
        float ratio = (float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        /*//正交投影方式
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            //横屏
            Matrix.orthoM(mMVPMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            //竖屏
            Matrix.orthoM(mMVPMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }*/
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //设置当前背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(aPoisitionLocation, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(aPoisitionLocation);

//        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(aColorLocation, 1, color, 0);
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        //绘制三角形
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        //索引法绘制正方形
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        //绘制圆形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, circularCoords.length/3);

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(aPoisitionLocation);
        GLES20.glDisableVertexAttribArray(aColorLocation);
    }

    public int loadShader(int shaderType, String shaderResource) {
        int shader = GLES20.glCreateShader(shaderType);
        if(shader != 0) {
            GLES20.glShaderSource(shader, shaderResource);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0) {
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        return shader;
    }

    public int linkProgram(int[] shaderId) {
        int link = 0;
        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        for (int i = 0; i < shaderId.length; i++) {
            GLES20.glAttachShader(mProgram, shaderId[i]);
        }
        int[] linked = new int[1];
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0);
        if(linked[0] == 0) {
            GLES20.glDeleteShader(mProgram);
        }
        link = linked[0];

        return link;
    }

    /**
     * 绘制圆形
     * @param radius - 半径
     * @param n      - 三角形个数
     */
    private void createPositions(int radius, int n){
        ArrayList<Float> data = new ArrayList<>();
        //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        data.add(0.0f);
        float angDegSpan = 360f/n;
        for(float i = 0; i < 360+angDegSpan; i += angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i<f.length; i++){
            f[i] = data.get(i);
        }

        circularCoords = f;

//        //处理各个顶点的颜色
//        color = new float[f.length*4/3];
//        ArrayList<Float> tempC = new ArrayList<>();
//        ArrayList<Float> totalC = new ArrayList<>();
//        tempC.add(1.0f);
//        tempC.add(0.0f);
//        tempC.add(0.0f);
//        tempC.add(1.0f);
//        for (int i=0;i<f.length/3;i++){
//            totalC.addAll(tempC);
//        }
//
//        for (int i=0; i<totalC.size();i++){
//            color[i] = totalC.get(i);
//        }
    }
}