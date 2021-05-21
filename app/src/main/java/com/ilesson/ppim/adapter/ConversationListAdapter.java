package com.ilesson.ppim.adapter;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ilesson.ppim.view.CircleImageView;

import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imkit.R.dimen;
import io.rong.imkit.R.drawable;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.Event.MessageDeleteEvent;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIConversation.UnreadRemindType;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public class ConversationListAdapter extends BaseAdapter<UIConversation> {
    private static final String TAG = "ConversationListAdapter";
    LayoutInflater mInflater;
    Context mContext;
    private com.ilesson.ppim.adapter.ConversationListAdapter.OnPortraitItemClick mOnPortraitItemClick;

    public long getItemId(int position) {
        UIConversation conversation = (UIConversation)this.getItem(position);
        return conversation == null ? 0L : (long)conversation.hashCode();
    }

    public ConversationListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    public int findGatheredItem(ConversationType type) {
        int index = this.getCount();
        int position = -1;

        while(index-- > 0) {
            UIConversation uiConversation = (UIConversation)this.getItem(index);
            if (uiConversation.getConversationType().equals(type)) {
                position = index;
                break;
            }
        }

        return position;
    }

    public int findPosition(ConversationType type, String targetId) {
        int index = this.getCount();
        int position = -1;

        while(index-- > 0) {
            if (((UIConversation)this.getItem(index)).getConversationType().equals(type) && ((UIConversation)this.getItem(index)).getConversationTargetId().equals(targetId)) {
                position = index;
                break;
            }
        }

        return position;
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View result = this.mInflater.inflate(layout.rc_item_conversation, (ViewGroup)null);
        com.ilesson.ppim.adapter.ConversationListAdapter.ViewHolder holder = new com.ilesson.ppim.adapter.ConversationListAdapter.ViewHolder();
        holder.layout = this.findViewById(result, id.rc_item_conversation);
        holder.leftImageLayout = this.findViewById(result, id.rc_item1);
        holder.rightImageLayout = this.findViewById(result, id.rc_item2);
        holder.leftUnReadView = this.findViewById(result, id.rc_unread_view_left);
        holder.rightUnReadView = this.findViewById(result, id.rc_unread_view_right);
        holder.leftImageView = (CircleImageView)this.findViewById(result, id.rc_left);
        holder.rightImageView = (CircleImageView)this.findViewById(result, id.rc_right);
        holder.contentView = (ProviderContainerView)this.findViewById(result, id.rc_content);
        holder.unReadMsgCount = (TextView)this.findViewById(result, id.rc_unread_message);
        holder.unReadMsgCountRight = (TextView)this.findViewById(result, id.rc_unread_message_right);
        holder.unReadMsgCountIcon = (ImageView)this.findViewById(result, id.rc_unread_message_icon);
        holder.unReadMsgCountRightIcon = (ImageView)this.findViewById(result, id.rc_unread_message_icon_right);
        result.setTag(holder);
        return result;
    }

    protected void bindView(View v, int position, final UIConversation data) {
        com.ilesson.ppim.adapter.ConversationListAdapter.ViewHolder holder = (com.ilesson.ppim.adapter.ConversationListAdapter.ViewHolder)v.getTag();
        if (data != null) {
            IContainerItemProvider provider = RongContext.getInstance().getConversationTemplate(data.getConversationType().getName());
            if (provider == null) {
                RLog.e("ConversationListAdapter", "provider is null");
            } else {
                View view = holder.contentView.inflate(provider);
                provider.bindView(view, position, data);
                if (data.isTop()) {
                    holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(drawable.rc_item_top_list_selector));
                } else {
                    holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(drawable.rc_item_list_selector));
                }

                ConversationProviderTag tag = RongContext.getInstance().getConversationProviderTag(data.getConversationType().getName());
                int defaultId;
                if (data.getConversationType().equals(ConversationType.GROUP)) {
                    defaultId = drawable.rc_default_group_portrait;
                } else if (data.getConversationType().equals(ConversationType.DISCUSSION)) {
                    defaultId = drawable.rc_default_discussion_portrait;
                } else {
                    defaultId = drawable.rc_default_portrait;
                }

                if (tag.portraitPosition() == 1) {
                    holder.leftImageLayout.setVisibility(View.VISIBLE);
                    holder.leftImageLayout.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick != null) {
                                com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemClick(v, data);
                            }

                        }
                    });
                    holder.leftImageLayout.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            if (com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick != null) {
                                com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemLongClick(v, data);
                            }

                            return true;
                        }
                    });
                    if (data.getConversationGatherState()) {
//                        holder.leftImageView.setAvatar((String)null, defaultId);
                    } else if (data.getIconUrl() != null) {
//                        holder.leftImageView.setAvatar(data.getIconUrl().toString(), defaultId);
                        Glide.with(mContext).load(data.getIconUrl().toString()).into(holder.leftImageView);
                    } else {
//                        holder.leftImageView.setAvatar((String)null, defaultId);
                    }

                    if (data.getUnReadMessageCount() > 0) {
                        holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                        this.setUnReadViewLayoutParams(holder.leftUnReadView, data.getUnReadType());
                        if (data.getUnReadType().equals(UnreadRemindType.REMIND_WITH_COUNTING)) {
                            if (data.getUnReadMessageCount() > 99) {
                                holder.unReadMsgCount.setText(this.mContext.getResources().getString(string.rc_message_unread_count));
                            } else {
                                holder.unReadMsgCount.setText(Integer.toString(data.getUnReadMessageCount()));
                            }

                            holder.unReadMsgCount.setVisibility(View.VISIBLE);
                            holder.unReadMsgCountIcon.setImageResource(drawable.rc_unread_count_bg);
                        } else {
                            holder.unReadMsgCount.setVisibility(View.GONE);
                            holder.unReadMsgCountIcon.setImageResource(drawable.rc_unread_remind_list_count);
                        }
                    } else {
                        holder.unReadMsgCountIcon.setVisibility(View.GONE);
                        holder.unReadMsgCount.setVisibility(View.GONE);
                    }

                    holder.rightImageLayout.setVisibility(View.GONE);
                } else if (tag.portraitPosition() == 2) {
                    holder.rightImageLayout.setVisibility(View.VISIBLE);
                    holder.rightImageLayout.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick != null) {
                                com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemClick(v, data);
                            }

                        }
                    });
                    holder.rightImageLayout.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            if (com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick != null) {
                                com.ilesson.ppim.adapter.ConversationListAdapter.this.mOnPortraitItemClick.onPortraitItemLongClick(v, data);
                            }

                            return true;
                        }
                    });
                    if (data.getConversationGatherState()) {
//                        holder.rightImageView.setAvatar((String)null, defaultId);
                    } else if (data.getIconUrl() != null) {
//                        holder.rightImageView.setAvatar(data.getIconUrl().toString(), defaultId);
                        Glide.with(mContext).load(data.getIconUrl().toString()).into(holder.rightImageView);
                    } else {
//                        holder.rightImageView.setAvatar((String)null, defaultId);
                    }

                    if (data.getUnReadMessageCount() > 0) {
                        holder.unReadMsgCountRightIcon.setVisibility(View.VISIBLE);
                        this.setUnReadViewLayoutParams(holder.rightUnReadView, data.getUnReadType());
                        if (data.getUnReadType().equals(UnreadRemindType.REMIND_WITH_COUNTING)) {
                            holder.unReadMsgCount.setVisibility(View.VISIBLE);
                            if (data.getUnReadMessageCount() > 99) {
                                holder.unReadMsgCountRight.setText(this.mContext.getResources().getString(string.rc_message_unread_count));
                            } else {
                                holder.unReadMsgCountRight.setText(Integer.toString(data.getUnReadMessageCount()));
                            }

                            holder.unReadMsgCountRightIcon.setImageResource(drawable.rc_unread_count_bg);
                        } else {
                            holder.unReadMsgCount.setVisibility(View.GONE);
                            holder.unReadMsgCountRightIcon.setImageResource(drawable.rc_unread_remind_without_count);
                        }
                    } else {
                        holder.unReadMsgCountIcon.setVisibility(View.GONE);
                        holder.unReadMsgCount.setVisibility(View.GONE);
                    }

                    holder.leftImageLayout.setVisibility(View.GONE);
                } else {
                    if (tag.portraitPosition() != 3) {
                        throw new IllegalArgumentException("the portrait position is wrong!");
                    }

                    holder.rightImageLayout.setVisibility(View.GONE);
                    holder.leftImageLayout.setVisibility(View.GONE);
                }

                MessageContent content = data.getMessageContent();
                if (content != null && content.isDestruct()) {
                    RongIMClient.getInstance().getMessage(data.getLatestMessageId(), new ResultCallback<Message>() {
                        public void onSuccess(Message message) {
                            if (message == null) {
                                EventBus.getDefault().post(new MessageDeleteEvent(new int[]{data.getLatestMessageId()}));
                            }

                        }

                        public void onError(ErrorCode e) {
                        }
                    });
                }

            }
        }
    }

    protected void setUnReadViewLayoutParams(View view, UnreadRemindType type) {
        MarginLayoutParams params = (MarginLayoutParams)view.getLayoutParams();
        Context context = view.getContext();
        if (type == UnreadRemindType.REMIND_WITH_COUNTING) {
            params.width = (int)context.getResources().getDimension(dimen.rc_dimen_size_18);
            params.height = (int)context.getResources().getDimension(dimen.rc_dimen_size_18);
            params.leftMargin = (int)this.mContext.getResources().getDimension(dimen.rc_dimen_size_44);
            params.topMargin = (int)context.getResources().getDimension(dimen.rc_dimen_size_5);
        } else {
            params.width = (int)context.getResources().getDimension(dimen.rc_dimen_size_9);
            params.height = (int)context.getResources().getDimension(dimen.rc_dimen_size_9);
            params.leftMargin = (int)context.getResources().getDimension(dimen.rc_dimen_size_50);
            params.topMargin = (int)context.getResources().getDimension(dimen.rc_dimen_size_7);
        }

        view.setLayoutParams(params);
    }

    public void setOnPortraitItemClick(com.ilesson.ppim.adapter.ConversationListAdapter.OnPortraitItemClick onPortraitItemClick) {
        this.mOnPortraitItemClick = onPortraitItemClick;
    }

    public interface OnPortraitItemClick {
        void onPortraitItemClick(View var1, UIConversation var2);

        boolean onPortraitItemLongClick(View var1, UIConversation var2);
    }

    protected class ViewHolder {
        public View layout;
        public View leftImageLayout;
        public View rightImageLayout;
        public View leftUnReadView;
        public View rightUnReadView;
        public CircleImageView leftImageView;
        public TextView unReadMsgCount;
        public ImageView unReadMsgCountIcon;
        public CircleImageView rightImageView;
        public TextView unReadMsgCountRight;
        public ImageView unReadMsgCountRightIcon;
        public ProviderContainerView contentView;

        protected ViewHolder() {
        }
    }
}
