package org.xutils.download;

import android.view.View;

import org.xutils.common.Callback;
import org.xutils.x;

import java.io.File;

public abstract class DownloadViewHolder {

    public DownloadInfo downloadInfo;
    public View view;
    public DownloadViewHolder(View view, DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
        this.view = view;
        x.view().inject(this, view);
    }

    public final DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void update(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }
    public abstract void onWaiting();
    public abstract void onStarted();
    public abstract void onLoading(long total, long current);
    public abstract void onSuccess(File result);
    public abstract void onError(Throwable ex, boolean isOnCallback);

    public abstract void onCancelled(Callback.CancelledException cex);
}
