package com.ilesson.ppim.db;


import com.ilesson.ppim.entity.ConversationInfo;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class ConversationDao {

    private final String TAG = ConversationDao.class.getSimpleName();
    private DbManager dbManager;

    public ConversationDao() {
        dbManager = DatabaseManager.getInstance();
    }

    public List<ConversationInfo> getConversations(){
        List<ConversationInfo> datas = new ArrayList<>();
        try{
            datas = dbManager.selector(ConversationInfo.class).orderBy("date").findAll();
        }catch (Exception e){
            e.printStackTrace();
        }
        return datas;
    }
//    public List<ConversationInfo> getConversations(String key){
//
//    }
    public void deleteGroup(String groupId){
        try {
            dbManager.delete(ConversationInfo.class, WhereBuilder.b("groupId",
                    "=", groupId));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void delete(ConversationInfo info){
        try {
            dbManager.delete(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void update(ConversationInfo info){
        try {
            dbManager.saveOrUpdate(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
