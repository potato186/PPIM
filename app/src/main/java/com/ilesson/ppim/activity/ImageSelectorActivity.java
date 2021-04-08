package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.adapter.ImageFolderAdapter;
import com.ilesson.ppim.adapter.ImageListAdapter;
import com.ilesson.ppim.entity.LocalMedia;
import com.ilesson.ppim.entity.LocalMediaFolder;
import com.ilesson.ppim.utils.GridSpacingItemDecoration;
import com.ilesson.ppim.utils.LocalMediaLoader;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.view.FolderWindow;

import java.util.ArrayList;
import java.util.List;

import static com.ilesson.ppim.activity.ComposeActivity.REQUEST_CODE_TAKE_PICTURE;

public class ImageSelectorActivity extends BaseActivity {
    public final static int REQUEST_IMAGE = 66;
    public final static int REQUEST_CAMERA = 67;

    public final static String BUNDLE_CAMERA_PATH = "CameraPath";

    public final static String REQUEST_OUTPUT = "outputList";

    public final static String EXTRA_SELECT_MODE = "SelectMode";
    public final static String EXTRA_SHOW_CAMERA = "ShowCamera";
    public final static String EXTRA_ENABLE_PREVIEW = "EnablePreview";
    public final static String EXTRA_ENABLE_CROP = "EnableCrop";
    public final static String EXTRA_MAX_SELECT_NUM = "MaxSelectNum";
    public final static String EXTRA_ALREADY_NUM = "extra_already_num";

    public final static int MODE_MULTIPLE = 1;
    public final static int MODE_SINGLE = 2;
    public final static int ICON_CAMREA = 20;

    private int maxSelectNum = 9;
    private int selectMode = MODE_MULTIPLE;
    private int alreadyNum = 0;
    private boolean showCamera = true;
    private boolean enablePreview = true;
    private boolean enableCrop = false;

    private int spanCount = 3;

    private Toolbar toolbar;
    private TextView doneText;

    private TextView previewText;

    private RecyclerView recyclerView;
    private ImageListAdapter imageAdapter;

    private LinearLayout folderLayout;
    private TextView folderName;
    private FolderWindow folderWindow;

    private String cameraPath;

    public static void start(Activity activity, int maxSelectNum, int mode, boolean isShow, boolean enablePreview, boolean enableCrop) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_SELECT_MODE, mode);
        intent.putExtra(EXTRA_SHOW_CAMERA, false);
        intent.putExtra(EXTRA_ENABLE_PREVIEW, enablePreview);
        intent.putExtra(EXTRA_ENABLE_CROP, enableCrop);
        activity.startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageselector);

        maxSelectNum = getIntent().getIntExtra(EXTRA_MAX_SELECT_NUM, 3);
        selectMode = getIntent().getIntExtra(EXTRA_SELECT_MODE, MODE_MULTIPLE);
        alreadyNum = getIntent().getIntExtra(EXTRA_ALREADY_NUM, 0);
        showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);
        enablePreview = getIntent().getBooleanExtra(EXTRA_ENABLE_PREVIEW, false);
        enableCrop = getIntent().getBooleanExtra(EXTRA_ENABLE_CROP, true);

        if (selectMode == MODE_MULTIPLE) {
//            enableCrop = false;
        } else {
            enablePreview = false;
        }
        if (savedInstanceState != null) {
            cameraPath = savedInstanceState.getString(BUNDLE_CAMERA_PATH);
        }
        initView();
        registerListener();
        new LocalMediaLoader(this, LocalMediaLoader.TYPE_IMAGE).loadAllImage(new LocalMediaLoader.LocalMediaLoadListener() {

            @Override
            public void loadComplete(List<LocalMediaFolder> folders) {
                if(folders==null||folders.size()==0)return;
                int index = 0;
//                String appName = getResources().getString(R.string.app_name);
//                for (int i = 0; i < folders.size(); i++) {
//                    if(folders.get(i).getName().contains(appName)){
//                        index = i;
//                        break;
//                    }
//                }
                folderWindow.bindFolder(folders);
                imageAdapter.bindImages(folders.get(index).getImages());
//                folderName.setText(appName);
            }
        });
    }

    public void initView() {
        folderWindow = new FolderWindow(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.picture);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.mipmap.ic_back);

        doneText = (TextView) findViewById(R.id.done_text);
        doneText.setVisibility(selectMode == MODE_MULTIPLE ? View.VISIBLE : View.GONE);

        previewText = (TextView) findViewById(R.id.preview_text);
        previewText.setVisibility(enablePreview ? View.VISIBLE : View.GONE);

        folderLayout = (LinearLayout) findViewById(R.id.folder_layout);
        folderName = (TextView) findViewById(R.id.folder_name);

        recyclerView = (RecyclerView) findViewById(R.id.folder_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, PPScreenUtils.dip2px(this, 2), false));
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        imageAdapter = new ImageListAdapter(this, maxSelectNum, selectMode, false, enablePreview, alreadyNum);
        recyclerView.setAdapter(imageAdapter);

    }

    private String mImageUrl;
    public void registerListener() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        folderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderWindow.isShowing()) {
                    folderWindow.dismiss();
                } else {
                    folderWindow.showAsDropDown(toolbar);
                }
            }
        });
        imageAdapter.setOnImageSelectChangedListener(new ImageListAdapter.OnImageSelectChangedListener() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onChange(List<LocalMedia> selectImages) {
                boolean enable = selectImages.size() != 0;
                doneText.setEnabled(enable ? true : false);
                previewText.setEnabled(enable ? true : false);
                if (enable) {
                    doneText.setText(getString(R.string.done_num, selectImages.size() + alreadyNum));
                    previewText.setText(getString(R.string.preview_num, selectImages.size()));
                } else {
                    doneText.setText(R.string.done);
                    previewText.setText(R.string.preview);
                }
            }

            @Override
            public void onTakePhoto() {
//                startCamera();
                Intent intent = new Intent(ImageSelectorActivity.this, TakePhoto.class);
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
//                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                String f = System.currentTimeMillis()+".jpg";
//                mImageUrl = FileTool.getDir(ImageSelectorActivity.this,PHOTO_PATH)+f;
//                Uri imageUri = Uri.fromFile(new File(mImageUrl));
//                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片存放位置，指定后，在onActivityResult里得到的Data将为null
//                startActivityForResult(openCameraIntent, REQUEST_CODE_TAKE_PICTURE);
            }

            @Override
            public void onPictureClick(LocalMedia media, int position) {
//                mImageUrl = media.getPath();
                setResult(media.getPath());
//                Uri uri = Uri.fromFile(new File(media.getPath()));

//                if (uri != null) {
//                    Intent it = new Intent(ImageSelectorActivity.this, CropActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable(CropActivity.CROP_IMG_URI, uri);
//                    it.putExtras(bundle);
//                    startActivityForResult(it, REQUEST_CROP);
//                }
//                if (enablePreview) {
//                    startPreview(imageAdapter.getImages(), position);
//                } else if (enableCrop) {
//
//                    startCrop(media.getPath());
//                } else {
//                    List<LocalMedia> list = new ArrayList<LocalMedia>();
//                    list.add(media);
//                    onResult(list);
//                }
            }
        });
        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResult(imageAdapter.getSelectedImages());
            }
        });
        folderWindow.setOnItemClickListener(new ImageFolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String name, List<LocalMedia> images) {
                folderWindow.dismiss();
                imageAdapter.bindImages(images);
                folderName.setText(name);
            }
        });
        previewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreview(imageAdapter.getSelectedImages(), 0);
            }
        });
    }

    private void setResult(String url){
        Intent intent = new Intent();
        intent.putExtra(PhotoActivity.IMAGE_URL,url);
        setResult(PhotoActivity.IMAGE_RESULT,intent);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            String url = data.getStringExtra(PhotoActivity.IMAGE_URL);
            setResult(url);
//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(cameraPath))));
//                if (enableCrop) {
//                    startCrop(cameraPath);
//                } else {
////                    onSelectDone(cameraPath);
//                    List<LocalMedia> list = new ArrayList<>();
//                    list.add(new LocalMedia(cameraPath));
//                    onResult(list);
//                }
//            Uri uri = Uri.fromFile(new File(cameraPath));
//            if (uri != null) {
//            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_CAMERA_PATH, cameraPath);
    }

    public void startPreview(List<LocalMedia> previewImages, int position) {
        ImagePreviewActivity.startPreview(this, previewImages, imageAdapter.getSelectedImages(), maxSelectNum, position);
    }

    public void onResult(List<LocalMedia> images) {
        setResult(RESULT_OK, new Intent().putStringArrayListExtra(REQUEST_OUTPUT, (ArrayList) images));
        finish();
    }
}
