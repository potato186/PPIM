package com.ilesson.ppim.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ImagePreviewActivity;
import com.ilesson.ppim.view.PhotoViewAttacher;

import java.io.File;


/**
 * Created by dee on 15/11/25.
 */
public class ImagePreviewFragment extends Fragment {
    public static final String PATH = "path";
    public static ImagePreviewFragment getInstance(String path) {
        ImagePreviewFragment fragment = new ImagePreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PATH, path);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_image_preview, container, false);
        final ImageView imageView = (ImageView) contentView.findViewById(R.id.preview_image);
        final ProgressBar bar = (ProgressBar) contentView.findViewById(R.id.progress);
        final PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        String path = getArguments().getString(PATH);
        Object object;
        if(path.startsWith("http")){
            object = path;
        }else {
            object = new File(path);
        }
        Glide.with(this).asBitmap().load(object).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                imageView.setImageBitmap(resource);
                mAttacher.update();
                bar.setVisibility(View.GONE);
            }
        });
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                ImagePreviewActivity activity = (ImagePreviewActivity) getActivity();
                activity.switchBarVisibility();
            }
        });
        return contentView;
    }
}
