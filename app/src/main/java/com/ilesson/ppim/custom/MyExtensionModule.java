package com.ilesson.ppim.custom;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imlib.model.Conversation;

/**
 * Created by potato on 2020/3/12.
 */

public class MyExtensionModule extends DefaultExtensionModule {
    public static boolean shopGroup;
    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = new ArrayList();
        pluginModules.add(new ImagePlugin());
        pluginModules.add(new FilePlugin());
        pluginModules.add(new ComposePlugin());
        if (conversationType == Conversation.ConversationType.PRIVATE) {
        }else if (conversationType == Conversation.ConversationType.GROUP) {
            if(shopGroup){
                pluginModules.add(new CustomServerPlugin());
            }else{
            }
            pluginModules.add(new TransactionPlugin());
//            pluginModules.add(new RedPacketPlugin());
        }
        if(!shopGroup){
//            pluginModules.add(new PPLoctionPlugin());
//            pluginModules.add(new ContactCardPlugin());
        }
        return pluginModules;
    }
}
