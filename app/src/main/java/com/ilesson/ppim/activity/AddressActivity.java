package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.AddressInfo;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.CityList;
import com.ilesson.ppim.entity.ContryCode;
import com.ilesson.ppim.entity.ExchangeAddress;
import com.ilesson.ppim.entity.ProvinceList;
import com.ilesson.ppim.utils.Constants;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_address)
public class AddressActivity extends BaseActivity {
    @ViewInject(R.id.user_name)
    private EditText userName;
    @ViewInject(R.id.user_phone)
    private EditText userPhone;
    @ViewInject(R.id.adress_detail)
    private EditText adressDetail;
    @ViewInject(R.id.edit_tag)
    private EditText editTag;
    @ViewInject(R.id.save)
    private TextView save;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.home)
    private TextView homeView;
    @ViewInject(R.id.company)
    private TextView companyView;
    @ViewInject(R.id.school)
    private TextView schoolView;
    @ViewInject(R.id.adress_area)
    private TextView adressArea;
    @ViewInject(R.id.phone_prefix)
    private TextView phonePrefix;
    private EditText[] editTexts;
    private TextView[] tagViews;
    private static final String ACTION_ADD = "add";
    private static final String ACTION_MODIFY = "modify";
    public static final String ACTION_MODIFY_BUY = "action_modify_buy";
    public static final String CURRENTADDRESS = "currentAddress";
    public static final int SET_ADDRESS_SUCCESS = 1;
    private String tagContent;
    private boolean useAddress;
    private boolean buyAddress;
    private String countryCode;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this, true);
        save.setEnabled(false);
        editTexts = new EditText[]{userName, userPhone, adressDetail};
        tagViews = new TextView[]{homeView, companyView, schoolView};
        currentAddress = (AddressInfo) getIntent().getSerializableExtra(AddressActivity.CURRENTADDRESS);
        useAddress = getIntent().getBooleanExtra(ExchangeActivity.ADDRESS_INFO, false);
        buyAddress = getIntent().getBooleanExtra(ACTION_MODIFY_BUY, false);
        if (null == currentAddress) {
            title.setText(R.string.create_address);
        } else {
            tagContent = currentAddress.getTag();
            province = currentAddress.getProvince();
            city = currentAddress.getCity();
            for (int i = 0; i < tagViews.length; i++) {
                if (tagViews[i].getText().toString().equals(tagContent)) {
                    selectTagView(i);
                }
            }
            userName.setText(currentAddress.getName());
            adressDetail.setText(currentAddress.getAddress());
            userPhone.setText(currentAddress.getPhone());
        }
//        getContry();
        getArea();
        addListener();
        editTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (content.length() > 5) {
                    content = content.substring(0, 6);
                    Toast.makeText(AddressActivity.this, R.string.most_inupt_five_words, Toast.LENGTH_SHORT).show();
                }
                if (!getResources().getString(R.string.home).equals(content) && !getResources().getString(R.string.company).equals(content) && !getResources().getString(R.string.school).equals(content)) {
                    selectTagView(-1);
                }
                tagContent = content;
                checkHome();
                checkSave();
            }
        });
    }

    private static final String TAG = "AdressActivity";

    @Event(R.id.save)
    private void save(View view) {
        if (TextUtils.isEmpty(tagContent)) {
            Toast.makeText(this, R.string.choose_address_tag, Toast.LENGTH_LONG).show();
            return;
        }
        if (homeItems == 3) {
            String action = currentAddress == null ? ACTION_ADD : ACTION_MODIFY;
            int id = currentAddress == null ? -1 : currentAddress.getId();
            add(id, action, tagContent, userName.getText().toString(), userPhone.getText().toString(), adressDetail.getText().toString());
        }
    }

    @Event(R.id.home)
    private void home(View view) {
        selectTagView(0);
    }

    @Event(R.id.company)
    private void company(View view) {
        selectTagView(1);
    }

    @Event(R.id.school)
    private void school(View view) {
        selectTagView(2);
    }

    @Event(R.id.phone_prefix_layout)
    private void phonePrefix(View view) {
        showCountryDialog();
    }

    @Event(R.id.area)
    private void area(View view) {
        if (pvOptions != null && !pvOptions.isShowing()) {
            pvOptions.show();
            hideInput();
        }
    }

    private void selectTagView(int index) {
        for (int i = 0; i < tagViews.length; i++) {
            if (i == index) {
                tagViews[i].setBackgroundResource(R.drawable.background_theme_corner20);
                tagViews[i].setTextColor(getResources().getColor(R.color.white));
                tagContent = tagViews[i].getText().toString();
            } else {
                tagViews[i].setBackgroundResource(R.drawable.general_gray_edge_white_corner20_selector);
                tagViews[i].setTextColor(getResources().getColor(R.color.helptext_color));
            }
        }
        if (currentAddress != null) {
            if (tagContent.equals(currentAddress.getTag())) {
                changed = false;
            } else {
                changed = true;
            }
        } else {
            changed = true;
        }
        if (index >= 0 && index < tagViews.length) {
            editTag.setText(tagViews[index].getText());
        }
        checkHome();
        checkSave();
    }

    @Event(R.id.back)
    private void back(View view) {
        finish();
    }

    private void add(final int id, String action, final String tag, final String name, final String phone, final String address) {
        //map.put("province", province);
        //map.put("city", city);
        //map.put("country", country);
        save.setEnabled(false);
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ADDRESS);
        params.addParameter("action", action);
        if (id >= 0) {
            params.addParameter("id", id);
        }
        params.addParameter("province", province);
        params.addParameter("city", city);
        params.addParameter("name", name);
//        params.addParameter("country", phone);
        params.addParameter("phone", phone);
        String ads = address;
//        if(!address.contains(city)){
//            ads=city+address;
//        }
//        if(!address.contains(province)){
//            ads=province+ads;
//        }
        params.addParameter("address", ads);
//        params.addParameter("prefix", phonePrefix);
        params.addParameter("tag", tag);
        Log.d(TAG, "search: " + params.toString());
        showProgress();
        final String faddress = ads;
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "add: " + result);
                BaseCode<List<AddressInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<AddressInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    AddressInfo addressInfo = new AddressInfo(faddress, phone, name, tag);
                    addressInfo.setProvince(province);
                    addressInfo.setCity(city);
                    addressInfo.setId(id);
                    if (currentAddress == null) {

                    }
                    if (id > 0 && buyAddress) {
                        EventBus.getDefault().post(addressInfo);
                    }
                    EventBus.getDefault().post(new ExchangeAddress(addressInfo));
                    List<AddressInfo> list = (List<AddressInfo>) base.getData();
                    for (AddressInfo info : list) {
                        if (info.getAddress().equals(address) && info.getName().equals(name) && info.getPhone().equals(phone)) {
                            addressInfo.setId(info.getId());
                            break;
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra(ExchangeActivity.ADDRESS_INFO, addressInfo);
                    intent.putExtra(CURRENTADDRESS, currentAddress);
                    int resultCode = useAddress ? ExchangeActivity.SET_ADDRESS_SUCCESS_TO_USE : SET_ADDRESS_SUCCESS;
                    setResult(resultCode, intent);
                    finish();
                } else {
                    showToast(base.getMessage());
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                save.setEnabled(true);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                save.setEnabled(true);
            }

            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    private void getArea() {
        RequestParams params = new RequestParams(Constants.BASE_URL + "/country/city.json");
        Log.d(TAG, "search: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                readArea(result);
                return true;
            }

            @Override
            public void onSuccess(String result) {
                readArea(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    private List<String> provinceLists;
    private List<List<String>> cityLists;
    private int provienceIndex;
    private int cityIndex;

    private void readArea(String json) {
        List<ProvinceList> list1 = new Gson().fromJson(
                json,
                new TypeToken<List<ProvinceList>>() {
                }.getType());
        provinceLists = new ArrayList<>();
        cityLists = new ArrayList<>();
        String provinceName = getResources().getString(R.string.default_province);
        String address = provinceName;
        String p = "";
        String c = "";
        if (currentAddress != null) {
            address = currentAddress.getAddress();
            if (!TextUtils.isEmpty(currentAddress.getProvince())) {
                if (!address.startsWith(currentAddress.getProvince())) {
                    address = currentAddress.getProvince() + currentAddress.getCity() + address;
                }
                adressArea.setText(currentAddress.getProvince() + currentAddress.getCity());
            }
            if (!currentAddress.getTag().equals(getResources().getString(R.string.home)) && !currentAddress.getTag().equals(getResources().getString(R.string.company)) && !currentAddress.getTag().equals(getResources().getString(R.string.school))) {
                editTag.setText(currentAddress.getTag());
            }
        }
        for (int i = 0; i < list1.size(); i++) {
            ProvinceList province = list1.get(i);
            if (currentAddress == null) {
                if (province.getName().equals(provinceName)) {
                    provienceIndex = i;
                }
            } else {
                if (address.startsWith(province.getName())) {
                    provienceIndex = i;
                    p = province.getName();
                    address = address.replace(province.getName(), "");
                }
            }

            provinceLists.add(province.getName());
            List<String> cs = new ArrayList<>();
            for (int j = 0; j < province.getCityList().size(); j++) {
                CityList city = province.getCityList().get(j);
                cs.add(city.getName());
                if (currentAddress != null && address.startsWith(city.getName())) {
                    cityIndex = j;
                    c = city.getName();
                }
            }
            cityLists.add(cs);
        }
        if (!TextUtils.isEmpty(p)) {
            if (p.equals(c)) {
                adressArea.setText(p);
            } else {
                adressArea.setText(p + c);
            }
        }
        initOptionPicker();
    }

    private AddressInfo currentAddress;

    private int homeItems;

    private void checkHome() {
        homeItems = 0;
        for (EditText editText : editTexts) {
            if (!TextUtils.isEmpty(editText.getText().toString())) {
                homeItems++;
            }
        }
    }

    private boolean changed;

    private boolean checkSave() {
        String areas = adressArea.getText().toString();
        if (homeItems == 3 && !TextUtils.isEmpty(areas) && !TextUtils.isEmpty(tagContent)) {
            changed = true;
        }

        if (changed) {
            save.setTextColor(getResources().getColor(R.color.white));
            save.setBackgroundResource(R.drawable.general_theme_corner20_selector);
            save.setEnabled(true);
        } else {
            save.setTextColor(getResources().getColor(R.color.color_999999));
            save.setBackgroundResource(R.drawable.background_gray_corner20);
            save.setEnabled(false);
        }
        return false;
    }

    private void addListener() {
        for (EditText editText : editTexts) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    checkHome();
                    checkSave();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private OptionsPickerView pvOptions;
    private String area;
    private String province;
    private String city;

    private void initOptionPicker() {//条件选择器初始化

        /**
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         */

        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                area = provinceLists.get(options1) + provinceLists.get(options2);
                Log.d(TAG, "onOptionsSelect: " + area);
                setSelectArea(options1, options2);
            }

        }).setSelectOptions(provienceIndex, cityIndex)//默认选中项
                .setCancelColor(getResources().getColor(R.color.helptext_color))
                .setSubmitColor(getResources().getColor(R.color.second_blk_text))
                .setOptionsSelectChangeListener(new OnOptionsSelectChangeListener() {
                    @Override
                    public void onOptionsSelectChanged(int options1, int options2, int options3) {
                        setSelectArea(options1, options2);
                    }
                })
                .setDividerColor(0xFFeb0c6e)//设置分割线颜色
                .build();
        pvOptions.setPicker(provinceLists, cityLists);//二级选择器
    }

    private void setSelectArea(int options1, int options2) {
        province = provinceLists.get(options1);
        city = cityLists.get(options1).get(options2);
        if (province.equals(city)) {
            area = province;
        } else {
            area = province + city;
        }
        adressArea.setText(area);
        if (currentAddress != null) {
            if (area.equals(currentAddress.getProvince() + currentAddress.getCity())) {
                changed = false;
            } else {
                changed = true;
            }
        } else {
            changed = true;
        }
        checkHome();
        checkSave();
    }

    private List<ContryCode> contryCodes;

    private void getContry() {
        contryCodes = new ArrayList<>();
        RequestParams params = new RequestParams(Constants.BASE_URL + "/country/country.json");
        org.xutils.x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                List<ContryCode> datas = new Gson().fromJson(
                        result,
                        new TypeToken<List<ContryCode>>() {
                        }.getType());
                contryCodes.clear();
                contryCodes.addAll(datas);
                return true;
            }

            @Override
            public void onSuccess(String result) {
                List<ContryCode> datas = new Gson().fromJson(
                        result,
                        new TypeToken<List<ContryCode>>() {
                        }.getType());
                contryCodes.clear();
                contryCodes.addAll(datas);
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

    private Dialog mCountryDialog;

    private void showCountryDialog() {
        mCountryDialog = new Dialog(this);
        mCountryDialog.setCanceledOnTouchOutside(false);
        Window window = mCountryDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        View view = View.inflate(this, R.layout.country_layout, null);
        ListView listView = view.findViewById(R.id.country_list);
        listView.setAdapter(new CountryAdapter(contryCodes));
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        window.setBackgroundDrawableResource(android.R.color.transparent);
        mCountryDialog.setCanceledOnTouchOutside(true);
        mCountryDialog.show();
    }

    private class CountryAdapter extends BaseAdapter {

        private List<ContryCode> data;

        private CountryAdapter(List<ContryCode> data) {
            this.data = data;
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
            final ContryCode content = data.get(i);
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.phone_country_item, null);
                holder = new ViewHolder();
                holder.countryTextView = view.findViewById(R.id.country_name);
                holder.codeTextView = view.findViewById(R.id.country_code);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.countryTextView.setText(content.getCn());
            holder.codeTextView.setText(content.getTel());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    countryCode = content.getTel();
                    phonePrefix.setText("+" + countryCode);
                    mCountryDialog.dismiss();
                }
            });
            return view;
        }
    }

    class ViewHolder {
        private TextView countryTextView;
        private TextView codeTextView;
    }

    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(adressArea.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideInput();
    }
}
