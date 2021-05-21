package com.ilesson.ppim.adapter;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ilesson.ppim.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imkit.R.bool;
import io.rong.imkit.R.id;
import io.rong.imkit.R.integer;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM.ConversationBehaviorListener;
import io.rong.imkit.RongIM.ConversationClickListener;
import io.rong.imkit.destruct.DestructManager;
import io.rong.imkit.dialog.BurnHintDialog;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.Event.ShowDurnDialogEvent;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.utils.RongDateUtils;
import io.rong.imkit.widget.DebouncedOnClickListener;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.destruct.DestructionTaskManager;
import io.rong.imlib.location.message.RealTimeLocationJoinMessage;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.ReadReceiptInfo;
import io.rong.imlib.model.UnknownMessage;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.HistoryDividerMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.TextMessage;

public class MessageListAdapter extends BaseAdapter<UIMessage> {
    private static final String TAG = "MessageListAdapter";
    private static long readReceiptRequestInterval = 120L;
    private LayoutInflater mInflater;
    private Context mContext;
    private MessageListAdapter.OnItemHandlerListener mOnItemHandlerListener;
    boolean evaForRobot = false;
    boolean robotMode = true;
    protected boolean timeGone = false;
    private boolean isShowCheckbox;
    private int maxMessageSelectedCount = -1;
    private MessageListAdapter.OnMessageCheckedChanged messageCheckedChanged;
    private MessageListAdapter.OnSelectedCountDidExceed selectedCountDidExceed;

    public MessageListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);

        try {
            if (RongContext.getInstance() != null) {
                readReceiptRequestInterval = (long)RongContext.getInstance().getResources().getInteger(integer.rc_read_receipt_request_interval);
            } else {
                RLog.e("MessageListAdapter", "SDK isn't init, use default readReceiptRequestInterval. Please refer to http://support.rongcloud.cn/kb/Mjc2 about how to init.");
            }
        } catch (NotFoundException var3) {
            RLog.e("MessageListAdapter", "MessageListAdapter rc_read_receipt_request_interval not configure in rc_config.xml", var3);
        }

    }

    public void setMaxMessageSelectedCount(int maxMessageSelectedCount) {
        this.maxMessageSelectedCount = maxMessageSelectedCount;
    }

    public void setSelectedCountDidExceed(MessageListAdapter.OnSelectedCountDidExceed selectedCountDidExceed) {
        this.selectedCountDidExceed = selectedCountDidExceed;
    }

    public void setOnItemHandlerListener(MessageListAdapter.OnItemHandlerListener onItemHandlerListener) {
        this.mOnItemHandlerListener = onItemHandlerListener;
    }

    protected MessageListAdapter.OnItemHandlerListener getItemHandlerListener() {
        return this.mOnItemHandlerListener;
    }

    public boolean isShowCheckbox() {
        return this.isShowCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox) {
        this.isShowCheckbox = showCheckbox;
    }

    public void setMessageCheckedChanged(MessageListAdapter.OnMessageCheckedChanged messageCheckedChanged) {
        this.messageCheckedChanged = messageCheckedChanged;
    }

    public long getItemId(int position) {
        UIMessage message = (UIMessage)this.getItem(position);
        return message == null ? -1L : (long)message.getMessageId();
    }

    public int getPositionBySendTime(long sentTime) {
        for(int i = 0; i < this.getCount(); ++i) {
            UIMessage message = (UIMessage)this.getItem(i);
            if (message.getSentTime() > sentTime) {
                return i;
            }
        }

        return this.getCount();
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View result = this.mInflater.inflate(layout.rc_item_message, (ViewGroup)null);
        MessageListAdapter.ViewHolder holder = new MessageListAdapter.ViewHolder();
        holder.leftIconView = (CircleImageView)this.findViewById(result, id.rc_left);
        holder.rightIconView = (CircleImageView)this.findViewById(result, id.rc_right);
        holder.nameView = (TextView)this.findViewById(result, id.rc_title);
        holder.contentView = (ProviderContainerView)this.findViewById(result, id.rc_content);
        holder.layout = (ViewGroup)this.findViewById(result, id.rc_layout);
        holder.progressBar = (ProgressBar)this.findViewById(result, id.rc_progress);
        holder.warning = (ImageView)this.findViewById(result, id.rc_warning);
        holder.readReceipt = (TextView)this.findViewById(result, id.rc_read_receipt);
        holder.readReceiptRequest = (TextView)this.findViewById(result, id.rc_read_receipt_request);
        holder.readReceiptStatus = (TextView)this.findViewById(result, id.rc_read_receipt_status);
        holder.message_check = (CheckBox)this.findViewById(result, id.message_check);
        holder.checkboxLayout = (LinearLayout)this.findViewById(result, id.ll_message_check);
        holder.time = (TextView)this.findViewById(result, id.rc_time);
        holder.sentStatus = (TextView)this.findViewById(result, id.rc_sent_status);
        holder.layoutItem = (RelativeLayout)this.findViewById(result, id.rc_layout_item_message);
        this.timeGone = holder.time.getVisibility() == View.GONE;
        result.setTag(holder);
        return result;
    }

    protected boolean getNeedEvaluate(UIMessage data) {
        String robotEva = "";
        String sid = "";
        if (data != null && data.getConversationType() != null && data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE)) {
            if (data.getContent() instanceof TextMessage) {
                String extra = ((TextMessage)data.getContent()).getExtra();
                if (TextUtils.isEmpty(extra)) {
                    return false;
                }

                try {
                    JSONObject jsonObj = new JSONObject(extra);
                    robotEva = jsonObj.optString("robotEva");
                    sid = jsonObj.optString("sid");
                } catch (JSONException var6) {
                }
            }

            return data.getMessageDirection() == MessageDirection.RECEIVE && data.getContent() instanceof TextMessage && this.evaForRobot && this.robotMode && !TextUtils.isEmpty(robotEva) && !TextUtils.isEmpty(sid) && !data.getIsHistoryMessage();
        } else {
            return false;
        }
    }

    public List<Message> getCheckedMessage() {
        List<Message> checkedMessage = new ArrayList();

        for(int i = 0; i < this.getCount(); ++i) {
            UIMessage uiMessage = (UIMessage)this.getItem(i);
            if (uiMessage.isChecked()) {
                checkedMessage.add(uiMessage.getMessage());
            }
        }

        return checkedMessage;
    }

    private void bindViewClickEvent(View convertView, View contentView, final int position, final UIMessage data) {
        final MessageListAdapter.ViewHolder holder = (MessageListAdapter.ViewHolder)convertView.getTag();
        OnClickListener viewClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (MessageListAdapter.this.isShowCheckbox()) {
                    boolean checked = !data.isChecked();
                    if (MessageListAdapter.this.maxMessageSelectedCount != -1 && MessageListAdapter.this.getCheckedMessage().size() >= MessageListAdapter.this.maxMessageSelectedCount && checked) {
                        if (MessageListAdapter.this.selectedCountDidExceed != null) {
                            MessageListAdapter.this.selectedCountDidExceed.onSelectedCountDidExceed();
                        }

                        return;
                    }

                    data.setChecked(checked);
                    holder.message_check.setChecked(checked);
                    if (MessageListAdapter.this.messageCheckedChanged != null) {
                        MessageListAdapter.this.messageCheckedChanged.onCheckedEnable(MessageListAdapter.this.getCheckedMessage().size() > 0);
                    }
                }

            }
        };
        OnTouchListener viewTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (MessageListAdapter.this.isShowCheckbox() && event.getAction() == 1) {
                    boolean checked = !data.isChecked();
                    if (MessageListAdapter.this.maxMessageSelectedCount != -1 && MessageListAdapter.this.getCheckedMessage().size() >= MessageListAdapter.this.maxMessageSelectedCount && checked) {
                        if (MessageListAdapter.this.selectedCountDidExceed != null) {
                            MessageListAdapter.this.selectedCountDidExceed.onSelectedCountDidExceed();
                        }

                        return true;
                    } else {
                        data.setChecked(checked);
                        holder.message_check.setChecked(checked);
                        if (MessageListAdapter.this.messageCheckedChanged != null) {
                            MessageListAdapter.this.messageCheckedChanged.onCheckedEnable(MessageListAdapter.this.getCheckedMessage().size() > 0);
                        }

                        return true;
                    }
                } else {
                    return false;
                }
            }
        };
        OnClickListener contentClickListener = new MessageListAdapter.NoDoubleClickListener() {
            public void onNoDoubleClick(View v) {
                if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                    if (RongContext.getInstance().getConversationBehaviorListener().onMessageClick(MessageListAdapter.this.mContext, v, data.getMessage())) {
                        return;
                    }
                } else if (RongContext.getInstance().getConversationClickListener() != null && RongContext.getInstance().getConversationClickListener().onMessageClick(MessageListAdapter.this.mContext, v, data.getMessage())) {
                    return;
                }

                if (MessageListAdapter.this.mOnItemHandlerListener != null) {
                    MessageListAdapter.this.mOnItemHandlerListener.onMessageClick(position, data.getMessage(), v);
                }

                Object provider;
                if (MessageListAdapter.this.getNeedEvaluate(data)) {
                    provider = RongContext.getInstance().getEvaluateProvider();
                } else {
                    provider = RongContext.getInstance().getMessageTemplate(data.getContent().getClass());
                }

                if (provider != null) {
                    if (data != null && data.getContent() != null && data.getContent().isDestruct() && data.getMessageDirection().equals(MessageDirection.RECEIVE) && !BurnHintDialog.isFirstClick(v.getContext())) {
                        EventBus.getDefault().post(new ShowDurnDialogEvent());
                        return;
                    }

                    ((MessageProvider)provider).onItemClick(v, position, data != null ? data.getContent() : null, data);
                }

            }

            public void onClick(View v) {
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime - this.lastClickTime > 500L) {
                    this.lastClickTime = currentTime;
                    this.onNoDoubleClick(v);
                }

            }
        };
        OnLongClickListener contentLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (MessageListAdapter.this.isShowCheckbox()) {
                    return true;
                } else {
                    if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                        if (RongContext.getInstance().getConversationBehaviorListener().onMessageLongClick(MessageListAdapter.this.mContext, v, data.getMessage())) {
                            return true;
                        }
                    } else if (RongContext.getInstance().getConversationClickListener() != null && RongContext.getInstance().getConversationClickListener().onMessageLongClick(MessageListAdapter.this.mContext, v, data.getMessage())) {
                        return true;
                    }

                    Object provider;
                    if (MessageListAdapter.this.getNeedEvaluate(data)) {
                        provider = RongContext.getInstance().getEvaluateProvider();
                    } else {
                        provider = RongContext.getInstance().getMessageTemplate(data.getContent().getClass());
                    }

                    if (provider != null) {
                        ((MessageProvider)provider).onItemLongClick(v, position, data.getContent(), data);
                    }

                    return true;
                }
            }
        };
        OnClickListener iconClickListener = new OnClickListener() {
            public void onClick(View v) {
                UserInfo userInfo = data.getUserInfo();
                if (!TextUtils.isEmpty(data.getSenderUserId())) {
                    if (userInfo == null) {
                        userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                    }

                    userInfo = userInfo == null ? new UserInfo(data.getSenderUserId(), (String)null, (Uri)null) : userInfo;
                }

                if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                    RongContext.getInstance().getConversationBehaviorListener().onUserPortraitClick(MessageListAdapter.this.mContext, data.getConversationType(), userInfo);
                } else if (RongContext.getInstance().getConversationClickListener() != null) {
                    RongContext.getInstance().getConversationClickListener().onUserPortraitClick(MessageListAdapter.this.mContext, data.getConversationType(), userInfo, data.getTargetId());
                }

            }
        };
        OnLongClickListener iconLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                UserInfo userInfo = data.getUserInfo();
                if (!TextUtils.isEmpty(data.getSenderUserId())) {
                    if (userInfo == null) {
                        userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                    }

                    userInfo = userInfo == null ? new UserInfo(data.getSenderUserId(), (String)null, (Uri)null) : userInfo;
                }

                if (data.getConversationType().equals(ConversationType.GROUP)) {
                    GroupUserInfo groupUserInfo = RongUserInfoManager.getInstance().getGroupUserInfo(data.getTargetId(), data.getSenderUserId());
                    if (groupUserInfo != null && !TextUtils.isEmpty(groupUserInfo.getNickname()) && userInfo != null) {
                        userInfo.setName(groupUserInfo.getNickname());
                    }
                }

                if (data.getMessageDirection().equals(MessageDirection.SEND)) {
                    if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                        return RongContext.getInstance().getConversationBehaviorListener().onUserPortraitLongClick(MessageListAdapter.this.mContext, data.getConversationType(), userInfo);
                    }

                    if (RongContext.getInstance().getConversationClickListener() != null) {
                        return RongContext.getInstance().getConversationClickListener().onUserPortraitLongClick(MessageListAdapter.this.mContext, data.getConversationType(), userInfo, data.getTargetId());
                    }
                } else {
                    Object conversationListener = RongContext.getInstance().getConversationListener();
                    if (conversationListener != null && (conversationListener instanceof ConversationClickListener && ((ConversationClickListener)conversationListener).onUserPortraitLongClick(MessageListAdapter.this.mContext, data.getConversationType(), userInfo, data.getTargetId()) || conversationListener instanceof ConversationBehaviorListener && ((ConversationBehaviorListener)conversationListener).onUserPortraitLongClick(MessageListAdapter.this.mContext, data.getConversationType(), userInfo))) {
                        return true;
                    }

                    if (RongContext.getInstance().getResources().getBoolean(bool.rc_enable_mentioned_message) && (data.getConversationType().equals(ConversationType.GROUP) || data.getConversationType().equals(ConversationType.DISCUSSION))) {
                        RongMentionManager.getInstance().mentionMember(data.getConversationType(), data.getTargetId(), data.getSenderUserId());
                        return true;
                    }
                }

                return true;
            }
        };
        if (this.isShowCheckbox() && this.allowShowCheckButton(data.getMessage())) {
            convertView.setOnClickListener(viewClickListener);
            contentView.setOnTouchListener(viewTouchListener);
            holder.rightIconView.setOnClickListener(viewClickListener);
            holder.leftIconView.setOnClickListener(viewClickListener);
        } else {
            contentView.setOnClickListener(contentClickListener);
            contentView.setOnLongClickListener(contentLongClickListener);
            holder.rightIconView.setOnClickListener(iconClickListener);
            holder.leftIconView.setOnClickListener(iconClickListener);
            holder.rightIconView.setOnLongClickListener(iconLongClickListener);
            holder.leftIconView.setOnLongClickListener(iconLongClickListener);
        }

        holder.warning.setOnClickListener(new DebouncedOnClickListener() {
            public void onDebouncedClick(View view) {
                if (MessageListAdapter.this.getItemHandlerListener() != null) {
                    MessageListAdapter.this.getItemHandlerListener().onWarningViewClick(position, data.getMessage(), view);
                }

            }
        });
    }

    protected void bindView(View v, int position, final UIMessage data) {
        if (data != null) {
            final MessageListAdapter.ViewHolder holder = (MessageListAdapter.ViewHolder)v.getTag();
            if (holder == null) {
                RLog.e("MessageListAdapter", "view holder is null !");
            } else {
                Object provider;
                ProviderTag tag;
                if (this.getNeedEvaluate(data)) {
                    provider = RongContext.getInstance().getEvaluateProvider();
                    tag = RongContext.getInstance().getMessageProviderTag(data.getContent().getClass());
                } else {
                    if (RongContext.getInstance() == null || data.getContent() == null) {
                        RLog.e("MessageListAdapter", "Message is null !");
                        return;
                    }

                    provider = RongContext.getInstance().getMessageTemplate(data.getContent().getClass());
                    if (provider == null) {
                        provider = RongContext.getInstance().getMessageTemplate(UnknownMessage.class);
                        tag = RongContext.getInstance().getMessageProviderTag(UnknownMessage.class);
                    } else {
                        tag = RongContext.getInstance().getMessageProviderTag(data.getContent().getClass());
                    }

                    if (provider == null) {
                        RLog.e("MessageListAdapter", data.getObjectName() + " message provider not found !");
                        return;
                    }
                }

                if (data.getContent() != null && data.getContent().isDestruct() && data.getMessage() != null && data.getMessage().getReadTime() > 0L && data.getMessage().getContent() != null) {
                    if (data.getMessageDirection() == MessageDirection.SEND) {
                        DestructManager.getInstance().deleteMessage(data.getMessage());
                        this.remove(position);
                        this.notifyDataSetChanged();
                        return;
                    }

                    long readTime = data.getMessage().getReadTime();
                    MessageContent messageContent = data.getMessage().getContent();
                    long delay = (System.currentTimeMillis() - readTime) / 1000L;
                    if (delay >= messageContent.getDestructTime()) {
                        DestructionTaskManager.getInstance().deleteMessage(data.getMessage());
                        this.remove(position);
                        this.notifyDataSetChanged();
                        return;
                    }
                }

                final View v1;
                View v2;
                try {
                    v2 = holder.contentView.inflate((IContainerItemProvider)provider);
                } catch (Exception var14) {
                    RLog.e("MessageListAdapter", "bindView contentView inflate error", var14);
                    provider = RongContext.getInstance().getMessageTemplate(UnknownMessage.class);
                    tag = RongContext.getInstance().getMessageProviderTag(UnknownMessage.class);
                    v2 = holder.contentView.inflate((IContainerItemProvider)provider);
                }

                v1 = v2;
                ((IContainerItemProvider)provider).bindView(v1, position, data);
                if (tag == null) {
                    RLog.e("MessageListAdapter", "Can not find ProviderTag for " + data.getObjectName());
                } else {
                    if (tag.hide()) {
                        holder.contentView.setVisibility(View.GONE);
                        holder.time.setVisibility(View.GONE);
                        holder.nameView.setVisibility(View.GONE);
                        holder.leftIconView.setVisibility(View.GONE);
                        holder.rightIconView.setVisibility(View.GONE);
                        holder.layoutItem.setVisibility(View.GONE);
                        holder.layoutItem.setPadding(0, 0, 0, 0);
                    } else {
                        holder.contentView.setVisibility(View.VISIBLE);
                        holder.layoutItem.setVisibility(View.VISIBLE);
                        holder.layoutItem.setPadding(RongUtils.dip2px(8.0F), RongUtils.dip2px(6.0F), RongUtils.dip2px(8.0F), RongUtils.dip2px(6.0F));
                    }

                    UserInfo userInfo;
                    if (data.getMessageDirection() == MessageDirection.SEND) {
                        if (tag.showPortrait()) {
                            holder.rightIconView.setVisibility(View.VISIBLE);
                            holder.leftIconView.setVisibility(View.GONE);
                        } else {
                            holder.leftIconView.setVisibility(View.GONE);
                            holder.rightIconView.setVisibility(View.GONE);
                        }

                        if (!tag.centerInHorizontal()) {
                            this.setGravity(holder.layout, 5);
                            holder.contentView.containerViewRight();
                            holder.nameView.setGravity(5);
                        } else {
                            this.setGravity(holder.layout, 17);
                            holder.contentView.containerViewCenter();
                            holder.nameView.setGravity(1);
                            holder.contentView.setBackgroundColor(0);
                        }

                        boolean readRec = false;

                        try {
                            readRec = this.mContext.getResources().getBoolean(bool.rc_read_receipt);
                        } catch (NotFoundException var13) {
                            RLog.e("MessageListAdapter", "bindView rc_read_receipt not configure in rc_config.xml", var13);
                        }

                        if (data.getSentStatus() == SentStatus.SENDING) {
                            if (tag.showProgress()) {
                                holder.progressBar.setVisibility(View.VISIBLE);
                            } else {
                                holder.progressBar.setVisibility(View.GONE);
                            }

                            holder.warning.setVisibility(View.GONE);
                            holder.readReceipt.setVisibility(View.GONE);
                        } else if (data.getSentStatus() == SentStatus.FAILED) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.VISIBLE);
                            holder.readReceipt.setVisibility(View.GONE);
                        } else if (data.getSentStatus() == SentStatus.SENT) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.GONE);
                            holder.readReceipt.setVisibility(View.GONE);
                        } else if (readRec && data.getSentStatus() == SentStatus.READ) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.GONE);
                            if (data.getConversationType().equals(ConversationType.PRIVATE) && tag.showReadState()) {
                                holder.readReceipt.setVisibility(View.VISIBLE);
                            } else {
                                holder.readReceipt.setVisibility(View.GONE);
                            }
                        } else {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.GONE);
                            holder.readReceipt.setVisibility(View.GONE);
                        }

                        holder.readReceiptRequest.setVisibility(View.GONE);
                        holder.readReceiptStatus.setVisibility(View.GONE);
                        if (readRec && RongContext.getInstance().isReadReceiptConversationType(data.getConversationType()) && (data.getConversationType().equals(ConversationType.GROUP) || data.getConversationType().equals(ConversationType.DISCUSSION))) {
                            if (this.allowReadReceiptRequest(data.getMessage()) && !TextUtils.isEmpty(data.getUId())) {
                                boolean isLastSentMessage = true;

                                for(int i = position + 1; i < this.getCount(); ++i) {
                                    if (((UIMessage)this.getItem(i)).getMessageDirection() == MessageDirection.SEND) {
                                        isLastSentMessage = false;
                                        break;
                                    }
                                }

                                long serverTime = System.currentTimeMillis() - RongIMClient.getInstance().getDeltaTime();
                                if (serverTime - data.getSentTime() < readReceiptRequestInterval * 1000L && isLastSentMessage && (data.getReadReceiptInfo() == null || !data.getReadReceiptInfo().isReadReceiptMessage())) {
                                    holder.readReceiptRequest.setVisibility(View.VISIBLE);
                                }
                            }

                            if (this.allowReadReceiptRequest(data.getMessage()) && data.getReadReceiptInfo() != null && data.getReadReceiptInfo().isReadReceiptMessage()) {
                                if (data.getReadReceiptInfo().getRespondUserIdList() != null) {
                                    holder.readReceiptStatus.setText(String.format(v1.getResources().getString(string.rc_read_receipt_status), data.getReadReceiptInfo().getRespondUserIdList().size()));
                                } else {
                                    holder.readReceiptStatus.setText(String.format(v1.getResources().getString(string.rc_read_receipt_status), 0));
                                }

                                holder.readReceiptStatus.setVisibility(View.VISIBLE);
                            }
                        }

                        holder.nameView.setVisibility(View.GONE);
                        holder.readReceiptRequest.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                RongIMClient.getInstance().sendReadReceiptRequest(data.getMessage(), new OperationCallback() {
                                    public void onSuccess() {
                                        ReadReceiptInfo readReceiptInfo = data.getReadReceiptInfo();
                                        if (readReceiptInfo == null) {
                                            readReceiptInfo = new ReadReceiptInfo();
                                            data.setReadReceiptInfo(readReceiptInfo);
                                        }

                                        readReceiptInfo.setIsReadReceiptMessage(true);
                                        holder.readReceiptStatus.setText(String.format(v1.getResources().getString(string.rc_read_receipt_status), 0));
                                        holder.readReceiptRequest.setVisibility(View.GONE);
                                        holder.readReceiptStatus.setVisibility(View.VISIBLE);
                                    }

                                    public void onError(ErrorCode errorCode) {
                                        RLog.e("MessageListAdapter", "sendReadReceiptRequest failed, errorCode = " + errorCode);
                                    }
                                });
                            }
                        });
                        holder.readReceiptStatus.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (MessageListAdapter.this.mOnItemHandlerListener != null) {
                                    MessageListAdapter.this.mOnItemHandlerListener.onReadReceiptStateClick(data.getMessage());
                                }

                            }
                        });
                        if (!tag.showWarning()) {
                            holder.warning.setVisibility(View.GONE);
                        }
                    } else {
                        if (tag.showPortrait()) {
                            holder.rightIconView.setVisibility(View.GONE);
                            holder.leftIconView.setVisibility(View.VISIBLE);
                        } else {
                            holder.leftIconView.setVisibility(View.GONE);
                            holder.rightIconView.setVisibility(View.GONE);
                        }

                        if (!tag.centerInHorizontal()) {
                            this.setGravity(holder.layout, 3);
                            holder.contentView.containerViewLeft();
                            holder.nameView.setGravity(3);
                        } else {
                            this.setGravity(holder.layout, 17);
                            holder.contentView.containerViewCenter();
                            holder.nameView.setGravity(1);
                            holder.contentView.setBackgroundColor(0);
                        }

                        holder.progressBar.setVisibility(View.GONE);
                        holder.warning.setVisibility(View.GONE);
                        holder.readReceipt.setVisibility(View.GONE);
                        holder.readReceiptRequest.setVisibility(View.GONE);
                        holder.readReceiptStatus.setVisibility(View.GONE);
                        holder.nameView.setVisibility(View.VISIBLE);
                        if (data.getConversationType() != ConversationType.PRIVATE && tag.showSummaryWithName() && data.getConversationType() != ConversationType.PUBLIC_SERVICE && data.getConversationType() != ConversationType.APP_PUBLIC_SERVICE) {
                            userInfo = data.getUserInfo();
                            if (data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                                if (userInfo == null && data.getMessage() != null && data.getMessage().getContent() != null) {
                                    userInfo = data.getMessage().getContent().getUserInfo();
                                }

                                if (userInfo != null) {
                                    holder.nameView.setText(userInfo.getName());
                                } else {
                                    holder.nameView.setText(data.getSenderUserId());
                                }
                            } else if (data.getConversationType() == ConversationType.GROUP) {
                                GroupUserInfo groupUserInfo = RongUserInfoManager.getInstance().getGroupUserInfo(data.getTargetId(), data.getSenderUserId());
                                if (groupUserInfo != null && !TextUtils.isEmpty(groupUserInfo.getNickname())) {
                                    holder.nameView.setText(groupUserInfo.getNickname());
                                } else {
                                    if (userInfo == null) {
                                        userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                                    }

                                    if (userInfo == null) {
                                        holder.nameView.setText(data.getSenderUserId());
                                    } else {
                                        holder.nameView.setText(userInfo.getName());
                                    }
                                }
                            } else {
                                if (userInfo == null) {
                                    userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                                }

                                if (userInfo == null) {
                                    holder.nameView.setText(data.getSenderUserId());
                                } else {
                                    holder.nameView.setText(userInfo.getName());
                                }
                            }
                        } else {
                            holder.nameView.setVisibility(View.GONE);
                        }
                    }

                    ConversationKey mKey;
                    Uri portrait;
                    PublicServiceProfile publicServiceProfile;
                    if (holder.rightIconView.getVisibility() == View.VISIBLE) {
                        userInfo = data.getUserInfo();
                        portrait = null;
                        if (data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && data.getUserInfo() != null && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
                            }

//                            holder.rightIconView.setAvatar(portrait != null ? portrait.toString() : null, 0);
                            Glide.with(mContext).load(portrait).into(holder.rightIconView);
                        } else if ((data.getConversationType().equals(ConversationType.PUBLIC_SERVICE) || data.getConversationType().equals(ConversationType.APP_PUBLIC_SERVICE)) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();

                                Glide.with(mContext).load(portrait).into(holder.rightIconView);
//                                holder.rightIconView.setAvatar(portrait != null ? portrait.toString() : null, 0);
                            } else {
                                mKey = ConversationKey.obtain(data.getTargetId(), data.getConversationType());
                                publicServiceProfile = RongContext.getInstance().getPublicServiceInfoFromCache(mKey.getKey());
                                portrait = publicServiceProfile.getPortraitUri();
                                Glide.with(mContext).load(portrait).into(holder.rightIconView);
//                                holder.rightIconView.setAvatar(portrait != null ? portrait.toString() : null, 0);
                            }
                        } else if (!TextUtils.isEmpty(data.getSenderUserId())) {
                            if (userInfo == null) {
                                userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                            }

                            if (userInfo != null && userInfo.getPortraitUri() != null) {
//                                holder.rightIconView.setAvatar(userInfo.getPortraitUri().toString(), 0);
                            } else {
//                                holder.rightIconView.setAvatar((String)null, 0);
                            }
                            Glide.with(mContext).load(portrait).into(holder.rightIconView);
                        }
                    } else if (holder.leftIconView.getVisibility() == View.VISIBLE) {
                        userInfo = data.getUserInfo();
                        if (data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo == null && data.getMessage() != null && data.getMessage().getContent() != null) {
                                userInfo = data.getMessage().getContent().getUserInfo();
                            }

                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
//                                holder.leftIconView.setAvatar(portrait != null ? portrait.toString() : null, drawable.rc_cs_default_portrait);
                                Glide.with(mContext).load(portrait).into(holder.leftIconView);
                            }
                        } else if ((data.getConversationType().equals(ConversationType.PUBLIC_SERVICE) || data.getConversationType().equals(ConversationType.APP_PUBLIC_SERVICE)) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
//                                holder.leftIconView.setAvatar(portrait != null ? portrait.toString() : null, 0);
                                Glide.with(mContext).load(portrait).into(holder.leftIconView);
                            } else {
                                publicServiceProfile = null;
                                mKey = ConversationKey.obtain(data.getTargetId(), data.getConversationType());
                                if (mKey != null) {
                                    publicServiceProfile = RongContext.getInstance().getPublicServiceInfoFromCache(mKey.getKey());
                                }

                                if (publicServiceProfile != null && publicServiceProfile.getPortraitUri() != null) {
//                                    holder.leftIconView.setAvatar(publicServiceProfile.getPortraitUri().toString(), 0);
                                } else {
//                                    holder.leftIconView.setAvatar((String)null, 0);
                                }
                                Glide.with(mContext).load(publicServiceProfile.getPortraitUri().toString()).into(holder.leftIconView);
                            }
                        } else if (!TextUtils.isEmpty(data.getSenderUserId())) {
                            if (userInfo == null) {
                                userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                            }

                            if (userInfo != null && userInfo.getPortraitUri() != null) {
//                                holder.leftIconView.setAvatar(userInfo.getPortraitUri().toString(), 0);
                            } else {
//                                holder.leftIconView.setAvatar((String)null, 0);
                            }
                            Glide.with(mContext).load(userInfo.getPortraitUri().toString()).into(holder.leftIconView);
                        }
                    }

                    this.bindViewClickEvent(v, v1, position, data);
                    if (tag.hide()) {
                        holder.time.setVisibility(View.GONE);
                    } else {
                        if (!this.timeGone) {
                            String time = RongDateUtils.getConversationFormatDate(data.getSentTime(), v1.getContext());
                            holder.time.setText(time);
                            if (position == 0) {
                                if (data.getMessage() != null && data.getMessage().getContent() != null && data.getMessage().getContent() instanceof HistoryDividerMessage) {
                                    holder.time.setVisibility(View.GONE);
                                } else {
                                    holder.time.setVisibility(View.VISIBLE);
                                }
                            } else {
                                UIMessage pre = (UIMessage)this.getItem(position - 1);
                                if (RongDateUtils.isShowChatTime(data.getSentTime(), pre.getSentTime(), 180)) {
                                    holder.time.setVisibility(View.VISIBLE);
                                } else {
                                    holder.time.setVisibility(View.GONE);
                                }
                            }
                        }

                        if (this.isShowCheckbox() && this.allowShowCheckButton(data.getMessage())) {
                            holder.checkboxLayout.setVisibility(View.VISIBLE);
                            holder.message_check.setFocusable(false);
                            holder.message_check.setClickable(false);
                            holder.message_check.setChecked(data.isChecked());
                        } else {
                            holder.checkboxLayout.setVisibility(View.GONE);
                            data.setChecked(false);
                        }

                        if (this.messageCheckedChanged != null) {
                            this.messageCheckedChanged.onCheckedEnable(this.getCheckedMessage().size() > 0);
                        }

                    }
                }
            }
        }
    }

    protected void setGravity(View view, int gravity) {
        LayoutParams params = (LayoutParams)view.getLayoutParams();
        params.gravity = gravity;
    }

    public void setEvaluateForRobot(boolean needEvaluate) {
        this.evaForRobot = needEvaluate;
    }

    public void setRobotMode(boolean robotMode) {
        this.robotMode = robotMode;
    }

    public boolean allowReadReceiptRequest(Message message) {
        return message != null && message.getContent() != null && message.getContent() instanceof TextMessage;
    }

    protected boolean allowShowCheckButton(Message message) {
        if (message != null) {
            MessageContent messageContent = message.getContent();
            if (messageContent != null) {
                return !(messageContent instanceof InformationNotificationMessage) && !(messageContent instanceof UnknownMessage) && !(messageContent instanceof GroupNotificationMessage) && !(messageContent instanceof RecallNotificationMessage) && !(messageContent instanceof RealTimeLocationJoinMessage);
            }
        }

        return true;
    }

    private abstract class NoDoubleClickListener implements OnClickListener {
        public static final int MIN_CLICK_DELAY_TIME = 500;
        public long lastClickTime;

        private NoDoubleClickListener() {
            this.lastClickTime = 0L;
        }

        public abstract void onNoDoubleClick(View var1);
    }

    public interface OnItemHandlerListener {
        boolean onWarningViewClick(int var1, Message var2, View var3);

        void onReadReceiptStateClick(Message var1);

        void onMessageClick(int var1, Message var2, View var3);
    }

    public interface OnMessageCheckedChanged {
        void onCheckedEnable(boolean var1);
    }

    public interface OnSelectedCountDidExceed {
        void onSelectedCountDidExceed();
    }

    protected class ViewHolder {
        public CircleImageView leftIconView;
        public CircleImageView rightIconView;
        public TextView nameView;
        public ProviderContainerView contentView;
        public ProgressBar progressBar;
        public ImageView warning;
        public TextView readReceipt;
        public TextView readReceiptRequest;
        public TextView readReceiptStatus;
        public ViewGroup layout;
        public TextView time;
        public TextView sentStatus;
        public RelativeLayout layoutItem;
        public CheckBox message_check;
        public LinearLayout checkboxLayout;

        protected ViewHolder() {
        }
    }
}
