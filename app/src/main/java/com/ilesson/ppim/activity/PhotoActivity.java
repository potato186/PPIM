package com.ilesson.ppim.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.LocalMedia;
import com.ilesson.ppim.entity.ScanResult;
import com.ilesson.ppim.utils.BitmapUtils;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.FileTool;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.view.MenuGridView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

import static com.ilesson.ppim.activity.ComposeActivity.REQUEST_CODE_PIKE_PICTURE;
import static com.ilesson.ppim.activity.ComposeActivity.REQUEST_CODE_TAKE_PICTURE;
import static com.ilesson.ppim.activity.ComposeActivity.REQUEST_IDENTIFY_PICTURE;
import static com.ilesson.ppim.activity.ImagePreviewActivity.REQUEST_PREVIEW;
import static com.ilesson.ppim.activity.ImageSelectorActivity.EXTRA_ALREADY_NUM;
import static com.ilesson.ppim.activity.TakePhoto.RESULT_PATH;


/**
 * Created by potato on 2019/5/8.
 */

public class PhotoActivity extends BaseActivity implements OnClickListener {

    private MenuGridView mGridView;
    private int itemSize;
    private List<LocalMedia> photos;
    public static final int SHOW_IMAGE = 32;
    private static final String TAG = "PhotoActivity";
    public static final String IMAGE_URL = "image_url";
    public static final String CONTENT_RESULT = "content_result";
    public static final String TITLE_RESULT = "title_result";
    public static final int IMAGE_RESULT = 33;
    private PublicShowPicAdapter mAdapter;
    private View mStartBtn;
    private StringBuilder mStringBuilder;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_photo);
        setStatusBarLightMode(this,true);
        int width = PPScreenUtils.getScreenWidth(this);
        itemSize = (int) (width * 0.27);
        mGridView = findViewById(R.id.pic_grid);
        photos = new ArrayList<>();
//        List<LocalMedia> datas = new ArrayList<>();
//        String url = getIntent().getStringExtra(PhotoActivity.IMAGE_URL);
        List<LocalMedia> datas = (ArrayList<LocalMedia>) getIntent().getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
//        if (!TextUtils.isEmpty(url)) {
//            datas.add(new LocalMedia(url));
//        }
        mAdapter = new PublicShowPicAdapter();
        mGridView.setAdapter(mAdapter);
        setData(datas, true);
        findViewById(R.id.back_btn).setOnClickListener(this);
        mStartBtn = findViewById(R.id.start);
        mStartBtn.setOnClickListener(this);
    }

    private void setData(List<LocalMedia> images, boolean clear) {
        if (clear) {
            photos.clear();
        } else {
            photos.remove(photos.size() - 1);
        }
        photos.addAll(images);
        photos.add(new LocalMedia(""));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.start:
                mStringBuilder = new StringBuilder();
                mStartBtn.setEnabled(false);
                mIndex = 0;
                title = null;
                String path = getData();
                if(TextUtils.isEmpty(path)){
                    Toast.makeText(this,"获取图片失败",Toast.LENGTH_LONG).show();
                    return;
                }
                loadData(path);
                break;
        }
    }

    private int mIndex;

    private String getData() {
        String path = photos.get(mIndex).getPath();
        if(TextUtils.isEmpty(path)){
            return null;
        }
        Log.d(TAG, "getData: pic="+path);
        showProgress();
        if (BitmapUtils.needScale(path)) {
            Log.d(TAG, "getData: before=" + BitmapUtils.getBitmapSize(BitmapFactory.decodeFile(path)));
            Bitmap bitmap = BitmapUtils.getSmallBitmap(path);
            Log.d(TAG, "getData: getBitmapSize=" + BitmapUtils.getBitmapSize(bitmap));
            String res = BitmapUtils.bitmapToString(bitmap);
            return res;
        }
        String readed = FileTool.getImageStr(path);
        Log.d(TAG, "getData: 相册=" + BitmapUtils.getBitmapSize(BitmapFactory.decodeFile(path)));
        return readed;
    }

    String title = null;
    private void loadData(String data) {
        RequestParams params = new RequestParams(Constants.OCR_BASE + Constants.OCR_URL);
        params.addParameter("image", data);
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                hideProgress();
                ScanResult base = new Gson().fromJson(
                        result,
                        new TypeToken<ScanResult>() {
                        }.getType());
                if (base.getCode() != 0) {
                    mHandler.sendEmptyMessage(0);
                    return;
                }
                String res = base.getData();
                if(res.contains("\n")){
                    String tag = res.substring(0,res.indexOf("\n"));
                    if(tag.length()<9&&mIndex==0){
                        title = tag;
                        res = res.replace(tag,"");
                    }
                }
                mStringBuilder.append(res.replace("\n","\n\t\t\t\t").replace(",","，")
                        .replace(".","。").replace("?","？").replace("!","！")
                        .replace(":","：").replace("-","——"));
                mIndex++;
                if (mIndex < photos.size() - 1) {
                    loadData(getData());
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(PhotoActivity.CONTENT_RESULT, mStringBuilder.toString());
                    intent.putExtra(PhotoActivity.TITLE_RESULT, title);
                    setResult(REQUEST_IDENTIFY_PICTURE, intent);
                    finish();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mStartBtn.setEnabled(true);
            Toast.makeText(PhotoActivity.this, R.string.identify_fail, Toast.LENGTH_LONG).show();
        }
    };

    class PublicShowPicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int position) {
            return photos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            PicViewHolder holder = null;
            if (null == convertView) {
                holder = new PicViewHolder();
                convertView = getLayoutInflater().inflate(
                        R.layout.pic_item, null);
                holder.img = (ImageView) convertView
                        .findViewById(R.id.pic_imageview);
                convertView.setTag(holder);
            } else {
                holder = (PicViewHolder) convertView.getTag();
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.width = itemSize;
            layoutParams.height = itemSize;
            holder.img.setLayoutParams(layoutParams);
            if (parent.getChildCount() == photos.size() - 1) {
                holder.img.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
                holder.img.setBackgroundResource(R.drawable.add_pic_selector);
            } else {
                String url = photos.get(position).getPath();
                if(null!=url){
                    if (url.startsWith("content")) {
                        Glide.with(parent.getContext()).load(Uri.parse(url))
//                                .override(itemSize, itemSize)
                                .into(holder.img);
                    } else {
                        Glide.with(parent.getContext()).load(url)
//                                .override(itemSize, itemSize)
                                .into(holder.img);
                    }
                }
            }
            holder.img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPos = position;
                    requestPm();
                    Log.d(TAG, "getView onClick: ");

                }
            });
            return convertView;
        }

    }

    class PicViewHolder {
        private ImageView img;
    }

    private int currentPos = -1;

    public void requestPm() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    opreaImage(currentPos);
                } else {
                    //只要有一个权限被拒绝，就会执行
                    Toast.makeText(PhotoActivity.this, R.string.permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void opreaImage(int currentPos) {
        if (currentPos < photos.size() - 1) {
            List<LocalMedia> list = new ArrayList<LocalMedia>();
            list.addAll(photos);
            list.remove(photos.size() - 1);
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.EXTRA_PREVIEW_LIST, (ArrayList) list);
            intent.putExtra(ImagePreviewActivity.EXTRA_PREVIEW_SELECT_LIST, (ArrayList) list);
            intent.putExtra(ImagePreviewActivity.EXTRA_POSITION, currentPos);
            intent.putExtra(ImagePreviewActivity.EXTRA_MAX_SELECT_NUM, 3);
            startActivityForResult(intent, REQUEST_PREVIEW);
        } else {
//            if (photos.size() >= 4) {
//                showToast("最多可上传"+MAX_PIC_NUM+"张图片");
//                return;
//            }
            showPhotoDialog();
        }
    }

    private void showPhotoDialog() {
        if (mPhotoDialog == null) {
            initPhotoDialog();
        }
        mPhotoDialog.show();
    }

    /**
     * 初始化分享弹出框
     */
    private Dialog mPhotoDialog;

    public static final String PHOTO_PATH = "photo";
    private Uri mImageUri;

    private void initPhotoDialog() {
        mPhotoDialog = new Dialog(this);
        mPhotoDialog.setCanceledOnTouchOutside(true);
        mPhotoDialog.setCancelable(true);
        Window window = mPhotoDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        View view = View.inflate(this, R.layout.select_pic, null);
        view.findViewById(R.id.take_photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoDialog.dismiss();
//                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                String f = System.currentTimeMillis()+".jpg";
//                mImageUri = Uri.fromFile(new File(FileTool.getDir(ComposeActivity.this,PHOTO_PATH)+f));
//                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); //指定图片存放位置，指定后，在onActivityResult里得到的Data将为null
//                startActivityForResult(openCameraIntent, REQUEST_CODE_TAKE_PICTURE);
                Intent intent = new Intent(PhotoActivity.this, TakePhoto.class);
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
            }
        });
        view.findViewById(R.id.pike_photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoDialog.dismiss();
                Intent intent = new Intent(PhotoActivity.this, ImageSelectorActivity.class);
                intent.putExtra(EXTRA_ALREADY_NUM, 0);
                startActivityForResult(intent, REQUEST_CODE_PIKE_PICTURE);
//                Intent intent = new Intent(this, ImageSelectorActivity.class);
//                intent.putExtra(EXTRA_ALREADY_NUM, photos.size() - 1);
//                startActivityForResult(intent, SHOW_IMAGE);
//                Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
//                // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
//                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intentToPickPic, REQUEST_CODE_PIKE_PICTURE);
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            String url = data.getStringExtra(RESULT_PATH);
            if (TextUtils.isEmpty(url)) {
                return;
            }
            List<LocalMedia> datas = new ArrayList<>();
            datas.add(new LocalMedia(url));
            setData(datas, false);
        }else if(requestCode == REQUEST_CODE_PIKE_PICTURE) {
            List<LocalMedia> datas = (ArrayList<LocalMedia>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
            setData(datas, false);
        }else if (requestCode == REQUEST_PREVIEW) {
            List<LocalMedia> images = (List<LocalMedia>) data.getSerializableExtra(ImagePreviewActivity.OUTPUT_LIST);
            setData(images, true);
        }
    }
}
