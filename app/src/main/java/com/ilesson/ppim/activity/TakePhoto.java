package com.ilesson.ppim.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.BitmapUtils;
import com.ilesson.ppim.utils.CameraP;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.ilesson.ppim.activity.AvatarActivity.RESULT_CODE;

/**
 * Created by potato on 2016/7/4.
 */
@ContentView(R.layout.activity_takephoto)
public class TakePhoto extends BaseActivity implements SurfaceHolder.Callback{

    @ViewInject(R.id.camera_preview)
    private SurfaceView surface;
    @ViewInject(R.id.photo)
    private Button takePhoto;
    @ViewInject(R.id.switch_translate)
    private Button switchBtn;
    private Camera camera;
    private CameraP mPreview;
    private File dir;

    private ImageView back, position;//返回和切换前后置摄像头
    private ImageButton shutter;//快门
    private SurfaceHolder holder;
    private String filepath = "";//照片保存路径
    private int cameraPosition = 1;//0代表前置摄像头，1代表后置摄像头
    private String fileDir;
    private boolean isIcon;
    public static final String MODIFY_ICON = "modify_icon";
    public static final String RESULT_PATH = "result_path";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        holder = surface.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        String faceVideo = Environment.getExternalStorageDirectory()+"/temp/";
        File dir = new File(faceVideo);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        fileDir = dir.getAbsolutePath();
        isIcon = getIntent().getBooleanExtra(MODIFY_ICON,false);
    }

    @Event(R.id.cancel)
    private void cancel(View v){
        finish();
    }
    @Event(R.id.photo)
    private void takePhoto(View v){
        camera.cancelAutoFocus();
        camera.autoFocus(new Camera.AutoFocusCallback() {//自动对焦
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onAutoFocus: "+success);
//                if(success) {
                    Log.d(TAG, "onAutoFocus:success ");
                    //设置参数，并拍照
                    Camera.Parameters params = camera.getParameters();
                    params.setPictureFormat(PixelFormat.JPEG);//图片格式
                    params.setPreviewSize(720, 1280);//图片大小
//                    camera.setParameters(params);//将参数设置到我的camera
                    camera.takePicture(null, null, jpeg);//将拍摄到的照片给自定义的对象
//                }
            }
        });
    }
    @Event(R.id.switch_translate)
    private void switchAct(View v){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for(int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(cameraPosition == 1) {
                //现在是后置，变更为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                        camera.setDisplayOrientation(90);
                    } catch (IOException e) {
                        Log.d(TAG, "switchAct: IOException="+e.getMessage());
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                        camera.setDisplayOrientation(90);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.d(TAG, "switchAct: IOException="+e.getMessage());
                    }
                    camera.startPreview();//开始预览
                    cameraPosition = 1;
                    break;
                }
            }

        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        //当surfaceview创建时开启相机
        if(camera == null&&holder!=null) {
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                camera.setDisplayOrientation(90);
                camera.startPreview();//开始预览
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //当surfaceview关闭时，关闭预览并释放资源
        camera.stopPreview();
        camera.release();
        camera = null;
        holder = null;
        surface = null;
    }

    //创建jpeg图片回调数据对象
    Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if(bitmap.getWidth()>bitmap.getHeight()){
                    if(cameraPosition>0)
                    bitmap = rotateBitmapByDegree(bitmap,90);
                    else bitmap = rotateBitmapByDegree(bitmap,-90);
                }
                bitmap = BitmapUtils.compressImages(bitmap);
                filepath = fileDir+File.pathSeparator+System.currentTimeMillis()+".jpg";
                File file = new File(fileDir,System.currentTimeMillis()+".jpg");
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩的流里面
                bos.flush();// 刷新此缓冲区的输出流
                bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
                camera.stopPreview();//关闭预览 处理数据
                camera.startPreview();//数据处理完后继续开始预览
                bitmap.recycle();//回收bitmap空间
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(),
                            file.getAbsolutePath(), file.getName(), null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //最后通知图库更新
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//扫描单个文件
                intent.setData(Uri.fromFile(file));//给图片的绝对路径
//                sendBroadcast(intent);
                Log.d(TAG, "onPictureTaken: "+file.getAbsolutePath());
                intent = new Intent();
                intent.putExtra(RESULT_PATH,file.getAbsolutePath());
                setResult(RESULT_CODE,intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static final String TAG = "TakePhoto";
    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Log.d(TAG, "rotateBitmapByDegree: "+degree);
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "rotateBitmapByDegree: "+e.getMessage());
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
