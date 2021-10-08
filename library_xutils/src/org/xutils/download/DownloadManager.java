package org.xutils.download;

import android.content.Context;
import android.widget.Toast;

import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.common.util.LogUtil;
import org.xutils.db.converter.ColumnConverterFactory;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class DownloadManager {

    static {
        // 注册DownloadState在数据库中的值类型映射
        ColumnConverterFactory.registerColumnConverter(DownloadState.class, new DownloadStateConverter());
    }

    private static DownloadManager instance;

    private final static int MAX_DOWNLOAD_THREAD = 1; // 有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.

    private final DbManager db;
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private final List<DownloadInfo> downloadInfoList = new ArrayList<DownloadInfo>();
    private final ConcurrentHashMap<DownloadInfo, DownloadCallback>
            callbackMap = new ConcurrentHashMap<DownloadInfo, DownloadCallback>(5);

    private DownloadManager() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("book_download")
                .setDbVersion(2);
        db = x.getDb(daoConfig);
    }

    public List<DownloadInfo> getInfos(){
    	if(downloadInfoList!=null)downloadInfoList.clear();
    	try {
            List<DownloadInfo> infoList = db.selector(DownloadInfo.class).findAll();
            if (infoList != null) {
                for (DownloadInfo info : infoList) {
                    //初始化、如果没有完成则均置为stop状态
//                    if (info.getState().value() < DownloadState.FINISHED.value()) {
//                        info.setState(DownloadState.STOPPED);
//                    }
                    downloadInfoList.add(info);
                }
            }
        } catch (DbException ex) {
            LogUtil.e(ex.getMessage(), ex);
        }
        return downloadInfoList;
    }

    /*package*/
    public static DownloadManager getInstance() {
        return new DownloadManager();
    }

    public void updateDownloadInfo(DownloadInfo info) throws DbException {
        db.update(info);
    }

    public int getDownloadListCount() {
        return downloadInfoList.size();
    }

    public DownloadInfo getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }

    public synchronized void startDownload2(DownloadInfo info, DownloadViewHolder holder) {
        try {
        	System.out.println("current="+info.getProgress());
            startDownload(info.getUrl(), info.getImgUrl(), info.getFileSavePath(),null, info.isAutoResume(), info.isAutoRename(), holder,info.getBookType(),info.getBookId());
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    public void reDownload(Context context, DownloadManager downloadManager, DownloadInfo downloadInfo){
        Toast.makeText(context,"数据有误，重新下载中……",Toast.LENGTH_LONG).show();
        try {
            downloadManager.removeDownload(downloadInfo);
            downloadManager.startDownload(
                    downloadInfo.getUrl(), downloadInfo.getImgUrl(),
                    downloadInfo.getBookName()+System.currentTimeMillis(), null,
                    downloadInfo.isAutoResume(),
                    downloadInfo.isAutoRename(),
                    null, downloadInfo.getBookType(),downloadInfo.getBookId());
        } catch (DbException e) {
        }
    }

    public synchronized void startDownload(String url, String imageURL, String savePath,String name,
                                           boolean autoResume, boolean autoRename,
                                           DownloadViewHolder viewHolder,int type,int bookId) throws DbException {

        String fileSavePath = new File(savePath).getAbsolutePath();

        DownloadInfo downloadInfo = db.selector(DownloadInfo.class)
                .where("url", "=", url)
                .and("fileSavePath", "=", fileSavePath)
                .findFirst();
        if (downloadInfo != null) {
            DownloadCallback callback = callbackMap.get(downloadInfo);
            if (callback != null) {
                if (viewHolder == null) {
                    viewHolder = new DefaultDownloadViewHolder(null, downloadInfo);
                }
                if (callback.switchViewHolder(viewHolder)) {
                    return;
                } else {
                    callback.cancel();
                }
            }
        }

        // create download info
        if (downloadInfo == null) {
            downloadInfo = new DownloadInfo();
            downloadInfo.setUrl(url);
            downloadInfo.setBookName(name);
            downloadInfo.setAutoRename(autoRename);
            downloadInfo.setImgUrl(imageURL);
            downloadInfo.setAutoResume(autoResume);
            downloadInfo.setFileSavePath(fileSavePath);
            downloadInfo.setBookType(type);
            downloadInfo.setBookId(bookId);
            db.saveBindingId(downloadInfo);
        }
        // start downloading
        if (viewHolder == null) {
            viewHolder = new DefaultDownloadViewHolder(null, downloadInfo);
        } else {
            viewHolder.update(downloadInfo);
        }
        DownloadCallback callback = new DownloadCallback(viewHolder);
        callback.setDownloadManager(this);
        callback.switchViewHolder(viewHolder);
        RequestParams params = new RequestParams(url);
        params.setAutoResume(downloadInfo.isAutoResume());
        params.setAutoRename(downloadInfo.isAutoRename());
        params.setSaveFilePath(downloadInfo.getFileSavePath());
        params.setExecutor(executor);
        params.setCancelFast(true);
        Callback.Cancelable cancelable = x.http().get(params, callback);
        callback.setCancelable(cancelable);
        callbackMap.put(downloadInfo, callback);

        if (downloadInfoList.contains(downloadInfo)) {
            int index = downloadInfoList.indexOf(downloadInfo);
            downloadInfoList.remove(downloadInfo);
            downloadInfoList.add(index, downloadInfo);
        } else {
            downloadInfoList.add(downloadInfo);
        }
    }

    public synchronized void stopDownload(int index) {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        stopDownload(downloadInfo);
    }

    public synchronized void stopDownload(DownloadInfo downloadInfo) {
        Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    public void stopAllDownload() {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
            if (cancelable != null) {
                cancelable.cancel();
            }
        }
    }

    public void removeDownload(int index) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(index);
    }

    public void removeDownload(DownloadInfo downloadInfo) throws DbException {
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(downloadInfo);
    }
    public void delete(DownloadInfo info){
    	try {
			db.delete(info);
		} catch (DbException e) {
			e.printStackTrace();
		}
    }
//    public void update
}
