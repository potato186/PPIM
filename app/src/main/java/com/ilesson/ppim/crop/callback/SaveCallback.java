package com.ilesson.ppim.crop.callback;


public interface SaveCallback extends Callback{
    void onSuccess(String outputUri);
    void onError();
}
