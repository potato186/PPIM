package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.LocalMedia;
import com.ilesson.ppim.fragment.ImagePreviewFragment;
import com.ilesson.ppim.view.PreviewViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dee on 15/11/24.
 */
public class ImagePreviewActivity extends BaseActivity {
    public static final int REQUEST_PREVIEW = 68;
    public static final String EXTRA_PREVIEW_LIST = "previewList";
    public static final String EXTRA_PREVIEW_SELECT_LIST = "previewSelectList";
    public static final String EXTRA_MAX_SELECT_NUM = "maxSelectNum";
    public static final String EXTRA_POSITION = "position";

    public static final String OUTPUT_LIST = "outputList";
    public static final String OUTPUT_ISDONE = "isDone";

    private LinearLayout barLayout;
    private RelativeLayout selectBarLayout;
    private Toolbar toolbar;
    private TextView doneText;
    private CheckBox checkboxSelect;
    private PreviewViewPager viewPager;
    public static final String ONLY_PREVIEW = "only_preview";
    public static final String NO_INDEX = "no_index";
    private boolean onlyPreview;
    private boolean noIndex;
    private int position;
    private int maxSelectNum;
    private List<LocalMedia> images = new ArrayList<>();
    private List<LocalMedia> selectImages = new ArrayList<>();


    private boolean isShowBar = true;


    public static void startPreview(Activity context, List<LocalMedia> images, List<LocalMedia> selectImages, int maxSelectNum, int position) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(EXTRA_PREVIEW_LIST, (ArrayList) images);
        intent.putExtra(EXTRA_PREVIEW_SELECT_LIST, (ArrayList) selectImages);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        context.startActivityForResult(intent, REQUEST_PREVIEW);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_image_preview);
        initView();
        registerListener();
    }

    public void initView() {
        onlyPreview = getIntent().getBooleanExtra(ONLY_PREVIEW, false);
        noIndex = getIntent().getBooleanExtra(NO_INDEX, false);
        noIndex = getIntent().getBooleanExtra(NO_INDEX, false);
        images = (List<LocalMedia>) getIntent().getSerializableExtra(EXTRA_PREVIEW_LIST);
        position = getIntent().getIntExtra(EXTRA_POSITION, 1);
        selectImages = (List<LocalMedia>) getIntent().getSerializableExtra(EXTRA_PREVIEW_SELECT_LIST);
        maxSelectNum = getIntent().getIntExtra(EXTRA_MAX_SELECT_NUM, 3);

        barLayout = (LinearLayout) findViewById(R.id.bar_layout);
        selectBarLayout = (RelativeLayout) findViewById(R.id.select_bar_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(noIndex){
            toolbar.setTitle("");
        }else{
            toolbar.setTitle(position + 1 + "/" + images.size());
        }
//        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back_icon);


        doneText = (TextView) findViewById(R.id.done_text);
        checkboxSelect = (CheckBox) findViewById(R.id.checkbox_select);
        if (!onlyPreview) {
            onSelectNumChange();
            onImageSwitch(position);
        } else {

            barLayout.setVisibility(View.GONE);
            selectBarLayout.setVisibility(View.GONE);
        }

        viewPager = (PreviewViewPager) findViewById(R.id.preview_pager);
        viewPager.setAdapter(new SimpleFragmentAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(position);
    }

    public void registerListener() {
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if(noIndex){
//                    toolbar.setTitle("");
//                }else{
//                    toolbar.setTitle(position + 1 + "/" + images.size());
//                }
//                onImageSwitch(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//            }
//        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClick(false);
            }
        });
        checkboxSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = checkboxSelect.isChecked();
                if (selectImages.size() >= maxSelectNum && isChecked) {
                    Toast.makeText(ImagePreviewActivity.this, "已经选了" + maxSelectNum + "张图片", Toast.LENGTH_LONG).show();
                    checkboxSelect.setChecked(false);
                    return;
                }
                LocalMedia image = images.get(viewPager.getCurrentItem());
                if (isChecked) {
                    selectImages.add(image);
                } else {
                    for (LocalMedia media : selectImages) {
                        if (media.getPath().equals(image.getPath())) {
                            selectImages.remove(media);
                            break;
                        }
                    }
                }
                onSelectNumChange();
            }
        });
        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClick(true);
            }
        });
    }

    public class SimpleFragmentAdapter extends FragmentPagerAdapter {
        public SimpleFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImagePreviewFragment.getInstance(images.get(position).getPath());
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }

    @SuppressLint("StringFormatMatches")
    public void onSelectNumChange() {
//        boolean enable = selectImages.size() != 0;
        doneText.setEnabled(true);
//        if (enable) {
        doneText.setText(getString(R.string.done_num, selectImages.size(), maxSelectNum));
//        } else {
//            doneText.setText(R.string.done);
//        }
    }

    public void onImageSwitch(int position) {
        if(images!=null&&images.size()>position)
        checkboxSelect.setChecked(isSelected(images.get(position)));
    }

    public boolean isSelected(LocalMedia image) {
        if(null==selectImages)
            return false;
        for (LocalMedia media : selectImages) {
            if (media!=null&&media.getPath()!=null&&media.getPath().equals(image.getPath())) {
                return true;
            }
        }
        return false;
    }

    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    private void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void switchBarVisibility() {
        if(onlyPreview){
            return;
        }
        barLayout.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        toolbar.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        selectBarLayout.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        if (isShowBar) {
            hideStatusBar();
        } else {
            showStatusBar();
        }
        isShowBar = !isShowBar;
    }

    public void onDoneClick(boolean isDone) {
        Intent intent = new Intent();
        intent.putExtra(OUTPUT_LIST, (ArrayList) selectImages);
        intent.putExtra(OUTPUT_ISDONE, isDone);
        setResult(RESULT_OK, intent);
        finish();
    }
}
