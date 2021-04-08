package com.ilesson.ppim.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ContactActivity;
import com.ilesson.ppim.activity.ForwadSelectActivity;
import com.ilesson.ppim.activity.MainActivity;
import com.ilesson.ppim.activity.PayPwdActivity;
import com.ilesson.ppim.activity.ScoreCodeActivity;
import com.ilesson.ppim.custom.TransferMessage;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Currency;
import com.ilesson.ppim.entity.TargetCurrency;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.MyFileUtils;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.common.util.FileUtil;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.ilesson.ppim.activity.ContactActivity.PAY_TYPE;
import static com.ilesson.ppim.activity.ContactActivity.SELECT_ACTION;
import static com.ilesson.ppim.activity.ForwadSelectActivity.INTENT_TYPE;
import static com.ilesson.ppim.activity.ForwadSelectActivity.PAY_SCORE;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2016/4/12.
 */
@ContentView(R.layout.frag_fund1)
public class FundFragment extends BaseFragment {
    private static final String TAG = "FundFragment";
    private MainActivity mainActivity;
    @ViewInject(R.id.current_name)
    private TextView currentName;
    @ViewInject(R.id.current_balance)
    private TextView currentBalance;
    @ViewInject(R.id.current_score)
    private TextView currentScore;
    @ViewInject(R.id.listiview)
    private ListView listView;
    @ViewInject(R.id.create_fund_layout)
    private View crateFundLayout;
    public List<Currency> datas;
    private Currency currency;
    private CountryAdapter adapter;
    public static final String CURRENCY_LIST = "currency_list";
    public static final String CURRENCY = "currency";
    public static final String BALANCE = "balance";
    public static final String ACTION_PAY = "action_pay";
    public static final String FUND_NOT_ACTIVED = "fund_is_actived";
    public static final String FUND_HAD_LOAD = "fund_had_load";
    private String token;
    private boolean hasLoad;
    private boolean pay;
    private String phone;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        datas = new ArrayList<>();
        adapter = new CountryAdapter(datas);
        listView.setAdapter(adapter);
        token = SPUtils.get("token", "");
        phone = SPUtils.get(USER_PHONE, "");
//        currency.setCurrency("GWI");
//        requestTarget();
        hasLoad = SPUtils.get(FUND_HAD_LOAD, false);
        boolean pay = SPUtils.get(LOGIN_PAY, false);
        if (pay) {
//            showLocalData();
            requestList(!hasLoad);
        } else {
            crateFundLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onEventMainThread(Currency currency) {
        for (Currency c : datas) {
            if (currency.getCurrency().equals(this.currency.getCurrency())) {
                this.currency.setBalance(currency.getBalance());
                currentBalance.setText(BigDecimalUtil.format(currency.getBalance()) + "");
            }
            if (c.getCurrency().equals(currency.getCurrency())) {
                c.setBalance(currency.getBalance());
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showLocalData() {
        BaseCode<List<Currency>> data = SPUtils.get(phone + FUND_HAD_LOAD, new BaseCode());
        if (null == data) {
            return;
        }
        setData(data, false);
    }

    public void onEventMainThread(TransferMessage message) {
        requestCurrency(message.getExtra());
//        requestList(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Event(value = R.id.accept)
    private void accept(View view) {
        if(crateFundLayout.getVisibility()==View.VISIBLE){
            return;
        }
        startActivity(new Intent(getActivity(), ScoreCodeActivity.class));
    }

    @Event(value = R.id.fresh)
    private void fresh(View view) {
        if(crateFundLayout.getVisibility()==View.VISIBLE){
        return;
    }
        requestList(true);
    }
    @Event(value = R.id.bitcap)
    private void bitcap(View view) {
        openBitcaps("nXvMUbqI6SoM-fH2TL0Bic2V5y_TL1qRzBj7fcCj8ZGn0dotrg6ufPmzAli8J9zBZcI59YEhy3prTW91t8Gk8SMtVLHpfuIjrcKitW3BAvBVyR5Sxi58sV3UqS8oz1WdXUUiVStdLzTxURDo5jBcPMKBiYgYTB9PxHlx6UL-WqG-yYlOPimhusqI6Wf10sy3uuiD41kYbt0WLy30VsApRYM5Win8XGJZWx6c8cu1OUBPH6OWqL1uHZBRtCCikp6CMWigdpUPaV97GqZN_Z7eVQ");
    }
    public void openBitcaps(String token) {
        final String appPackageName = "com.bitcaps";
// get intent for package name “com.bitcaps”
        Intent launchIntent = mainActivity.getPackageManager().getLaunchIntentForPackage(appPackageName);
//null pointer check in case package name was not found
        if (launchIntent != null) {
//adding token in intent to pass to bitcaps
            launchIntent.putExtra("payload",token);
            startActivity(launchIntent);
        } else {
//Open bitcaps page on play store
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
// if play store is not available then open in web browser
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +
                        appPackageName)));
            }
        }
    }
    @Event(value = R.id.create_fund)
    private void create_fund(View view) {
//        showDialog();
        active();
    }

    @Event(value = R.id.pay)
    private void pay(View view) {
        if(crateFundLayout.getVisibility()==View.VISIBLE){
            return;
        }
        boolean active = SPUtils.get(LOGIN_PAY, false);
        if (!active) {
            Toast.makeText(getActivity(), R.string.no_active, Toast.LENGTH_LONG).show();
            return;
        }
        mainActivity.toScan();
    }

    @Event(value = R.id.transfer)
    private void transfer(View view) {
        if(crateFundLayout.getVisibility()==View.VISIBLE){
            return;
        }
        boolean active = SPUtils.get(LOGIN_PAY, false);
        if (!active) {
            Toast.makeText(getActivity(), R.string.no_active, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(mainActivity, ForwadSelectActivity.class);
        intent.putExtra(CURRENCY_LIST, (Serializable) datas);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENCY, currency);
        intent.putExtra(INTENT_TYPE, PAY_SCORE);
        intent.putExtras(bundle);
        startActivity(intent);
//        transfer();
//        requestTarget();
    }

    @Event(value = R.id.listiview, type = AdapterView.OnItemClickListener.class)
    private void listiview(AdapterView<?> parent, View view, int position, long id) {
        for (int i=0;i<datas.size();i++){
            Currency currency = datas.get(i);
            if(i==position){
                currency.setDefault(true);
            }else{
                currency.setDefault(false);
            }
        }
        adapter.notifyDataSetChanged();
//        currency = datas.get(position);
//        showCurrency(view,false,false);
    }

    private void showCurrency(View view,boolean local,boolean fresh) {
        showTitle(local);
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.banlance.setTextColor(getResources().getColor(R.color.theme_color));
        holder.name.setTextColor(getResources().getColor(R.color.theme_color));
        holder.tag.setVisibility(View.VISIBLE);
        if(fresh){
            adapter.notifyDataSetChanged();
        }
    }

    private void showTitle(boolean local) {
        if(local){
            double balace = Double.valueOf(SPUtils.get(phone+currency.getCurrency(),"0"));
            if(balace>0){
                currentBalance.setText(BigDecimalUtil.format(balace));
            }else{
                currentBalance.setText(BigDecimalUtil.format(currency.getBalance()));
            }
        }else{
            currentBalance.setText(BigDecimalUtil.format(currency.getBalance()));
        }
        currentName.setText(currency.getCurrency());
        currentScore.setVisibility(View.VISIBLE);
    }

    @Event(value = R.id.trans)
    private void trans(View view) {
        if(crateFundLayout.getVisibility()==View.VISIBLE){
            return;
        }
        boolean isPay = SPUtils.get(LOGIN_PAY, false);
        if (!isPay) {
            startActivity(new Intent(getActivity(), PayPwdActivity.class));
        } else {
            startActivity(new Intent(getActivity(), ContactActivity.class).putExtra(SELECT_ACTION, PAY_TYPE));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class CountryAdapter extends BaseAdapter {

        private List<Currency> data;

        private CountryAdapter(List<Currency> data) {
            this.data = data;
        }
        private boolean isLocal;
        public void setLocal(boolean isLocal){
            this.isLocal = isLocal;
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final Currency content = data.get(i);
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.current_item, null);
                holder = new ViewHolder();
                holder.name = view.findViewById(R.id.name);
                holder.banlance = view.findViewById(R.id.banlance);
                holder.tag = view.findViewById(R.id.tag);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tag.setVisibility(View.GONE);
            if(isLocal){
                double balace = Double.valueOf(SPUtils.get(phone+content.getCurrency(),"0"));
                if(balace>0){
                    holder.banlance.setText(BigDecimalUtil.format(balace));
                }else{
                    holder.banlance.setText(BigDecimalUtil.format(content.getBalance()));
                }
            }else{
                holder.banlance.setText(BigDecimalUtil.format(content.getBalance()));
            }
            holder.name.setText(content.getCurrency());
            holder.banlance.setTextColor(getResources().getColor(R.color.overlay));
            holder.name.setTextColor(getResources().getColor(R.color.overlay));
            if (content.isDefault()) {
                currency = content;
                showCurrency(view,isLocal,false);
            } else {
                    holder.banlance.setTextColor(getResources().getColor(R.color.overlay));
                    holder.name.setTextColor(getResources().getColor(R.color.overlay));
            }
            return view;
        }
    }

    class ViewHolder {
        private TextView name;
        private TextView banlance;
        private TextView tag;
    }

    public void activeSuccess() {
        requestList(false);
        crateFundLayout.setVisibility(View.GONE);
    }

    private void requestTarget() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_ASSET);
        params.addParameter("action", "target");
        params.addParameter("authorization", SPUtils.get("bToken", ""));
        params.addParameter("currency", currency.getCurrency());
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<List<Currency>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<Currency>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<Currency> list = base.getData();
                    if (list.isEmpty()) {
                        return;
                    }
                    Currency content = list.get(0);
                    currentName.setText(content.getCurrency());
                    currentBalance.setText(BigDecimalUtil.format(content.getBalance()) + "");
                } else {
                    Toast.makeText(mainActivity, base.getMessage(), Toast.LENGTH_LONG).show();
                }
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
            }
        });
    }

    int count;

    public void requestList(boolean showProgress) {
//        if("18620341982".equals(phone)){
//            return;
//        }
        mainActivity.handler.removeMessages(1);
        count++;
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_ASSET);
        params.addParameter("action", "all_v3");
        params.addParameter("token", token);
//        params.addParameter("currency", "ALL");
        if (showProgress) {
            mainActivity.showProgress();
        }
        Log.d(TAG, "loadData: " + params.toString());
        final String path = MD5.md5(params.toString()) + phone;
        final String dir = FileUtil.getCacheDir("json").getAbsolutePath();
        if(!showProgress){
            showDefualt(dir,path);
        }
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                readJson(result, dir,path,false);
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                showDefualt(dir,path);
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                mainActivity.hideProgress();
            }
        });
    }

    public void requestCurrency(final String target) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_ASSET);
        params.addParameter("action", "target_v3");
        params.addParameter("token", token);
        params.addParameter("currency", target);
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<TargetCurrency> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<TargetCurrency>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    TargetCurrency targetCurrency = base.getData();
                    Currency currency1 = targetCurrency.getData().get(0);
                    SPUtils.put(phone+target,currency1.getBalance());
                    for (Currency currency : datas) {
                        if (currency.getCurrency().equals(target)) {
                            currency.setBalance(currency1.getBalance());
                        }
                    }
                    adapter.notifyDataSetChanged();
//                    BaseCode<List<Currency>> data = SPUtils.get(phone + FUND_HAD_LOAD, new BaseCode<List<Currency>>());
//                    if (null != data) {
//                        data.setData(datas);
//                        SPUtils.put(phone + FUND_HAD_LOAD,data);
//                    }
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

    private void showDefualt(String dir, String path) {
        String result = MyFileUtils.file2String(dir + File.separator + path);
        if (TextUtils.isEmpty(result)) {
            return;
        }
        readJson(result, dir,path,true);
    }

    private void readJson(String json, String dir,String path,boolean local) {
        BaseCode<List<Currency>> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<List<Currency>>>() {
                }.getType());
        if (base.getCode() == 0) {
//            if (save) {
//                SPUtils.put(phone + FUND_HAD_LOAD, base);
//            }
            if (!local&&dir != null && path != null) {
                try {
                    SPUtils.put(FUND_HAD_LOAD,true);
                    MyFileUtils.saveFile(dir, path, json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<Currency> list = base.getData();
            IlessonApp.getInstance().setCurrecys(list);
            if (null != currency) {
                for (Currency c : list) {
                    if (c.getCurrency().equals(currency.getCurrency())) {
                        currency = c;
                        showTitle(false);
                        break;
                    }
                }
            }
            datas.clear();
            datas.addAll(list);
            adapter.setLocal(local);
            adapter.notifyDataSetChanged();
        } else if (base.getCode() == -1) {
            crateFundLayout.setVisibility(View.VISIBLE);
            SPUtils.put(FUND_NOT_ACTIVED, true);
//            startActivity(new Intent(mainActivity, LoginActivity1.class));
//            mainActivity.finish();
        } else {
            Toast.makeText(mainActivity, base.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setData(BaseCode<List<Currency>> base, boolean save) {

    }


    private void active(){
        Intent intent = new Intent(mainActivity, PayPwdActivity.class);
        intent.putExtra(PayPwdActivity.ACTIVE_PASSWORD, true);
        mainActivity.startActivityForResult(intent, 0);
    }
}
