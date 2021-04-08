package com.ilesson.ppim.contactview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.FriendDetailActivity;
import com.ilesson.ppim.activity.PayScoreActivity;
import com.ilesson.ppim.entity.PPUserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.PayScoreActivity.QR_PAY;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<PPUserInfo> mContactNames; // 联系人名称字符串数组
    private List<PPUserInfo> selects; // 联系人名称字符串数组
    private List<String> mContactList; // 联系人名称List（转换成拼音）
    private List<Contact> resultList; // 最终结果（包含分组的字母）
    private List<String> characterList; // 字母List
    private HashMap<Integer, Boolean> isSelected= new HashMap<Integer, Boolean>();;
    public enum ITEM_TYPE {
        ITEM_TYPE_CHARACTER,
        ITEM_TYPE_CONTACT
    }

    public ContactAdapter(Context context, List<PPUserInfo> contactNames) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mContactNames = contactNames;
        handleContact();
        selects = new ArrayList<>();
        for (int i = 0; i < 100000; i++){
            isSelected.put(i, false);
        }
    }
    private boolean onlyTrans;
    public void setOnlyTrans(boolean value){
        onlyTrans = value;
    }
    private boolean showCheck;
    public void setShowCheck(boolean showCheck){
        this.showCheck = showCheck;
    }
    private void handleContact() {
        mContactList = new ArrayList<>();
        Map<String, PPUserInfo> map = new HashMap<>();
        for (int i = 0; i < mContactNames.size(); i++) {
            if(TextUtils.isEmpty(mContactNames.get(i).getName())){
                continue;
            }
            String pinyin = Utils.getPingYin(mContactNames.get(i).getName());
            map.put(pinyin, mContactNames.get(i));
            mContactList.add(pinyin);
        }
        Collections.sort(mContactList, new ContactComparator());

        resultList = new ArrayList<>();
        characterList = new ArrayList<>();

        for (int i = 0; i < mContactList.size(); i++) {
            String name = mContactList.get(i);
            String character = (name.charAt(0) + "").toUpperCase(Locale.ENGLISH);
            if (!characterList.contains(character)) {
                if (character.hashCode() >= "A".hashCode() && character.hashCode() <= "Z".hashCode()) { // 是字母
                    characterList.add(character);
                    PPUserInfo userInfo = new PPUserInfo();
                    userInfo.setName(character);
                    resultList.add(new Contact(userInfo, ITEM_TYPE.ITEM_TYPE_CHARACTER.ordinal()));
                } else {
                    if (!characterList.contains("#")) {
                        characterList.add("#");
                        PPUserInfo userInfo = new PPUserInfo();
                        userInfo.setName("#");
                        resultList.add(new Contact(userInfo, ITEM_TYPE.ITEM_TYPE_CHARACTER.ordinal()));
                    }
                }
            }

            resultList.add(new Contact(map.get(name), ITEM_TYPE.ITEM_TYPE_CONTACT.ordinal()));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_CHARACTER.ordinal()) {
            return new CharacterHolder(mLayoutInflater.inflate(R.layout.item_character, parent, false));
        } else {
            return new ContactHolder(mLayoutInflater.inflate(R.layout.item_contact, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CharacterHolder) {
            ((CharacterHolder) holder).mTextView.setText(resultList.get(position).getUserInfo().getName());
        } else if (holder instanceof ContactHolder) {
            ((ContactHolder) holder).mTextView.setText(resultList.get(position).getUserInfo().getName());
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.showImageOnLoading(R.mipmap.default_icon)
                    .cacheInMemory(true).cacheOnDisk(true);
            ImageLoader.getInstance().displayImage(resultList.get(position).getUserInfo().getIcon(), ((ContactHolder) holder).imageView,
                    builder.build());
            ((ContactHolder) holder).checkBox.setChecked(isSelected.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return resultList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

    public class CharacterHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        CharacterHolder(View view) {
            super(view);

            mTextView = (TextView) view.findViewById(R.id.character);
        }
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView imageView;
        CheckBox checkBox;
        ContactHolder(View view) {
            super(view);

            mTextView = (TextView) view.findViewById(R.id.contact_name);
            imageView = view.findViewById(R.id.contact_icon);
            checkBox = view.findViewById(R.id.checkbox);
            final CheckBox box = checkBox;
            if(showCheck){
                checkBox.setVisibility(View.VISIBLE);
            }else{
                checkBox.setVisibility(View.GONE);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    PPUserInfo info = resultList.get(position).getUserInfo();
                    if(showCheck){
                        boolean isChecked = isSelected.get(position);
                        isSelected.put(position, !isChecked);
                        box.setChecked(!isChecked);
                        if(!isChecked){
                            selects.add(info);
                            box.setButtonDrawable(R.mipmap.selected);
                        }else{
                            box.setButtonDrawable(R.mipmap.unselect);
                            selects.remove(info);
                        }
                        if(onSelectChanger!=null){
                            onSelectChanger.onSelected(selects);
                        }
                        return;
                    }
                    if(onlyTrans){
                        Intent intent = new Intent(mContext,PayScoreActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(FriendDetailActivity.USER_INFO, info);
                        intent.putExtra(QR_PAY,false);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }else{
                        EventBus.getDefault().post(new Conversation());
                        RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.PRIVATE,info.getPhone(),info.getName());
                    }
                }
            });
//            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                    PPUserInfo info = resultList.get(getLayoutPosition()).getUserInfo();
//                    if(isChecked){
////                        selects.add(info);
//                        box.setButtonDrawable(R.mipmap.selected);
//                    }else{
//                        box.setButtonDrawable(R.mipmap.unselect);
////                        selects.remove(info);
//                    }
////                    if(onSelectChanger!=null){
////                        onSelectChanger.onSelected(selects);
////                    }
//                }
//            });
        }
    }

    public int getScrollPosition(String character) {
        if (characterList.contains(character)) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getUserInfo().getName().equals(character)) {
                    return i;
                }
            }
        }

        return -1; // -1不会滑动
    }
    public interface OnSelectChanger{
        void onSelected(List<PPUserInfo> members);
    }
    private OnSelectChanger onSelectChanger;
    public void setOnSelectChanger(OnSelectChanger onSelectChanger){
        this.onSelectChanger = onSelectChanger;
    }
}
