package com.ilesson.ppim.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.NoteActivity;
import com.ilesson.ppim.activity.SearchActivity;
import com.ilesson.ppim.entity.AllSearchInfo;
import com.ilesson.ppim.entity.SearchInfo;
import com.ilesson.ppim.utils.PPScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<AllSearchInfo> resultList = new ArrayList<>();
    public static int editIndex;
    public int recordIndex;
    private HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
    private RecyclerView recyclerView;
    private NoteActivity noteActivity;
    private List<EditText> editTexts = new ArrayList<>();
    private int screenWidth;
    private String keyWords;

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public AllSearchAdapter(Context context, List<AllSearchInfo> datas, String keyWords) {
        mContext = context;
        this.keyWords = keyWords;
        mLayoutInflater = LayoutInflater.from(context);
        resultList = datas;
        editIndex = resultList.size();
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public AllSearchAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public void setNoteActivity(NoteActivity noteActivity) {
        this.noteActivity = noteActivity;
    }

    public List<AllSearchInfo> getResultList() {
        return resultList;
    }

    public void setResultList(List<AllSearchInfo> resultList) {
        this.resultList = resultList;
        editIndex = resultList.size();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.search_type_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        AllSearchInfo allSearchInfo = resultList.get(position);
        List<SearchInfo> searchInfos = allSearchInfo.getSearchInfos();
        List<SearchInfo> datas = new ArrayList<>();
        if (searchInfos.size()>3) {
            viewHolder.moreLayout.setVisibility(View.VISIBLE);
            datas.addAll(searchInfos.subList(0,3));
            viewHolder.moreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SearchActivity.class);
                    intent.putExtra(SearchActivity.SEARCH_KEY,keyWords);
                    intent.putExtra(SearchActivity.SEARCH_TYPE_NAME,allSearchInfo.getSearchType());
                    mContext.startActivity(intent);
                }
            });
        }else{
            datas.addAll(searchInfos);
            viewHolder.moreLayout.setVisibility(View.GONE);
        }
        viewHolder.moreView.setText(String.format(mContext.getString(R.string.search_more),allSearchInfo.getSearchType()));
        viewHolder.titleView.setText(allSearchInfo.getSearchType());
        SearchItemAdapter adapter = new SearchItemAdapter(mContext,datas,keyWords);
        viewHolder.recyclerview.setLayoutManager(new LinearLayoutManager(mContext));
        viewHolder.recyclerview.setAdapter(adapter);
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
        TextView moreView;
        View moreLayout;
        RecyclerView recyclerview;
        ViewHolder(View view) {
            super(view);
            moreLayout = view.findViewById(R.id.more_layout);
            titleView = view.findViewById(R.id.type_name);
            moreView = view.findViewById(R.id.more_title);
            recyclerview = view.findViewById(R.id.recylerview);
        }
    }

}
