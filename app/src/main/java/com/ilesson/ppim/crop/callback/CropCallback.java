package com.ilesson.ppim.crop.callback;

import android.graphics.Bitmap;

public interface CropCallback extends Callback {
    void onSuccess(Bitmap cropped);
    void onError();
}
