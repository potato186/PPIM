package com.ilesson.ppim.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ConversationActivity;
import com.ilesson.ppim.activity.NoteActivity;
import com.ilesson.ppim.activity.SearchActivity;
import com.ilesson.ppim.entity.ConversationInfo;
import com.ilesson.ppim.entity.GroupInfo;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.SearchInfo;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.TextUtil;
import com.ilesson.ppim.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

public class SearchItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<SearchInfo> resultList = new ArrayList<>();
    public static int editIndex;
    public int recordIndex;
    private HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
    private RecyclerView recyclerView;
    private NoteActivity noteActivity;
    private List<EditText> editTexts = new ArrayList<>();
    private int screenWidth;
    private String keyWords;
//    public enum ITEM_TYPE {
//        ITEM_TYPE_TEXT,
//        ITEM_TYPE_IMAGE,
//        ITEM_TYPE_LOCATION,
//        ITEM_TYPE_FILE,
//        ITEM_TYPE_VOICE
//    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public SearchItemAdapter(Context context, List<SearchInfo> datas, String keyWords) {
        mContext = context;
        this.keyWords = keyWords;
        mLayoutInflater = LayoutInflater.from(context);
        resultList = datas;
        editIndex = resultList.size();
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public SearchItemAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public void setNoteActivity(NoteActivity noteActivity) {
        this.noteActivity = noteActivity;
    }

    public List<SearchInfo> getResultList() {
        return resultList;
    }

    public void setResultList(List<SearchInfo> resultList) {
        this.resultList = resultList;
        editIndex = resultList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.record_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        SearchInfo searchInfo = resultList.get(position);
        if(position>=resultList.size()-1){
            viewHolder.tagLine.setVisibility(View.GONE);
        }else {
            viewHolder.tagLine.setVisibility(View.VISIBLE);
        }
        if (searchInfo instanceof PPUserInfo){
            PPUserInfo userInfo = (PPUserInfo) searchInfo;
            Glide.with(mContext).load(userInfo.getIcon()).into(viewHolder.imageView);
            String name = userInfo.getName();
            String tagName = userInfo.getNick();
            if(null==tagName){
                tagName="";
            }
            if(name.contains(keyWords)){
                if(TextUtils.isEmpty(tagName)){
                    viewHolder.messageView.setVisibility(View.GONE);
                    viewHolder.titleView.setText(TextUtil.getKeyWordsColorString(mContext,name,keyWords));
                }else{
                    viewHolder.titleView.setText(tagName);
                    viewHolder.messageView.setVisibility(View.VISIBLE);
                    viewHolder.messageView.setText(TextUtil.getKeyWordsColorString(mContext,String.format(mContext.getString(R.string.user_nick),name),keyWords));
                }
            }
            else if(tagName.contains(keyWords)){
                viewHolder.messageView.setVisibility(View.GONE);
                viewHolder.titleView.setText(TextUtil.getKeyWordsColorString(mContext,tagName,keyWords));
            }else{
                viewHolder.messageView.setVisibility(View.VISIBLE);
                viewHolder.messageView.setText(TextUtil.getKeyWordsColorString(mContext,String.format(mContext.getString(R.string.pp_number),userInfo.getPhone()),keyWords));
                if(TextUtils.isEmpty(tagName)){
                    viewHolder.titleView.setText(name);
                }else{
                    viewHolder.titleView.setText(tagName);
                }
            }
            viewHolder.itemView.setOnClickListener(v -> {
                RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.PRIVATE,userInfo.getPhone(),viewHolder.titleView.getText().toString());
            });
        }else if(searchInfo instanceof GroupInfo){
            GroupInfo groupInfo = (GroupInfo) searchInfo;
            String name = groupInfo.getName();
            String tagName = groupInfo.getTag();
            Glide.with(mContext).load(groupInfo.getIcon()).into(viewHolder.imageView);
            if(null==tagName){
                tagName="";
            }
            if(name.contains(keyWords)){
                if(TextUtils.isEmpty(tagName)){
                    viewHolder.messageView.setVisibility(View.GONE);
                    viewHolder.titleView.setText(TextUtil.getKeyWordsColorString(mContext,name,keyWords));
                }else{
                    viewHolder.titleView.setText(tagName);
                    viewHolder.messageView.setVisibility(View.VISIBLE);
                    viewHolder.messageView.setText(TextUtil.getKeyWordsColorString(mContext,String.format(mContext.getString(R.string.group_nick),name),keyWords));
                }
            }
            else if(tagName.contains(keyWords)){
                viewHolder.messageView.setVisibility(View.GONE);
                viewHolder.titleView.setText(TextUtil.getKeyWordsColorString(mContext,tagName,keyWords));
            }else{
                viewHolder.messageView.setVisibility(View.VISIBLE);
                viewHolder.messageView.setText(TextUtil.getKeyWordsColorString(mContext,String.format(mContext.getString(R.string.group_contain),groupInfo.getUserName()),keyWords));
                if(TextUtils.isEmpty(tagName)){
                    viewHolder.titleView.setText(name);
                }else{
                    viewHolder.titleView.setText(tagName);
                }
            }
            viewHolder.itemView.setOnClickListener(v -> {
                RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.GROUP,groupInfo.getId(),viewHolder.titleView.getText().toString());
            });
        }else if(searchInfo instanceof ConversationInfo){
            ConversationInfo conversationInfo = (ConversationInfo) searchInfo;
            String name = conversationInfo.getConversationTitle();
            viewHolder.messageView.setVisibility(View.GONE);
            Glide.with(mContext).load(conversationInfo.getPortraitUrl()).into(viewHolder.imageView);
            viewHolder.titleView.setText(name);
            viewHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra(ConversationActivity.CONVERSATION_TYPE, conversationInfo.getType());
                intent.putExtra(ConversationActivity.TARGET_ID,conversationInfo.getTargetId());
                intent.putExtra(SearchActivity.SEARCH_TYPE,SearchActivity.SEARCH_RECORD_WITH_GARGET);
                intent.putExtra(SearchActivity.SEARCH_KEY,keyWords);
                intent.putExtra(ConversationActivity.TARGET_NAME, name);
                mContext.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView messageView;
        TextView timeView;
        View tagLine;
        CircleImageView imageView;
        ViewHolder(View view) {
            super(view);
            timeView = view.findViewById(R.id.time);
            tagLine = view.findViewById(R.id.tag_line);
            titleView = view.findViewById(R.id.title);
            messageView = view.findViewById(R.id.message);
            imageView = view.findViewById(R.id.icon);
        }
    }

}
