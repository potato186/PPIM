package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Collect;
import com.ilesson.ppim.entity.NoteContent;
import com.ilesson.ppim.entity.NoteInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.Dateuitls;
import com.ilesson.ppim.utils.FileTool;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.PPScreenUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ilesson.ppim.service.FavoriteHelper.TYPE_FILE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_IMAGE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_LOCATION;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_NOTE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_TEXT;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_VOICE;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_mycollect)
public class MyCollectActivity extends BaseActivity{
    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    private List<Collect> resultList = new ArrayList<>();
    private RefreshAdapter adapter;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        resultList.add(new Collect());
        adapter = new RefreshAdapter(resultList);
        loadData();
        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        swipeLayout.setEnabled(false);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this,5));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, PPScreenUtils.dip2px(this,15));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, PPScreenUtils.dip2px(this,15));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this,5));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));

    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.add_layout)
    private void add_layout(View view){
        Intent intent = new Intent(this,NoteActivity.class);
        startActivity(intent);
    }


    private void loadData() {
        RequestParams params = new RequestParams(Constants.FAV_URL + Constants.FAV_LIST);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<List<Collect>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<Collect>>>() {
                        }.getType());
                if(base.getCode()==0){
                    List<Collect> list = base.getData();
                    if(null!=base.getData()){
                        resultList.addAll(list);
                        adapter.notifyDataSetChanged();
                    }
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
            }
        });
    }
    private static final String TAG = "MyCollectActivity";

    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        Context mContext;
        LayoutInflater mInflater;
        List<Collect> NoteInfos = new ArrayList<>();

        private static final int TYPE_TOP = 0;
        private static final int TYPE_ITEM = 1;
        private static final int TYPE_FOOTER = 2;

        //上拉加载更多
        public static final int PULLUP_LOAD_MORE = 0;
        //正在加载中
        public static final int LOADING_MORE = 1;
        //没有加载更多 隐藏
        public static final int NO_LOAD_MORE = 2;

        //上拉加载更多状态-默认为0
        private int mLoadMoreStatus = 0;

        public RefreshAdapter(List<Collect> data) {
            this.NoteInfos = data;
            mInflater = LayoutInflater.from(MyCollectActivity.this);
        }

        /**a
         * 更新加载更多状态
         *
         * @param status
         */
        public void changeMoreStatus(int status) {
            mLoadMoreStatus = status;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //实现ViewHolder
            if (viewType == TYPE_ITEM) {
                View itemView = mInflater.inflate(R.layout.mycollect_item, parent, false);
                return new ItemViewHolder(itemView);
            } else if (viewType == TYPE_FOOTER) {
                View itemView = mInflater.inflate(R.layout.foot_view, parent, false);
                return new FooterViewHolder(itemView);
            }else{
                View itemView = mInflater.inflate(R.layout.note_type_item, parent, false);
                return new TopViewHolder(itemView);
            }
        }

        private NoteInfo getData(String json){
            return new Gson().fromJson(
                    json,
                    new TypeToken<NoteInfo>() {
                    }.getType());
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
//                NoteInfo note = NoteInfos.get(position);
                Collect collect = NoteInfos.get(position);
                itemViewHolder.date.setText(Dateuitls.getFormatSendTime(Long.valueOf(collect.getDate())));
                NoteInfo noteInfo = getData(collect.getJson());
                if(collect.getType()==TYPE_IMAGE){
                    itemViewHolder.pic.setVisibility(View.VISIBLE);
                    Glide.with(MyCollectActivity.this).load(noteInfo.getUrl()).into(itemViewHolder.pic);
                }else if(collect.getType()==TYPE_NOTE){
                    itemViewHolder.tag.setVisibility(View.VISIBLE);
                    itemViewHolder.type.setText(R.string.note);
                    NoteContent content = new Gson().fromJson(
                            collect.getJson(),
                            new TypeToken<NoteContent>() {
                            }.getType());
//                    NoteContent content = (NoteContent) getData(collect.getJson());
                    List<NoteInfo> data = content.getData();
                    int num = 0;
                    String imgUrl="";
                    String text="";
                    NoteInfo first=null;
                    NoteInfo second=null;
                    for(NoteInfo item:data){
                        switch (item.getType()){
                            case TYPE_IMAGE:
                                num++;
                                if(TextUtils.isEmpty(imgUrl)){
                                    imgUrl = item.getUrl();
                                }
                                itemViewHolder.pic.setVisibility(View.VISIBLE);
                                itemViewHolder.location.setVisibility(View.GONE);
                                itemViewHolder.voice.setVisibility(View.GONE);
                                itemViewHolder.file.setVisibility(View.GONE);
                                Glide.with(MyCollectActivity.this).load(imgUrl).into(itemViewHolder.pic);
                                break;
                            case TYPE_TEXT:
                                if(TextUtils.isEmpty(text)){
                                    text = item.getText().toString();
                                    if(text.contains("\n")){
                                        text = text.substring(0,text.indexOf("\n"));
                                    }
                                    itemViewHolder.textView1.setText(text);
                                    itemViewHolder.textView1.setVisibility(View.VISIBLE);
                                }
                                break;
                            case TYPE_LOCATION:
                                String adress = getResources().getString(R.string.rc_message_content_location);
                                if(null==first){
                                    first = item;
                                    itemViewHolder.textView2.setText(adress);
                                }else{
                                    if(item!=first&&null==second){
                                        second = item;
                                        itemViewHolder.textView3.setText(adress);
                                    }
                                }
                                break;
                            case TYPE_FILE:
                                String name = getResources().getString(R.string.rc_message_content_file)+item.getName();
                                if(null==first){
                                    first = item;
                                    itemViewHolder.textView2.setText(name);
                                }else{
                                    if(item!=first&&null==second){
                                        second = item;
                                        itemViewHolder.textView3.setText(name);
                                    }
                                }
                                break;
                            case TYPE_VOICE:
                                String time = getResources().getString(R.string.rc_message_content_voice)+item.getTime()+getString(R.string.seconds);
                                if(null==first){
                                    first = item;
                                    itemViewHolder.textView2.setText(time);
                                }else{
                                    if(item!=first&&null==second){
                                        second = item;
                                        itemViewHolder.textView3.setText(time);
                                    }
                                }
                                break;
                        }
                    }
                    if(TextUtils.isEmpty(text)){
                        itemViewHolder.textView1.setVisibility(View.GONE);
                    }
                    if(data.size()>1){

                    }else{
                        showOneItem(itemViewHolder,data.get(0));
                    }
                }else if(collect.getType()==TYPE_FILE){
                    if (noteInfo.getUrl().endsWith("zip")) {
                        itemViewHolder.file.setImageResource(R.mipmap.zip_icon);
                    } else if (noteInfo.getUrl().endsWith("doc") || noteInfo.getUrl().endsWith("docx") || noteInfo.getUrl().endsWith("docm") || noteInfo.getUrl().endsWith("dotx") || noteInfo.getUrl().endsWith("dotm")) {
                        itemViewHolder.file.setImageResource(R.mipmap.doc_icon);
                    } else if (noteInfo.getUrl().endsWith("xls") || noteInfo.getUrl().endsWith("xlsx")) {
                        itemViewHolder.file.setImageResource(R.mipmap.xls_icon);
                    } else if (noteInfo.getUrl().endsWith("ppt") || noteInfo.getUrl().endsWith("pptx")) {
                        itemViewHolder.file.setImageResource(R.mipmap.ppt_icon);
                    } else {
                        itemViewHolder.file.setImageResource(R.mipmap.other_icon);
                    }
                    itemViewHolder.textView1.setText(noteInfo.getName());
                    itemViewHolder.textView2.setText(FileTool.getFormatSize(noteInfo.getSize()));
                }else if(collect.getType()==TYPE_VOICE){
                    itemViewHolder.voice.setVisibility(View.VISIBLE);
                    itemViewHolder.textView2.setText(getString(R.string.rc_message_content_voice)+getString(R.string.seconds));
                }else if(collect.getType()==TYPE_LOCATION){
                    itemViewHolder.location.setVisibility(View.VISIBLE);
                    itemViewHolder.textView1.setText(noteInfo.getName());
                    itemViewHolder.textView2.setText(noteInfo.getAddress());
                }else if(collect.getType()==TYPE_TEXT){
                    itemViewHolder.textView1.setText(noteInfo.getText());
                }
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                switch (mLoadMoreStatus) {
                    case PULLUP_LOAD_MORE:
//                        footerViewHolder.mTvLoadText.setText("上拉加载更多...");
//                        footerViewHolder.mPbLoad.setVisibility(View.GONE);
                        break;
                    case LOADING_MORE:
                        footerViewHolder.layout.setVisibility(View.VISIBLE);
                        break;
                    case NO_LOAD_MORE:
                        //隐藏加载更多
                        footerViewHolder.layout.setVisibility(View.GONE);
                        break;
                }
            }
        }

        private void showOneItem(ItemViewHolder itemViewHolder,NoteInfo note){
            itemViewHolder.pic.setVisibility(View.GONE);
            itemViewHolder.voice.setVisibility(View.GONE);
            itemViewHolder.file.setVisibility(View.GONE);
            itemViewHolder.location.setVisibility(View.GONE);
            if(note.getType()==TYPE_LOCATION){
                itemViewHolder.location.setVisibility(View.VISIBLE);
//                itemViewHolder.textView1.setText(note.getDesc());
                itemViewHolder.textView2.setText(note.getAddress());
            }else if(note.getType()==TYPE_FILE){
                itemViewHolder.file.setVisibility(View.VISIBLE);
                itemViewHolder.textView1.setText(note.getName());
                itemViewHolder.textView2.setText(FileTool.getFormatSize(note.getSize()));
            }else if(note.getType()==TYPE_VOICE){
                itemViewHolder.voice.setVisibility(View.VISIBLE);
                itemViewHolder.textView1.setText(note.getTime()+getString(R.string.seconds));
            }
        }
        @Override
        public int getItemCount() {
            return NoteInfos.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {//position是mDatas的下标
            if(position==0){
                return TYPE_TOP;
            }
            else if (position + 1 == getItemCount()) {
                //最后一个item设置为footerView
                return TYPE_FOOTER;
            } else {
                return TYPE_ITEM;
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView textView1,textView2,textView3,textView4;
            TextView type,user,date,tag;
            ImageView pic,location,voice,file;
            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                textView1 = itemView.findViewById(R.id.first);
                textView2 = itemView.findViewById(R.id.second);
                textView3 = itemView.findViewById(R.id.third);
                textView4 = itemView.findViewById(R.id.forth);
                type = itemView.findViewById(R.id.type);
                user = itemView.findViewById(R.id.user);
                date = itemView.findViewById(R.id.date);
                tag = itemView.findViewById(R.id.tag);
                pic = itemView.findViewById(R.id.pic);
                file = itemView.findViewById(R.id.file_icon);
                location = itemView.findViewById(R.id.location);
                voice = itemView.findViewById(R.id.voice);
                initListener(itemView);
            }

            private void initListener(View itemView) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {
            View layout;
            public FooterViewHolder(View itemView) {
                super(itemView);
                layout = itemView.findViewById(R.id.layout);
            }
        }

        class TopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TopViewHolder(View itemView) {
                super(itemView);
                itemView.findViewById(R.id.pic).setOnClickListener(this);
                itemView.findViewById(R.id.link).setOnClickListener(this);
                itemView.findViewById(R.id.file).setOnClickListener(this);
                itemView.findViewById(R.id.chat_record).setOnClickListener(this);
                itemView.findViewById(R.id.voice).setOnClickListener(this);
                itemView.findViewById(R.id.note).setOnClickListener(this);
                itemView.findViewById(R.id.location).setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.pic:

                        break;
                }
            }
        }
    }
}
