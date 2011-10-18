/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.calzoneman.BuildSession;

/**
 *
 * @author Calvin
 */

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildSessionBlockListener extends BlockListener {
    private BuildSession plugin;
    
    public BuildSessionBlockListener(BuildSession plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer() != null && plugin.sessionExists(event.getPlayer().getName())) {
            if(event.getBlock().getTypeId() == 7 && !event.getPlayer().isOp()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You must be an operator to delete bedrock!");
            }
        }
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer() != null && plugin.sessionExists(event.getPlayer().getName())) {
            if(event.getBlock().getTypeId() == 7 && !event.getPlayer().isOp()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You must be an operator to place bedrock!");
            }
        }
    }
}
