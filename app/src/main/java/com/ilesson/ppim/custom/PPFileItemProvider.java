package com.ilesson.ppim.custom;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ilesson.ppim.activity.TbsFileActivity;
import com.tencent.smtt.sdk.TbsVideo;

import io.rong.imkit.R.drawable;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utils.FileTypeUtils;
import io.rong.imkit.widget.EllipsizeTextView;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.FileMessage;

@ProviderTag(
        messageContent = FileMessage.class,
        showProgress = false,
        showReadState = true
)
public class PPFileItemProvider extends IContainerItemProvider.MessageProvider<FileMessage> {
    private static final String TAG = "FileMessageItemProvider";

    public PPFileItemProvider() {
    }

    private Context context;

    public View newView(Context context, ViewGroup group) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(layout.rc_item_file_message, (ViewGroup) null);
        ViewHolder holder = new ViewHolder();
        holder.message = (LinearLayout) view.findViewById(id.rc_message);
        holder.fileTypeImage = (ImageView) view.findViewById(id.rc_msg_iv_file_type_image);
        holder.fileName = (EllipsizeTextView) view.findViewById(id.rc_msg_tv_file_name);
        holder.fileSize = (TextView) view.findViewById(id.rc_msg_tv_file_size);
        holder.fileUploadProgress = (ProgressBar) view.findViewById(id.rc_msg_pb_file_upload_progress);
        holder.cancelButton = (RelativeLayout) view.findViewById(id.rc_btn_cancel);
        holder.canceledMessage = (TextView) view.findViewById(id.rc_msg_canceled);
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, FileMessage content, final UIMessage message) {
        final ViewHolder holder = (ViewHolder) v.getTag();
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {
            holder.message.setBackgroundResource(drawable.rc_ic_bubble_right_file);
        } else {
            holder.message.setBackgroundResource(drawable.rc_ic_bubble_left_file);
        }

        holder.fileName.setAdaptiveText(content.getName());
        long fileSizeBytes = content.getSize();
        holder.fileSize.setText(FileTypeUtils.formatFileSize(fileSizeBytes));
        holder.fileTypeImage.setImageResource(FileTypeUtils.fileTypeImageId(content.getName()));
        if (message.getSentStatus().equals(Message.SentStatus.SENDING) && message.getProgress() < 100) {
            holder.fileUploadProgress.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.canceledMessage.setVisibility(View.INVISIBLE);
            holder.fileUploadProgress.setProgress(message.getProgress());
        } else {
            if (message.getSentStatus().equals(Message.SentStatus.CANCELED)) {
                holder.canceledMessage.setVisibility(View.VISIBLE);
            } else {
                holder.canceledMessage.setVisibility(View.INVISIBLE);
            }

            holder.fileUploadProgress.setVisibility(View.INVISIBLE);
            holder.cancelButton.setVisibility(View.GONE);
        }

        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RongIM.getInstance().cancelSendMediaMessage(message.getMessage(), new RongIMClient.OperationCallback() {
                    public void onSuccess() {
                        holder.canceledMessage.setVisibility(View.VISIBLE);
                        holder.fileUploadProgress.setVisibility(View.INVISIBLE);
                        holder.cancelButton.setVisibility(View.GONE);
                    }

                    public void onError(RongIMClient.ErrorCode errorCode) {
                    }
                });
//                FileMessageItemProvider
            }
        });
    }

    public Spannable getContentSummary(FileMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, FileMessage data) {
        StringBuilder summaryPhrase = new StringBuilder();
        String fileName = data.getName();
        summaryPhrase.append(context.getString(string.rc_message_content_file)).append(" ").append(fileName);
        return new SpannableString(summaryPhrase);
    }

//    @Override
//    public void onItemLongClick(final View view, final int position, final FileMessage content,final UIMessage message) {
//        String[] items1;//复制，删除
//        items1 = new String[]{ view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete), view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_forward)};
//        final OptionsPopupDialog dialog = OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
//            public void onOptionsItemClicked(int which) {
//               if (which == 0) {
//                    RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, (RongIMClient.ResultCallback) null);
//                }else if (which == 1) {
//                    Intent intent = new Intent(context, ForwadSelectActivity.class);
//                    intent.putExtra("msg",content);
//                    context.startActivity(intent);
//                }
//            }
//        });
//        dialog.show();
//    }

    public void onItemClick(View view, int position, FileMessage content, UIMessage message) {
//        if(path.endsWith(".mp4")||path.endsWith(".3gp")||path.endsWith(".avi")||path.endsWith(".mkv")||path.endsWith(".mkv")||path.endsWith(".rmvb")||path.endsWith(".mp3")){
//            AppUtils.openFile(view.getContext(),path,content.getFileUrl());
//            return;
//        }
        String fileUrl = "";
        Uri uri = content.getFileUrl();
        if (null != uri) {
            fileUrl = uri.toString();
        }
        Intent intent = new Intent();
        intent.setClass(view.getContext(), TbsFileActivity.class);
        intent.putExtra(TbsFileActivity.FILE_URL, fileUrl);
        Uri localUri = content.getLocalPath();
        if (null != localUri) {
            String path = localUri.toString().replace("file:///", "");
            intent.putExtra(TbsFileActivity.FILE_LOCAL_URL, path);
        }
        String mFileName = content.getName();
        if(mFileName.endsWith(".mp4")||mFileName.endsWith(".3gp")||mFileName.endsWith(".avi")||mFileName.endsWith(".mkv")||mFileName.endsWith(".rmvb")&& TbsVideo.canUseTbsPlayer(view.getContext())){
            TbsVideo.openVideo(view.getContext(), fileUrl);
            return;
        }
        intent.putExtra(TbsFileActivity.FILE_NAME, content.getName());
        view.getContext().startActivity(intent);
    }

    /**
     * 多种文件类型
     */
    private static class ViewHolder {
        RelativeLayout cancelButton;
        LinearLayout message;
        EllipsizeTextView fileName;
        TextView fileSize;
        TextView canceledMessage;
        ImageView fileTypeImage;
        ProgressBar fileUploadProgress;

        private ViewHolder() {
        }
    }
}
