package com.gmail.haloinverse.DynamicMarket;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.iConomy.iConomy;

/**
 * iPluginListener Allows us to hook into permissions even if it is loaded later on.
 * 
 * Checks for Plugins on the event that they are enabled, checks the name given with the usual name of the plugin to verify the existence. If the name matches we pass the plugin along to iConomy to utilize in various ways.
 * 
 * @author Nijikokun
 */
public class iPluginListener extends ServerListener {
    public iPluginListener() {
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (DynamicMarket.economy != null) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                DynamicMarket.setiConomy(null);
                System.out.println("[DynamicMarket] un-hooked from iConomy.");
            }
        }
    }
    
    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin iConomy = event.getPlugin().getServer().getPluginManager().getPlugin("iConomy");
        
        if (iConomy != null) {
            if (iConomy.isEnabled() && iConomy.getClass().getName().equals("com.iConomy.iConomy")) {
                if (iConomy != null) {
                    DynamicMarket.setiConomy((iConomy) iConomy);
                }
                System.out.println("[DynamicMarket] hooked into iConomy.");
            }
        }
    }
}