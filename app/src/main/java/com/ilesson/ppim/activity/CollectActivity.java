package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.ComposeCollectBase;
import com.ilesson.ppim.entity.ComposeCollectContent;
import com.ilesson.ppim.entity.ComposeCollectInfo;
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

public class CollectActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "CollectActivity";
    private PullToRefreshListView mListView;
    private int mCurrentPage;
    private int mPageRows = 20;
    private List<ComposeCollectContent> mList;
    private Adapter mAdapter;
    public static final String HISTORY = "history";
    public static final String SAMPLE_TYPE = "1";
    public static final String HISTORY_TYPE = "2";
    public static final int COLLECT_CANCEL = 8;
    public static final int TYPE_COMPOSE = 1;

    private View layout;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_collect);
        setStatusBarLightMode(this, true);
        mPhone = SPUtils.get(LoginActivity.USER_PHONE, "");
        mList = new ArrayList<>();
        mAdapter = new Adapter(mList);
        mListView = findViewById(R.id.list_view);
        layout = findViewById(R.id.layout);
        mListView.setAdapter(mAdapter);
        mListView
                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        mCurrentPage = 0;
                        loadData(true);
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
    private String mPhone;

    //action=fav_list&phone=%s&key=%s&p=%s&s=%s
    private void loadData(final boolean clear) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addBodyParameter("action", "fav_list");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addBodyParameter("page", mCurrentPage + "");
        params.addBodyParameter("size", "" + mPageRows);
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideProgress();
                Log.d(TAG, "onSuccess: +" + result);
                ComposeCollectBase base = new Gson().fromJson(
                        result,
                        new TypeToken<ComposeCollectBase>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<ComposeCollectContent> data = base.getData();
                    if (clear) {
                        mList.clear();
                    }
                    mList.addAll(data);
                    mAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                } else {
                    showToast(base.getMessage());
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
                hideProgress();
            }
        });
        showProgress();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.clear:

                break;
        }
    }

    class Adapter extends BaseAdapter {

        List<ComposeCollectContent> list;

        public Adapter(List<ComposeCollectContent> list) {
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
                        R.layout.sample_item, null);
                holder.grade = convertView
                        .findViewById(R.id.grade);
                holder.title = convertView
                        .findViewById(R.id.title);
                holder.count = convertView
                        .findViewById(R.id.count);
                holder.type = convertView
                        .findViewById(R.id.type);
                holder.date = convertView
                        .findViewById(R.id.date);
                holder.layout = convertView
                        .findViewById(R.id.layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final ComposeCollectContent collect = list.get(position);
            holder.title.setText(collect.getTitle());
            String arr[] = collect.getDesc().split("//");
            if(arr.length==1){
                arr = collect.getDesc().split("  ");
            }
            holder.date.setText(AppUtils.getDate(collect.getDate()));
            String grade="";
            String score="";
            String count="";
            if(arr.length==3){
                holder.grade.setText(arr[0]);
                holder.type.setText(arr[2]+"字");
//                holder.type.setVisibility(View.GONE);
//                holder.count.setVisibility(View.VISIBLE);
//                holder.count.setText(arr[2]);
                grade = arr[0].replace("年级", "").replace(":", "").replace("：", "");
                score = arr[1].replace("得分", "").replace("分", "").replace(":", "").replace("：", "");
                count = arr[2].replace("字数", "").replace("字", "").replace(":", "").replace("：", "");
            }
            final String s = score;
            final String c = count;
            final String g = grade;
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(CollectActivity.this, WebActivity.class);
//                    intent.putExtra(WebActivity.WORD_NUM,history.getNumber()+"");
                    intent.putExtra(WebActivity.TITLE, collect.getTitle());
                    intent.putExtra(WebActivity.UUID, collect.getUuid());

                    intent.putExtra(WebActivity.SCORE, s);
                    intent.putExtra(WebActivity.GRADE, g);
                    intent.putExtra(WebActivity.WORD_NUM, c);
                    startActivityForResult(intent, 0);
                    clickedItem = collect;
                }
            });
            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    showDeleteDialog(collect);
                    FloatMenu floatMenu = new FloatMenu(CollectActivity.this);
                    floatMenu.items("删除");
                    floatMenu.show(point);
                    floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                        @Override
                        public void onClick(View v, int position) {
//                            if(0==position){
//                                ComposeMessage message = new ComposeMessage();
//                                message.setTitle(collect.getTitle());
//                                message.setUuid(collect.getUuid());
//                                message.setCount(c);
//                                message.setGrade(g);
//                                message.setScore(s);
//                                new ComposeMsgUtils().showSendDialog(CollectActivity.this,message);
//                            }else {
                                unCollect(collect);
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
        private TextView title;
        private TextView grade;
        private TextView count;
        private TextView type;
        private TextView date;
        private View layout;
    }

    private ComposeCollectContent clickedItem;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode==COLLECT_CANCEL&&null!=clickedItem){
//            mList.remove(clickedItem);
//            mAdapter.notifyDataSetChanged();
//        }
    }

    private void showPopwindow(View view,final ComposeCollectContent collect) {
        //加载弹出框的布局
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.collect_pop_menu, null);


        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.showAsDropDown(layout, point.x,point.y);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unCollect(collect);
            }
        });
    }

    private void unCollect(final ComposeCollectContent collect) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addParameter("action", "fav_rm");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("id", collect.getId() + "");
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "unCollect onSuccess: " + result);
                ComposeCollectInfo base = new Gson().fromJson(
                        result,
                        new TypeToken<ComposeCollectInfo>() {
                        }.getType());
                if (base.getCode() == 0) {
                    mList.remove(collect);
                    mAdapter.notifyDataSetChanged();
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
