package com.webkonsept.bukkit.chestlock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class ChestLockPlayerListener extends PlayerListener {
	ChestLock plugin;
	
	public ChestLockPlayerListener(ChestLock instance) {
		plugin = instance;
	}
	public void onPlayerInteract (PlayerInteractEvent event){
		
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		
		Block block = event.getClickedBlock();
		if (block == null) return;  // We don't care about non-block (air) interactions.
		if (block.getType() == Material.CHEST){
			if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				if (plugin.chests.isLocked(block)){
					Player player = event.getPlayer();
					String owner = plugin.chests.getOwner(block);
					boolean ignoreOwner = plugin.permit(player, "chestlock.ignoreowner");
					if (! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+"This chest was locked by "+owner);
					}
					else if (! owner.equalsIgnoreCase(player.getName()) && ignoreOwner){
						player.sendMessage(ChatColor.GREEN+owner+"'s chest opened");
					}
					else {
						player.sendMessage(ChatColor.GREEN+"Locked chest opened");
					}
				}
			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				ItemStack tool = event.getItem();
				if (tool == null) return; // TODO Support for "Air" or "Nothing" as key?
				if (tool.getType().equals(plugin.key)){
					Player player = event.getPlayer();
					if (plugin.permit(player,"chestlock.lock")){
						if (plugin.chests.isLocked(block)){
							String owner = plugin.chests.getOwner(block);
							if (owner.equalsIgnoreCase(player.getName())){
								if (plugin.chests.unlock(block)){
									player.sendMessage(ChatColor.GREEN+"Chest unlocked");
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking your chest!");
								}
							}
							else if (plugin.permit(player, "chestlock.ignoreowner")){
								if (plugin.chests.unlock(block)){
									Player ownerObject = plugin.server.getPlayer(owner);
									if (ownerObject != null){
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s chest, and taddle-taled on you for it.");
										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked your chest using mystic powers!");
									}
									else {
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s chest, but that user is offline, and was not notified.");
									}
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking "+owner+"'s chest!");
								}
								
							}
							else {
								player.sendMessage(ChatColor.RED+"This is "+owner+"'s chest, you can't unlock it!");
							}
							
						}
						else {
							if (plugin.chests.lock(player, block)){
								player.sendMessage(ChatColor.GREEN+"Chest locked!");
							}
							else{
								player.sendMessage(ChatColor.RED+"Error encountered while locking chest!");
							}
						}
					}
					else {
						player.sendMessage(ChatColor.RED+"You can't lock or unlock chests!  Permission denied!");
					}
				}
			}
		}
	}
}
