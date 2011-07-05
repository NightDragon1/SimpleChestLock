package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class SimpleChestLockPlayerListener extends PlayerListener {
	SimpleChestLock plugin;
	
	public SimpleChestLockPlayerListener(SimpleChestLock instance) {
		plugin = instance;
	}
	public void onPlayerInteract (PlayerInteractEvent event){
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		Block block = event.getClickedBlock();
		
		
		Material toolUsed = Material.AIR;
		if (event.getItem() != null){
			toolUsed = event.getItem().getType();
		}
		
		if (block == null) return;  // We don't care about non-block (air) interactions.
		if (plugin.canLock(block)){
			if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || 
					( 
						event.getAction().equals(Action.LEFT_CLICK_BLOCK)
						&& plugin.leftLocked.contains(block.getType()))
						&& !(toolUsed.equals(plugin.key))
					){
				if (plugin.chests.isLocked(block)){
					Player player = event.getPlayer();
					String owner = plugin.chests.getOwner(block);
					plugin.babble(player.getName()+" wants to open "+owner+"'s chest");
					boolean ignoreOwner = plugin.permit(player, "simplechestlock.ignoreowner");
					if (! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+"This block was locked by "+owner);
					}
					else if (! owner.equalsIgnoreCase(player.getName()) && ignoreOwner){
						if (plugin.openMessage){
							player.sendMessage(ChatColor.GREEN+owner+"'s block opened");
						}
					}
					else {
						if (plugin.openMessage){
							player.sendMessage(ChatColor.GREEN+"Lock opened");
						}
					}
				}
			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				ItemStack tool = event.getItem();
				if (tool == null) return; // TODO Support for "Air" or "Nothing" as key?
				if (tool.getType().equals(plugin.key)){
					event.setCancelled(true);
					Player player = event.getPlayer();
					if (plugin.permit(player,"simplechestlock.lock")){
						if (plugin.chests.isLocked(block)){
							String owner = plugin.chests.getOwner(block);
							if (owner.equalsIgnoreCase(player.getName())){
								Integer unlockedChests = plugin.chests.unlock(block);
								if (unlockedChests == 1){
									player.sendMessage(ChatColor.GREEN+"Block unlocked");
								}
								else if (unlockedChests > 1){
									player.sendMessage(ChatColor.GREEN+"Double block unlocked");
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking your block!");
								}
							}
							else if (plugin.permit(player, "simplechestlock.ignoreowner")){
								Integer unlockedChests = plugin.chests.unlock(block);
								Player ownerObject = plugin.server.getPlayer(owner);
								if (unlockedChests == 1){
									if (ownerObject != null){
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s block, and taddle-taled on you for it.");
										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked your block using mystic powers!");
									}
									else {
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s block, but that user is offline, and was not notified.");
									}
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking "+owner+"'s block!");
								}
								
							}
							else {
								player.sendMessage(ChatColor.RED+"Locked by "+owner+":  You can't use it!");
							}
							
						}
						else {
							Integer chestsLocked = plugin.chests.lock(player, block);
							if (chestsLocked == 1){
								player.sendMessage(ChatColor.GREEN+"Block locked!");
							}
							else if (chestsLocked > 1){
								player.sendMessage(ChatColor.GREEN+"Double block locked!");
							}
							else{
								player.sendMessage(ChatColor.RED+"Error encountered while locking this block!");
							}
						}
					}
					else {
						player.sendMessage(ChatColor.RED+"You can't lock or unlock blocks!  Permission denied!");
					}
				}
			}
		}
	}
}
