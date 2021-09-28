package com.ilesson.ppim.db;


import com.ilesson.ppim.entity.PPUserInfo;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class PPUserDao {

    private final String TAG = PPUserDao.class.getSimpleName();
    private DbManager dbManager;
    public PPUserDao() {
        dbManager = DatabaseManager.getInstance();
    }

    public List<PPUserInfo> getAllUserData(){

        List<PPUserInfo> allDevices = new ArrayList<>();
        try{
            allDevices = dbManager.selector(PPUserInfo.class).findAll();
        }catch (Exception e){
            e.printStackTrace();
        }

        return allDevices;
    }
    public List<PPUserInfo> getAllFriends(){
        List<PPUserInfo> datas = new ArrayList<>();
        try{
            datas = dbManager.selector(PPUserInfo.class).where("isFriend",
                    "=", true).findAll();
            if(null==datas){
                datas = new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return datas;
    }

    public List<PPUserInfo> getFriendsByKey(String key){
        List<PPUserInfo> datas = new ArrayList<>();
        try{
            datas = dbManager.selector(PPUserInfo.class).where("isFriend",
                    "=", true).and("name", "like", "%"+key+"%").or("nick", "like", "%"+key+"%").or("phone", "like", "%"+key+"%").findAll();
            if(null==datas){
                datas = new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return datas;
    }

    public PPUserInfo getFriendByKey(String key){
        try{
            return dbManager.selector(PPUserInfo.class).where("isFriend",
                    "=", true).and("phone", "=", key).findFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFriend(String phone){
        try {
            dbManager.delete(PPUserInfo.class, WhereBuilder.b("phone",
                    "=", phone).b("isFriend",
                    "=", true));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void insertList(String openid,List<PPUserInfo> list){
        for (PPUserInfo PPUserInfo : list) {
            insert(PPUserInfo);
        }
    }

    public void insert(PPUserInfo info){
        try {
            dbManager.save(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void update(PPUserInfo info){
        try {
            dbManager.saveOrUpdate(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void update(List<PPUserInfo> list){
        try {
            dbManager.saveOrUpdate(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


}
