package com.ilesson.ppim.entity;

import java.util.List;

/**
 * Created by potato on 2019/9/2.
 */

public class ComposeCollectBase extends ErrorBase {
    private List<ComposeCollectContent> data;

    public List<ComposeCollectContent> getData() {
        return data;
    }

    public void setData(List<ComposeCollectContent> data) {
        this.data = data;
    }
}
