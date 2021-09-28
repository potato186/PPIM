package com.ilesson.ppim.entity;

import java.util.List;

/**
 * @ClassName: AllSearchInfo
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/28 14:16
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/28 14:16
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class AllSearchInfo {
    private String searchType;
    public List<SearchInfo> searchInfos;
    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public List<SearchInfo> getSearchInfos() {
        return searchInfos;
    }

    public void setSearchInfos(List<SearchInfo> searchInfos) {
        this.searchInfos = searchInfos;
    }
}
