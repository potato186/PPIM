package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.History;
import com.ilesson.ppim.entity.HistoryBase;
import com.ilesson.ppim.utils.AppUtils;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.noober.menu.FloatMenu;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;


/**
 * Created by potato on 2019/5/7.
 */

public class HistoryActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "HistoryActivity";
    private PullToRefreshListView mListView;
    private int mCurrentPage;
    private int mPageRows = 20;
    private List<History> mList;
    private Adapter mAdapter;
    public static final String HISTORY = "history";

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_history);
        setStatusBarLightMode(this,true);
        mPhone = SPUtils.get(LoginActivity.USER_PHONE,"");
        mList = new ArrayList<>();
        mAdapter = new Adapter(mList);
        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mListView
                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        mCurrentPage = 0;
                        loadData(false);
                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        mCurrentPage++;
                        loadData(false);
                    }
                });
        loadData(false);
        initIndicator();
        findViewById(R.id.back).setOnClickListener(this);
    }
    private void initIndicator() {
        ILoadingLayout startLabels = mListView
                .getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("下拉加载");
        startLabels.setRefreshingLabel("正在刷新...");
        startLabels.setReleaseLabel("松开加载");

        ILoadingLayout endLabels = mListView.getLoadingLayoutProxy(
                false, true);
        endLabels.setPullLabel("上拉加载");
        endLabels.setRefreshingLabel("正在刷新...");
        endLabels.setReleaseLabel("松开加载");
    }
    private String[] grades = {"一年级", "一年级", "二年级", "三年级", "四年级", "五年级", "六年级"};
    private String mPhone;

    private void loadData(final boolean clear) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addBodyParameter("action", "list");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addBodyParameter("page", mCurrentPage+"");
        params.addBodyParameter("size", "" + mPageRows);
        Log.d(TAG, "loadData: "+params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                HistoryBase base = new Gson().fromJson(
                        result,
                        new TypeToken<HistoryBase>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<History> data = base.getData();
                    if (clear) {
                        mList.clear();
                    }
                    mList.addAll(data);
                    mAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                } else {
                    showToast(base.getMessage());
                }
                hideProgress();
                Log.d(TAG, "onSuccess: +" + result);
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
                hideProgress();
            }
        });
        showProgress();
    }
    private void delete(final History history) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addBodyParameter("action", "delete");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addBodyParameter("uuid", history.getUuid());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideProgress();
                HistoryBase base = new Gson().fromJson(
                        result,
                        new TypeToken<HistoryBase>() {
                        }.getType());
                if (base.getCode() == 0) {
                    mList.remove(history);
                    mAdapter.notifyDataSetChanged();
                } else {
                    showToast( base.getMessage());
                }

            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                removeDialog(DIALOG_PROGRESS);
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
                removeDialog(DIALOG_PROGRESS);
            }


            @Override
            public void onFinished() {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
            break;
            case R.id.clear:

            break;
        }
    }

    class Adapter extends BaseAdapter {

        List<History> list;

        public Adapter(List<History> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(
                        R.layout.history_item, null);
                holder.score = convertView
                        .findViewById(R.id.content);
                holder.title = convertView
                        .findViewById(R.id.title);
                holder.content = convertView
                        .findViewById(R.id.descript);
                holder.date = convertView
                        .findViewById(R.id.date);
                holder.grade = convertView
                        .findViewById(R.id.grade);
                holder.count = convertView
                        .findViewById(R.id.count);
                holder.date = convertView
                        .findViewById(R.id.date);
                holder.historyLayout = convertView
                        .findViewById(R.id.history_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final History history = list.get(position);
            holder.grade.setText(String.format(getResources().getString(R.string.grade_),history.getGrade()));
            holder.count.setText(String.format(getResources().getString(R.string.num_),history.getNumber())+"字");
            int score = history.getScore();
            if(score<60){
                holder.score.setBackgroundResource(R.drawable.background_standard_bad_corner5);
            }else{
                holder.score.setBackgroundResource(R.drawable.background_standard_good_corner5);
            }
            holder.score.setText(score+"");
            holder.date.setText(AppUtils.getDate(history.getDate()));
            holder.title.setText(history.getTitle());
            holder.content.setText(history.getSrc());
//            holder.delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showDelete(history);
//                }
//            });
            holder.historyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    History history = mList.get(position);
                    Intent intent = new Intent(HistoryActivity.this, WebActivity.class);
                    intent.putExtra(WebActivity.WORD_NUM,history.getNumber()+"");
                    intent.putExtra(WebActivity.TITLE,history.getTitle());
                    intent.putExtra(WebActivity.UUID,history.getUuid());
                    intent.putExtra(WebActivity.SCORE,history.getScore()+"");
                    intent.putExtra(WebActivity.GRADE,history.getGrade()+"");
                    startActivity(intent);
                }
            });
            holder.historyLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    FloatMenu floatMenu = new FloatMenu(HistoryActivity.this);
                    floatMenu.items("删除");
                    floatMenu.show(point);
                    floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                        @Override
                        public void onClick(View v, int position) {
//                            if(0==position){
//                                ComposeMessage message = new ComposeMessage();
//                                message.setTitle(history.getTitle());
//                                message.setUuid(history.getUuid());
//                                message.setCount(history.getNumber()+"");
//                                message.setGrade(history.getGrade()+"");
//                                message.setScore(history.getScore()+"");
//                                new ComposeMsgUtils().showSendDialog(HistoryActivity.this,message);
//                            }else{
                                delete(history);
//                            }
                        }
                    });
                    return true;
                }
            });
            return convertView;
        }

    }

    class ViewHolder {
        private TextView score;
        private TextView title;
        private TextView grade;
        private TextView content;
        private TextView date;
        private TextView count;
        private View historyLayout;
    }
    private Point point = new Point();
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            point.x = (int) ev.getRawX();
            point.y = (int) ev.getRawY();
        }
        return super.dispatchTouchEvent(ev);
    }
}
