package com.ilesson.ppim.activity;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.decoding.RGBLuminanceSource;
import com.google.zxing.qrcode.QRCodeReader;
import com.ilesson.ppim.utils.BitmapUtil;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imageloader.core.assist.FailReason;
import io.rong.imageloader.core.assist.ImageScaleType;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.listener.ImageLoadingListener;
import io.rong.imageloader.core.listener.ImageLoadingProgressListener;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.destruct.DestructManager;
import io.rong.imkit.model.Event.MessageDeleteEvent;
import io.rong.imkit.model.Event.RemoteMessageRecallEvent;
import io.rong.imkit.model.Event.changeDestructionReadTimeEvent;
import io.rong.imkit.plugin.image.HackyViewPager;
import io.rong.imkit.utilities.KitStorageUtils;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utilities.OptionsPopupDialog.OnOptionsItemClickedListener;
import io.rong.imkit.utils.ImageDownloadManager;
import io.rong.imkit.utils.ImageDownloadManager.DownloadStatusError;
import io.rong.imkit.utils.ImageDownloadManager.DownloadStatusListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongCommonDefine.GetMessageDirection;
import io.rong.imlib.RongIMClient.DestructCountDownTimerListener;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.message.ImageMessage;
import io.rong.message.ReferenceMessage;
import io.rong.subscaleview.ImageSource;
import io.rong.subscaleview.SubsamplingScaleImageView;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class PPicturePagerActivity extends RongBaseNoActionbarActivity implements OnLongClickListener {
    private static final String TAG = "PicturePagerActivity";
    private static final int IMAGE_MESSAGE_COUNT = 10;
    protected HackyViewPager mViewPager;
    protected ImageMessage mCurrentImageMessage;
    protected Message mMessage;
    protected ConversationType mConversationType;
    protected int mCurrentMessageId;
    protected String mTargetId = null;
    private int mCurrentIndex = 0;
    protected PPicturePagerActivity.ImageAdapter mImageAdapter;
    protected boolean isFirstTime = false;
    protected OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            RLog.i("PPicturePagerActivity", "onPageSelected. position:" + position);
            PPicturePagerActivity.this.mCurrentIndex = position;
            View view = PPicturePagerActivity.this.mViewPager.findViewById(position);
            if (view != null) {
                PPicturePagerActivity.this.mImageAdapter.updatePhotoView(position, view);
            }

            if (position == PPicturePagerActivity.this.mImageAdapter.getCount() - 1) {
                PPicturePagerActivity.this.getConversationImageUris(PPicturePagerActivity.this.mImageAdapter.getItem(position).getMessageId().getMessageId(), GetMessageDirection.BEHIND);
            } else if (position == 0) {
                PPicturePagerActivity.this.getConversationImageUris(PPicturePagerActivity.this.mImageAdapter.getItem(position).getMessageId().getMessageId(), GetMessageDirection.FRONT);
            }

        }

        public void onPageScrollStateChanged(int state) {
        }
    };

    public PPicturePagerActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layout.rc_fr_photo);
        Message currentMessage = (Message)this.getIntent().getParcelableExtra("message");
        this.mMessage = currentMessage;
        if (currentMessage.getContent() instanceof ReferenceMessage) {
            ReferenceMessage referenceMessage = (ReferenceMessage)currentMessage.getContent();
            this.mCurrentImageMessage = (ImageMessage)referenceMessage.getReferenceContent();
        } else {
            this.mCurrentImageMessage = (ImageMessage)currentMessage.getContent();
        }

        this.mConversationType = currentMessage.getConversationType();
        this.mCurrentMessageId = currentMessage.getMessageId();
        this.mTargetId = currentMessage.getTargetId();
        this.mViewPager = (HackyViewPager)this.findViewById(id.viewpager);
        this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
        this.mImageAdapter = new PPicturePagerActivity.ImageAdapter();
        this.isFirstTime = true;
        if (!this.mMessage.getContent().isDestruct() && !(this.mMessage.getContent() instanceof ReferenceMessage)) {
            this.getConversationImageUris(this.mCurrentMessageId, GetMessageDirection.FRONT);
            this.getConversationImageUris(this.mCurrentMessageId, GetMessageDirection.BEHIND);
        } else {
            ArrayList<PPicturePagerActivity.ImageInfo> lists = new ArrayList();
            lists.add(new PPicturePagerActivity.ImageInfo(this.mMessage, this.mCurrentImageMessage.getThumUri(), this.mCurrentImageMessage.getLocalUri() == null ? this.mCurrentImageMessage.getRemoteUri() : this.mCurrentImageMessage.getLocalUri()));
            this.mImageAdapter.addData(lists, true);
            this.mViewPager.setAdapter(this.mImageAdapter);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }

    public void onEventMainThread(RemoteMessageRecallEvent event) {
        if (this.mCurrentMessageId == event.getMessageId()) {
            (new Builder(this, 5)).setMessage(this.getString(string.rc_recall_success)).setPositiveButton(this.getString(string.rc_dialog_ok), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PPicturePagerActivity.this.finish();
                }
            }).setCancelable(false).show();
        } else {
            this.mImageAdapter.removeRecallItem(event.getMessageId());
            this.mImageAdapter.notifyDataSetChanged();
            if (this.mImageAdapter.getCount() == 0) {
                this.finish();
            }
        }

    }

    public void onEventMainThread(MessageDeleteEvent deleteEvent) {
        RLog.d("PPicturePagerActivity", "MessageDeleteEvent");
        if (deleteEvent.getMessageIds() != null) {
            Iterator var2 = deleteEvent.getMessageIds().iterator();

            while(var2.hasNext()) {
                int messageId = (Integer)var2.next();
                this.mImageAdapter.removeRecallItem(messageId);
            }

            this.mImageAdapter.notifyDataSetChanged();
            if (this.mImageAdapter.getCount() == 0) {
                this.finish();
            }
        }

    }

    private void getConversationImageUris(int mesageId, final GetMessageDirection direction) {
        if (this.mConversationType != null && !TextUtils.isEmpty(this.mTargetId)) {
            RongIMClient.getInstance().getHistoryMessages(this.mConversationType, this.mTargetId, "RC:ImgMsg", mesageId, 10, direction, new ResultCallback<List<Message>>() {
                public void onSuccess(List<Message> messages) {
                    ArrayList<PPicturePagerActivity.ImageInfo> lists = new ArrayList();
                    if (messages != null) {
                        if (direction.equals(GetMessageDirection.FRONT)) {
                            Collections.reverse(messages);
                        }

                        for(int i = 0; i < messages.size(); ++i) {
                            Message message = (Message)messages.get(i);
                            if (message.getContent() instanceof ImageMessage && !message.getContent().isDestruct()) {
                                ImageMessage imageMessage = (ImageMessage)message.getContent();
                                Uri largeImageUri = imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri();
                                if (imageMessage.getThumUri() != null && largeImageUri != null) {
                                    lists.add(PPicturePagerActivity.this.new ImageInfo(message, imageMessage.getThumUri(), largeImageUri));
                                }
                            }
                        }
                    }

                    if (direction.equals(GetMessageDirection.FRONT) && PPicturePagerActivity.this.isFirstTime) {
                        lists.add(PPicturePagerActivity.this.new ImageInfo(PPicturePagerActivity.this.mMessage, PPicturePagerActivity.this.mCurrentImageMessage.getThumUri(), PPicturePagerActivity.this.mCurrentImageMessage.getLocalUri() == null ? PPicturePagerActivity.this.mCurrentImageMessage.getRemoteUri() : PPicturePagerActivity.this.mCurrentImageMessage.getLocalUri()));
                        PPicturePagerActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
                        PPicturePagerActivity.this.mViewPager.setAdapter(PPicturePagerActivity.this.mImageAdapter);
                        PPicturePagerActivity.this.isFirstTime = false;
                        PPicturePagerActivity.this.mViewPager.setCurrentItem(lists.size() - 1);
                        PPicturePagerActivity.this.mCurrentIndex = lists.size() - 1;
                    } else if (lists.size() > 0) {
                        PPicturePagerActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
                        PPicturePagerActivity.this.mImageAdapter.notifyDataSetChanged();
                        if (direction.equals(GetMessageDirection.FRONT)) {
                            PPicturePagerActivity.this.mViewPager.setCurrentItem(lists.size());
                            PPicturePagerActivity.this.mCurrentIndex = lists.size();
                        }
                    }

                }

                public void onError(ErrorCode e) {
                }
            });
        }

    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        ImageLoader.getInstance().clearMemoryCache();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public boolean onPictureLongClick(View v, Uri thumbUri, Uri largeImageUri) {
        return false;
    }

    public boolean onLongClick(View v) {
        if (this.mCurrentImageMessage.isDestruct()) {
            return false;
        } else {
            PPicturePagerActivity.ImageInfo imageInfo = this.mImageAdapter.getImageInfo(this.mCurrentIndex);
            if (imageInfo != null) {
                Uri thumbUri = imageInfo.getThumbUri();
                Uri largeImageUri = imageInfo.getLargeImageUri();
                if (this.onPictureLongClick(v, thumbUri, largeImageUri)) {
                    return true;
                }

                if (largeImageUri == null) {
                    return false;
                }

                final File file;
                if (!largeImageUri.getScheme().startsWith("http") && !largeImageUri.getScheme().startsWith("https")) {
                    file = new File(largeImageUri.getPath());
                } else {
                    file = ImageLoader.getInstance().getDiskCache().get(largeImageUri.toString());
                }

                if (file == null || !file.exists()) {
                    return false;
                }
                Hashtable<DecodeHintType, String> hints = new Hashtable<>();
                hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码
                Result result = null;
//                Bitmap scanBitmap = BitmapUtil.decodeUri(this, largeImageUri, 500, 500);
                try {
                    Bitmap scanBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
                    BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                    QRCodeReader reader = new QRCodeReader();
                    result = reader.decode(bitmap1, hints);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                String[] items = new String[]{};
                String content="";
                if(null==result){
                    items = new String[]{this.getString(string.rc_save_picture)};
                }else{
                    items = new String[]{this.getString(string.rc_save_picture),this.getString(string.rc_scan_qr)};
                    content = result.getText();
                }
                final String text = content;
                OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OnOptionsItemClickedListener() {
                    public void onOptionsItemClicked(int which) {
                        if (which == 0) {
                            String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
                            if (!PermissionCheckUtil.requestPermissions(PPicturePagerActivity.this, permissions)) {
                                return;
                            }

                            if (file != null && file.exists()) {
                                String name = "rong_" + System.currentTimeMillis();
                                KitStorageUtils.saveMediaToPublicDir(PPicturePagerActivity.this, file, "image");
                                Toast.makeText(PPicturePagerActivity.this, PPicturePagerActivity.this.getString(string.rc_save_picture_at), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PPicturePagerActivity.this, PPicturePagerActivity.this.getString(string.rc_src_file_not_found), Toast.LENGTH_SHORT).show();
                            }
                        }else if(which == 1){
                            String targetId = text.substring(text.lastIndexOf("=") + 1);
                            if (text.contains("a=g")) {
                                String name = SPUtils.get(LoginActivity.USER_PHONE, "");
                                new IMUtils().requestGroupChat(SPUtils.get(LoginActivity.TOKEN, ""), targetId, name, "");
                                finish();
                                return;
                            }
                            if (text.contains("a=p")) {
                                Intent intent = new Intent(PPicturePagerActivity.this,FriendDetailActivity.class);
                                intent.putExtra(FriendDetailActivity.USER_ID,targetId);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }

                    }
                }).show();
            }

            return true;
        }
    }

    private static class DestructListener implements DestructCountDownTimerListener {
        private WeakReference<PPicturePagerActivity.ImageAdapter.ViewHolder> mHolder;
        private String mMessageId;

        public DestructListener(PPicturePagerActivity.ImageAdapter.ViewHolder pHolder, String pMessageId) {
            this.mHolder = new WeakReference(pHolder);
            this.mMessageId = pMessageId;
        }

        public void onTick(long millisUntilFinished, String pMessageId) {
            if (this.mMessageId.equals(pMessageId)) {
                PPicturePagerActivity.ImageAdapter.ViewHolder viewHolder = (PPicturePagerActivity.ImageAdapter.ViewHolder)this.mHolder.get();
                if (viewHolder != null) {
                    viewHolder.mCountDownView.setVisibility(View.VISIBLE);
                    viewHolder.mCountDownView.setText(String.valueOf(Math.max(millisUntilFinished, 1L)));
                }
            }

        }

        public void onStop(String messageId) {
            if (this.mMessageId.equals(messageId)) {
                PPicturePagerActivity.ImageAdapter.ViewHolder viewHolder = (PPicturePagerActivity.ImageAdapter.ViewHolder)this.mHolder.get();
                if (viewHolder != null) {
                    viewHolder.mCountDownView.setVisibility(View.GONE);
                }
            }

        }
    }

    protected class ImageInfo {
        private Message message;
        private Uri thumbUri;
        private Uri largeImageUri;

        ImageInfo(Message message, Uri thumbnail, Uri largeImageUri) {
            this.message = message;
            this.thumbUri = thumbnail;
            this.largeImageUri = largeImageUri;
        }

        public Message getMessageId() {
            return this.message;
        }

        public Uri getLargeImageUri() {
            return this.largeImageUri;
        }

        public Uri getThumbUri() {
            return this.thumbUri;
        }
    }

    protected class ImageAdapter extends PagerAdapter {
        private ArrayList<PPicturePagerActivity.ImageInfo> mImageList = new ArrayList();

        protected ImageAdapter() {
        }

        private View newView(Context context, PPicturePagerActivity.ImageInfo imageInfo) {
            View result = LayoutInflater.from(context).inflate(layout.rc_fr_image, (ViewGroup)null);
            PPicturePagerActivity.ImageAdapter.ViewHolder holder = new PPicturePagerActivity.ImageAdapter.ViewHolder();
            holder.progressBar = (ProgressBar)result.findViewById(id.rc_progress);
            holder.progressText = (TextView)result.findViewById(id.rc_txt);
            holder.photoView = (SubsamplingScaleImageView)result.findViewById(id.rc_photoView);
            holder.mCountDownView = (TextView)result.findViewById(id.rc_count_down);
            holder.photoView.setOnLongClickListener(PPicturePagerActivity.this);
            holder.photoView.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(View v) {
                    Window window = PPicturePagerActivity.this.getWindow();
                    if (window != null) {
                        window.setFlags(2048, 2048);
                    }

                    PPicturePagerActivity.this.finish();
                }
            });
            result.setTag(holder);
            return result;
        }

        public void addData(ArrayList<PPicturePagerActivity.ImageInfo> newImages, boolean direction) {
            if (newImages != null && newImages.size() != 0) {
                if (this.mImageList.size() == 0) {
                    this.mImageList.addAll(newImages);
                } else if (direction && !PPicturePagerActivity.this.isFirstTime && !this.isDuplicate(((PPicturePagerActivity.ImageInfo)newImages.get(0)).getMessageId().getMessageId())) {
                    ArrayList<PPicturePagerActivity.ImageInfo> temp = new ArrayList();
                    temp.addAll(this.mImageList);
                    this.mImageList.clear();
                    this.mImageList.addAll(newImages);
                    this.mImageList.addAll(this.mImageList.size(), temp);
                } else if (!PPicturePagerActivity.this.isFirstTime && !this.isDuplicate(((PPicturePagerActivity.ImageInfo)newImages.get(0)).getMessageId().getMessageId())) {
                    this.mImageList.addAll(this.mImageList.size(), newImages);
                }

            }
        }

        private boolean isDuplicate(int messageId) {
            Iterator var2 = this.mImageList.iterator();

            PPicturePagerActivity.ImageInfo info;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                info = (PPicturePagerActivity.ImageInfo)var2.next();
            } while(info.getMessageId().getMessageId() != messageId);

            return true;
        }

        public PPicturePagerActivity.ImageInfo getItem(int index) {
            return (PPicturePagerActivity.ImageInfo)this.mImageList.get(index);
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public int getCount() {
            return this.mImageList.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            RLog.i("PPicturePagerActivity", "instantiateItem.position:" + position);
            View imageView = this.newView(container.getContext(), (PPicturePagerActivity.ImageInfo)this.mImageList.get(position));
            this.updatePhotoView(position, imageView);
            imageView.setId(position);
            container.addView(imageView);
            return imageView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            RLog.i("PPicturePagerActivity", "destroyItem.position:" + position);
            container.removeView((View)object);
        }

        private void removeRecallItem(int messageId) {
            for(int i = this.mImageList.size() - 1; i >= 0; --i) {
                if (((PPicturePagerActivity.ImageInfo)this.mImageList.get(i)).message.getMessageId() == messageId) {
                    this.mImageList.remove(i);
                    break;
                }
            }

        }

        private void updatePhotoView(final int position, View view) {
            final PPicturePagerActivity.ImageAdapter.ViewHolder holder = (PPicturePagerActivity.ImageAdapter.ViewHolder)view.getTag();
            Uri originalUri = ((PPicturePagerActivity.ImageInfo)this.mImageList.get(position)).getLargeImageUri();
            final Uri thumbUri = ((PPicturePagerActivity.ImageInfo)this.mImageList.get(position)).getThumbUri();
            if (originalUri != null && thumbUri != null) {
                if (PPicturePagerActivity.this.mCurrentImageMessage.isDestruct() && PPicturePagerActivity.this.mMessage.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                    DestructManager.getInstance().addListener(PPicturePagerActivity.this.mMessage.getUId(), new PPicturePagerActivity.DestructListener(holder, PPicturePagerActivity.this.mMessage.getUId()), "PPicturePagerActivity");
                }

                File file = ImageLoader.getInstance().getDiskCache().get(originalUri.toString());
                if (file != null && file.exists()) {
                    Uri resultUri = Uri.fromFile(file);
                    String path = "";
                    if (!resultUri.equals(holder.photoView.getUri())) {
                        if (resultUri.getScheme().equals("file")) {
                            path = resultUri.toString().substring(5);
                        } else if (resultUri.getScheme().equals("content")) {
                            Cursor cursor = PPicturePagerActivity.this.getApplicationContext().getContentResolver().query(resultUri, new String[]{"_data"}, (String)null, (String[])null, (String)null);
                            cursor.moveToFirst();
                            path = cursor.getString(0);
                            cursor.close();
                        }

                        holder.photoView.setOrientation(FileUtils.readPictureDegree(PPicturePagerActivity.this, path));
                        Options options = new Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path, options);
                        holder.photoView.suitMaxScaleWithSize(options.outWidth, options.outHeight);
                        holder.photoView.setImage(ImageSource.uri(resultUri));
                    }

                } else {
                    DisplayImageOptions optionsx = (new io.rong.imageloader.core.DisplayImageOptions.Builder()).cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.NONE).bitmapConfig(Config.RGB_565).considerExifParams(true).build();
                    ImageLoader.getInstance().loadImage(originalUri.toString(), (ImageSize)null, optionsx, new ImageLoadingListener() {
                        public void onLoadingStarted(String imageUri, View view) {
                            String thumbPath = null;
                            Bitmap thumbBitmap = null;
                            if ("file".equals(thumbUri.getScheme())) {
                                thumbPath = thumbUri.toString().substring(5);
                            }

                            if (thumbPath != null) {
                                thumbBitmap = BitmapFactory.decodeFile(thumbPath);
                            }

                            holder.photoView.setBitmapAndFileUri(thumbBitmap, (Uri)null);
                            holder.progressText.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.VISIBLE);
                            holder.progressText.setText("0%");
                        }

                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            if (imageUri.startsWith("file://")) {
                                holder.progressText.setVisibility(View.GONE);
                                holder.progressBar.setVisibility(View.GONE);
                            } else {
                                String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
                                if (!PermissionCheckUtil.requestPermissions(PPicturePagerActivity.this, permissions)) {
                                    holder.progressText.setVisibility(View.GONE);
                                    holder.progressBar.setVisibility(View.GONE);
                                    return;
                                }

                                ImageDownloadManager.getInstance().downloadImage(imageUri, new DownloadStatusListener() {
                                    public void downloadSuccess(String localPath, Bitmap bitmap) {
                                        holder.photoView.setImage(ImageSource.uri(localPath));
                                        holder.progressText.setVisibility(View.GONE);
                                        holder.progressBar.setVisibility(View.GONE);
                                    }

                                    public void downloadFailed(DownloadStatusError error) {
                                        holder.progressText.setVisibility(View.GONE);
                                        holder.progressBar.setVisibility(View.GONE);
                                    }
                                });
                            }

                        }

                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (PPicturePagerActivity.this.mCurrentImageMessage.isDestruct() && PPicturePagerActivity.this.mMessage.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                                DestructManager.getInstance().startDestruct(PPicturePagerActivity.this.mMessage);
                                EventBus.getDefault().post(new changeDestructionReadTimeEvent(PPicturePagerActivity.this.mMessage));
                            }

                            holder.progressText.setVisibility(View.GONE);
                            holder.progressBar.setVisibility(View.GONE);
                            File file = ImageLoader.getInstance().getDiskCache().get(imageUri);
                            Uri resultUri = null;
                            if (file != null) {
                                resultUri = Uri.fromFile(file);
                            }

                            holder.photoView.suitMaxScaleWithSize(loadedImage.getWidth(), loadedImage.getHeight());
                            holder.photoView.setImage(ImageSource.uri(resultUri));
                            View inPagerView = PPicturePagerActivity.this.mViewPager.findViewById(position);
                            if (inPagerView != null) {
                                PPicturePagerActivity.ImageAdapter.ViewHolder inPagerHolder = (PPicturePagerActivity.ImageAdapter.ViewHolder)inPagerView.getTag();
                                if (inPagerHolder != holder) {
                                    inPagerHolder.progressText.setVisibility(View.GONE);
                                    inPagerHolder.progressBar.setVisibility(View.GONE);
                                    PPicturePagerActivity.this.mImageAdapter.updatePhotoView(position, inPagerView);
                                }
                            }

                        }

                        public void onLoadingCancelled(String imageUri, View view) {
                            holder.progressText.setVisibility(View.GONE);
                            holder.progressText.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            holder.progressText.setText(current * 100 / total + "%");
                            if (current == total) {
                                holder.progressText.setVisibility(View.GONE);
                                holder.progressBar.setVisibility(View.GONE);
                            } else {
                                holder.progressText.setVisibility(View.VISIBLE);
                                holder.progressBar.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                }
            } else {
                RLog.e("PPicturePagerActivity", "large uri and thumbnail uri of the image should not be null.");
            }
        }

        public PPicturePagerActivity.ImageInfo getImageInfo(int position) {
            return (PPicturePagerActivity.ImageInfo)this.mImageList.get(position);
        }

        public class ViewHolder {
            ProgressBar progressBar;
            TextView progressText;
            SubsamplingScaleImageView photoView;
            TextView mCountDownView;

            public ViewHolder() {
            }
        }
    }
}
