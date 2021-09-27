package com.ilesson.ppim.entity;

import java.io.Serializable;

/**
 * @ClassName: SearchInfo
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/27 15:06
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/27 15:06
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SearchInfo implements Serializable {
    private int searchType;

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }
}
