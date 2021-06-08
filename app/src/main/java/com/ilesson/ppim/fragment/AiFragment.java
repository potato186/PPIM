package com.ilesson.ppim.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ilesson.ppim.R;

import org.xutils.view.annotation.ContentView;

/**
 * Created by potato on 2016/4/12.
 */
@ContentView(R.layout.fragment_ai)
public class AiFragment extends BaseFragment {
    private static final String TAG = "AiFragment";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        MainActivity mainActivity = (MainActivity) getActivity();
//        if(isVisibleToUser){
//            mainActivity.request("");
//        }
//    }
}
