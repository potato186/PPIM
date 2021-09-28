package com.ilesson.ppim.db;


import com.ilesson.ppim.entity.GroupInfo;
import com.ilesson.ppim.entity.PPUserInfo;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupUserDao {

    private final String TAG = GroupUserDao.class.getSimpleName();
    private DbManager dbManager;

    public GroupUserDao() {
        dbManager = DatabaseManager.getInstance();
    }

    public List<GroupInfo> getAllData(){

        List<GroupInfo> allDevices = new ArrayList<>();
        try{
            List<GroupInfo> datas = dbManager.selector(GroupInfo.class).findAll();
            return datas==null?allDevices:datas;

        }catch (Exception e){
            e.printStackTrace();
        }

        return allDevices;
    }

    public List<GroupInfo> getGroupByKey(String key){
        try{
            List<GroupInfo> datas = dbManager.selector(GroupInfo.class).where("name", "like", "%"+key+"%").or("tag", "like", "%"+key+"%").findAll();
            return datas==null?new ArrayList<GroupInfo>():datas;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<GroupInfo> searchByKey(String key){
        List<GroupInfo> all = getAllData();
        List<GroupInfo> groups = getGroupByKey(key);
        Map<String,String>map=new HashMap<>();
        for (GroupInfo group : groups) {
            map.put(group.getId(),group.getId());
        }
        for (GroupInfo groupInfo : all) {
            if(null==map.get(groupInfo.getId())){
                List<PPUserInfo> userInfos = groupInfo.getUsers();

                for (PPUserInfo userInfo : userInfos) {
                    if(userInfo.getName().contains(key)){
                        groupInfo.setUserName(userInfo.getName());
                        groups.add(groupInfo);
                        break;
                    }
                }
            }
        }
        return groups;
    }
    public void deleteGroup(String groupId){
        try {
            dbManager.delete(GroupInfo.class, WhereBuilder.b("groupId",
                    "=", groupId));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void insert(GroupInfo info){
        try {
            dbManager.save(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void update(GroupInfo info){
        try {
            dbManager.saveOrUpdate(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
