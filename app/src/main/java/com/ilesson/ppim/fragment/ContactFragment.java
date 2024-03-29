package com.ilesson.ppim.fragment;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.MainActivity.FRIEND_ACCEPT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.MainActivity;
import com.ilesson.ppim.activity.NewFriendsListActivity;
import com.ilesson.ppim.contactcard.ContactCardContext;
import com.ilesson.ppim.contactcard.IContactCardInfoProvider;
import com.ilesson.ppim.contactview.ContactAdapter;
import com.ilesson.ppim.contactview.DividerItemDecoration;
import com.ilesson.ppim.contactview.LetterView;
import com.ilesson.ppim.db.PPUserDao;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.DeleteFriend;
import com.ilesson.ppim.entity.ModifyUserNike;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.StatusBarUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rong.eventbus.EventBus;
import io.rong.imlib.model.UserInfo;


/**
 * Created by potato on 2016/4/12.
 */
@ContentView(R.layout.frag_contact)
public class ContactFragment extends BaseFragment {
    @ViewInject(R.id.top)
    public View top;
    @ViewInject(R.id.msg_tip)
    public TextView msgTip;

    @ViewInject(R.id.recylerview)
    private RecyclerView contactList;
    @ViewInject(R.id.letter_view)
    private LetterView letterView;
    private LinearLayoutManager layoutManager;

    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    private ContactAdapter adapter;
    public List<PPUserInfo> datas = new ArrayList<>();;
    public Map<String, String> friends = new HashMap<>();
    private MainActivity mainActivity;
    private static final String TAG = "ContactFragment";
    private static final String CONTACT_LIST = "contact_listdatas";
    private PPUserDao ppUserDao;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        mainActivity = (MainActivity) getActivity();
        StatusBarUtil.setBarPadding(mainActivity,top);
        layoutManager = new LinearLayoutManager(mainActivity);
        contactList.setLayoutManager(layoutManager);
        contactList.addItemDecoration(new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL_LIST));

        letterView.setCharacterListener(new LetterView.CharacterClickListener() {
            @Override
            public void clickCharacter(String character) {
                layoutManager.scrollToPositionWithOffset(adapter.getScrollPosition(character), 0);
            }

            @Override
            public void clickArrow() {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        });
        Log.d(TAG, "onViewCreated: ");
        ppUserDao = new PPUserDao();
        datas.addAll(ppUserDao.getAllFriends());
        adapter = new ContactAdapter(mainActivity, datas);
        contactList.setAdapter(adapter);
        IlessonApp.getInstance().setDatas(datas);
//        setUsers();
        requestFriendsList(false);
        showUnread();
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestFriendsList(true);
            }
        });

    }
    public void onEventMainThread(PPUserInfo userInfo) {
        requestFriendsList(true);
    }
    public void onEventMainThread(ModifyUserNike userInfo) {
        requestFriendsList(true);
    }
    public void showUnread() {
        int unAcceptFriends = SPUtils.get(FRIEND_ACCEPT, 0);
        if(msgTip==null){
            return;
        }
        if (unAcceptFriends > 0) {
            msgTip.setVisibility(View.VISIBLE);
            msgTip.setText(unAcceptFriends + "");
        } else {
            msgTip.setVisibility(View.GONE);
        }
    }

    @Event(value = R.id.search)
    private void search(View view) {
        if(null!=mainActivity){
            mainActivity.search();
        }
    }

    @Event(value = R.id.add)
    private void add(View view) {
        if(null!=mainActivity){
            mainActivity.add(view);
        }
    }

    @Event(value = R.id.new_friend_layout)
    private void newFriend(View view) {
        startActivity(new Intent(getActivity(), NewFriendsListActivity.class));
        SPUtils.put(FRIEND_ACCEPT, 0);
        mainActivity.unAcceptFriends = 0;
        mainActivity.showUnreadRequestNewFriends();
    }

    private boolean hasRequest;
    @Override
    public void onResume() {
        super.onResume();
//        if(datas.isEmpty()&&!hasRequest){
//            modify();
//        }
    }
    public void onEventMainThread(DeleteFriend var) {
        ppUserDao.deleteFriend(var.getUserInfo().getPhone());
        int index=-1;
        for (int i = 0; i < datas.size(); i++) {
            if(datas.get(i).getPhone().equals(var.getUserInfo().getPhone())){
                index = i;
                break;
            }
        }
        datas.remove(index);
        adapter = new ContactAdapter(mainActivity, datas);
        contactList.setAdapter(adapter);
    }

    public void requestFriendsList(final boolean update) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addParameter("action", "friend");
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addParameter("token", token);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
//            @Override
//            public boolean onCache(String result) {
//                readFriendsJson(result,update);
//                return false;
//            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                readFriendsJson(result,update);
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
                if(swipeLayout!=null&&swipeLayout.isRefreshing()){
                    swipeLayout.setRefreshing(false);
                }
            }
        });
    }

    private void readFriendsJson(String json,boolean update){
        BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<List<PPUserInfo>>>() {
                }.getType());
        hasRequest = true;

        if (base.getCode() == 0) {
            List<PPUserInfo> data = base.getData();
            if (null == data || data.isEmpty()) {
                return;
            }
            friends = new HashMap<>();
            final List<UserInfo> users = new ArrayList<>();
            for (PPUserInfo info : data) {
                friends.put(info.getPhone(), info.getPhone());
                SPUtils.put(info.getPhone(), info.getPhone());
                SPUtils.put(info.getPhone()+"icon",info.getIcon());
                SPUtils.put(info.getPhone()+"name",info.getName());
                UserInfo user = new UserInfo(info.getPhone(),info.getName(), Uri.parse(info.getIcon()));
                users.add(user);
                info.setFriend(true);
//                PPUserInfo ppUserInfo = ppUserDao.getFriendByKey(info.getPhone());
//                if(null!=ppUserInfo){
//                    info.setId(ppUserInfo.getId());
//                }
                ppUserDao.update(info);
            }
            if(datas==null){
                datas = new ArrayList<>();
            }else{
                datas.clear();
            }
            datas.addAll(data);
            if (null == contactList) {
                return;
            }
            adapter = new ContactAdapter(mainActivity, datas);
            contactList.setAdapter(adapter);
            setUsers();
            if(update||!SPUtils.get(CONTACT_LIST,"").equals(json)){
                new IMUtils().upUser(mainActivity);
            }
            IlessonApp.getInstance().setDatas(datas);
            SPUtils.put(CONTACT_LIST,json);
            ContactCardContext.getInstance().setContactCardInfoProvider(new IContactCardInfoProvider() {
                @Override
                public void getContactAllInfoProvider(IContactCardInfoCallback contactInfoCallback) {
                    contactInfoCallback.getContactCardInfoCallback(users);
                }

                @Override
                public void getContactAppointedInfoProvider(String userId, String name, String portrait, IContactCardInfoCallback contactInfoCallback) {
                    contactInfoCallback.getContactCardInfoCallback(users);
                }
            });
        }
    }
    private void setUsers(){
        if(datas.isEmpty()){
            letterView.setVisibility(View.GONE);
            contactList.setVisibility(View.GONE);
        }else{
            contactList.setVisibility(View.VISIBLE);
            letterView.setVisibility(View.VISIBLE);
        }
        IlessonApp.getInstance().setDatas(datas);
       adapter.notifyDataSetChanged();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
