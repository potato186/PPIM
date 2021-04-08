package com.ilesson.ppim.crop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.BaseActivity;
import com.ilesson.ppim.crop.callback.CropCallback;
import com.ilesson.ppim.crop.callback.LoadCallback;
import com.ilesson.ppim.crop.callback.SaveCallback;
import com.ilesson.ppim.utils.SDCardUtils;

import java.io.File;


public class CropActivity extends BaseActivity {
    private Uri mImgUri;
    // Lifecycle Method ////////////////////////////////////////////////////////////////////////////
    public static final String CROP_IMG_URI = "crop_img_uri";
    public static final int REQUEST_CROP = 69;
    private static final int REQUEST_PICK_IMAGE = 10011;
    private static final int REQUEST_SAF_PICK_IMAGE = 10012;
    private static final String PROGRESS_DIALOG = "ProgressDialog";
    private ProgressBar progress;
    private CropImageView mCropView;
    private LinearLayout mRootLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        Intent intent = getIntent();
        mImgUri = intent.getParcelableExtra(CROP_IMG_URI);
        bindViews();
        FontUtils.setFont(mRootLayout);
        mCropView.startLoad(mImgUri, mLoadCallback);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().add(R.id.container, MainFragment.getInstance(uri)).commit();
//        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            showProgres();
            Uri uri = result.getData();
            mImgUri = uri;
            mCropView.startLoad(result.getData(), mLoadCallback);
        } else if (requestCode == REQUEST_SAF_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            showProgres();
            Uri uri = result.getData();
            mImgUri = uri;
            mCropView.startLoad(uri, mLoadCallback);
        }
    }
    private void showProgres(){
        progress.setVisibility(View.VISIBLE);
    }

    // Bind views //////////////////////////////////////////////////////////////////////////////////

    private void bindViews() {
        mCropView = (CropImageView) findViewById(R.id.cropImageView);
        findViewById(R.id.buttonDone).setOnClickListener(btnListener);
        findViewById(R.id.buttonFitImage).setOnClickListener(btnListener);
        findViewById(R.id.button1_1).setOnClickListener(btnListener);
        findViewById(R.id.button3_4).setOnClickListener(btnListener);
        findViewById(R.id.button4_3).setOnClickListener(btnListener);
        findViewById(R.id.button9_16).setOnClickListener(btnListener);
        findViewById(R.id.button16_9).setOnClickListener(btnListener);
        findViewById(R.id.buttonFree).setOnClickListener(btnListener);
        findViewById(R.id.buttonPickImage).setOnClickListener(btnListener);
        findViewById(R.id.buttonRotateLeft).setOnClickListener(btnListener);
        findViewById(R.id.buttonRotateRight).setOnClickListener(btnListener);
        findViewById(R.id.buttonCustom).setOnClickListener(btnListener);
        findViewById(R.id.buttonCircle).setOnClickListener(btnListener);
        findViewById(R.id.buttonShowCircleButCropAsSquare).setOnClickListener(btnListener);
        findViewById(R.id.back).setOnClickListener(btnListener);
        mRootLayout = (LinearLayout) findViewById(R.id.layout_root);
        progress = (ProgressBar) findViewById(R.id.progress);
    }

    public void pickImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_PICK_IMAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_SAF_PICK_IMAGE);
        }
    }

    public void cropImage() {
        showProgres();
        String picPath = SDCardUtils.getBookDatapath() + "crop/";
        File dir = new File(picPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String path = picPath+System.currentTimeMillis()+".jpg";

        mCropView.startCrop(path, mCropCallback, mSaveCallback);
    }

    private final View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonDone:
                    cropImage();
                    break;
                case R.id.buttonFitImage:
                    mCropView.setCropMode(CropImageView.CropMode.FIT_IMAGE);
                    break;
                case R.id.button1_1:
                    mCropView.setCropMode(CropImageView.CropMode.SQUARE);
                    break;
                case R.id.button3_4:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
                    break;
                case R.id.button4_3:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_4_3);
                    break;
                case R.id.button9_16:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);
                    break;
                case R.id.button16_9:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_16_9);
                    break;
                case R.id.buttonCustom:
                    mCropView.setCustomRatio(7, 5);
                    break;
                case R.id.buttonFree:
                    mCropView.setCropMode(CropImageView.CropMode.FREE);
                    break;
                case R.id.buttonCircle:
                    mCropView.setCropMode(CropImageView.CropMode.CIRCLE);
                    break;
                case R.id.buttonShowCircleButCropAsSquare:
                    mCropView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
                    break;
                case R.id.buttonRotateLeft:
                    mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
                    break;
                case R.id.buttonRotateRight:
                    mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                    break;
                case R.id.buttonPickImage:
                    pickImage();
                    break;
                case R.id.back:
                    finish();
                    break;
            }
        }
    };

    private void dismissProgress(){
        progress.setVisibility(View.GONE);
    }
    // Callbacks ///////////////////////////////////////////////////////////////////////////////////

    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override
        public void onSuccess() {
            dismissProgress();
        }

        @Override
        public void onError() {
            dismissProgress();
        }
    };

    private final CropCallback mCropCallback = new CropCallback() {
        @Override
        public void onSuccess(Bitmap cropped) {
        }

        @Override
        public void onError() {
        }
    };

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override
        public void onSuccess(String outputUri) {
            dismissProgress();
            startResultActivity(outputUri);
        }

        @Override
        public void onError() {
            dismissProgress();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void startResultActivity(String uri) {
        if (isFinishing()) return;
        Intent intent = new Intent();
        intent.putExtra(CropActivity.CROP_IMG_URI, uri);
        setResult(REQUEST_CROP, intent);
        finish();
    }
}