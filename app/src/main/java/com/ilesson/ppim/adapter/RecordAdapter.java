package com.ilesson.ppim.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ConversationActivity;
import com.ilesson.ppim.activity.NoteActivity;
import com.ilesson.ppim.utils.Dateuitls;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Message> resultList = new ArrayList<>();
    public static int editIndex;
    public int recordIndex;
    private HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
    private RecyclerView recyclerView;
    private NoteActivity noteActivity;
    private List<EditText> editTexts = new ArrayList<>();
    private int screenWidth;
    private String targetName;

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

    public RecordAdapter(Context context, List<Message> datas,String targetName) {
        mContext = context;
        this.targetName = targetName;
        mLayoutInflater = LayoutInflater.from(context);
        resultList = datas;
        editIndex = resultList.size();
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public RecordAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public void setNoteActivity(NoteActivity noteActivity) {
        this.noteActivity = noteActivity;
    }

    public List<Message> getResultList() {
        return resultList;
    }

    public void setResultList(List<Message> resultList) {
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
        Message message = resultList.get(position);
        if(message.getContent() instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message.getContent();
            Uri uri = RongContext.getInstance().getConversationTemplate(message.getConversationType().getName()).getPortraitUri(message.getSenderUserId());
            Glide.with(mContext).load(SPUtils.get(message.getSenderUserId(),"")).into(viewHolder.imageView);
            String title = RongContext.getInstance().getConversationTemplate(message.getConversationType().getName()).getTitle(message.getSenderUserId());
            viewHolder.titleView.setText(SPUtils.get(message.getSenderUserId()+"name",""));
            viewHolder.messageView.setText(textMessage.getContent());
            viewHolder.timeView.setText(Dateuitls.QQFormatTime(message.getSentTime()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RongIM.getInstance().startConversation(mContext,message.getConversationType(),message.getTargetId(), ConversationActivity.title,message.getSentTime());
                }
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
        CircleImageView imageView;
        ViewHolder(View view) {
            super(view);
            timeView = view.findViewById(R.id.time);
            titleView = view.findViewById(R.id.title);
            messageView = view.findViewById(R.id.message);
            imageView = view.findViewById(R.id.icon);
        }
    }

}
