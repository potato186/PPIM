package com.ilesson.ppim.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.ilesson.ppim.R;
import com.ilesson.ppim.fragment.PPConversation;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.actions.IClickActions;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.PublicServiceMultiRichContentMessage;
import io.rong.message.PublicServiceRichContentMessage;
import io.rong.message.RichContentItem;
import io.rong.message.VoiceMessage;

public class FavClickActions implements IClickActions {

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.mipmap.fav);
    }

    @Override
    public void onClick(Fragment curFragment) {
        PPConversation fragment = (PPConversation) curFragment;
        List<Message> messages = fragment.getCheckedMessages();
        List<Message> forwardMessagesList = new ArrayList<>();
        boolean allMessagesAllowForward = true;
        for (Message message : messages) {
            if (!allowForward(message)) {
                allMessagesAllowForward = false;
                break;
            }
//            if (message.getContent() instanceof PublicServiceRichContentMessage) {
//                //公众号消息需要转为图文消息转发
//                if (((PublicServiceRichContentMessage) message.getContent()).getMessage() != null) {
//                    RichContentItem richContentItem = ((PublicServiceRichContentMessage) message.getContent()).getMessage();
//                    if (richContentItem != null)
//                        forwardMessagesList.add(getRichContentMessage(richContentItem, message));
//                }
//            } else if (message.getContent() instanceof PublicServiceMultiRichContentMessage) {
//                PublicServiceMultiRichContentMessage multiRichContentMessage = (PublicServiceMultiRichContentMessage) message.getContent();
//                if (multiRichContentMessage != null) {
//                    ArrayList<RichContentItem> richContentItemsList = multiRichContentMessage.getMessages();
//                    if (richContentItemsList != null) {
//                        for (RichContentItem richContentItem : richContentItemsList) {
//                            if (richContentItem != null)
//                                forwardMessagesList.add(getRichContentMessage(richContentItem, message));
//                        }
//                    }
//                }
//            } else {
                forwardMessagesList.add(message);
//            }
        }
        Toast.makeText(curFragment.getActivity(),"jfkdls",Toast.LENGTH_LONG).show();
            if (!allMessagesAllowForward) {
            new AlertDialog.Builder(curFragment.getActivity())
//                    .setTitle(R.string.seal_not_support_forward_pic)
                    .setMessage("部分消息不支持收藏")
                    .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

    }

    private boolean allowForward(Message message) {
        if (message != null) {
            MessageContent messageContent = message.getContent();
            if (messageContent != null) {
                if (messageContent instanceof VoiceMessage ||messageContent instanceof PPayMessage ||messageContent instanceof TransactionMessage ||
                        messageContent instanceof RedBackMessage ||messageContent instanceof TransferMessage ||
//                        message.getObjectName().equals(RedpacketModule.jrmfOpenMessage) ||
//                        message.getObjectName().equals(RedpacketModule.jrmfMessage) ||
                        messageContent instanceof RealTimeLocationStartMessage ||
                        message.getSentStatus() == Message.SentStatus.FAILED ||
                        message.getSentStatus() == Message.SentStatus.CANCELED) {
                    return false;
                }
            }
        }
        return true;
    }
}
