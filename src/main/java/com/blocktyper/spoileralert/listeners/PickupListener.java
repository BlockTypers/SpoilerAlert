package com.blocktyper.spoileralert.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class PickupListener extends SpoilerAlertListenerBase {

	public PickupListener(SpoilerAlertPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * ON PLAYER PICK UP
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {

		plugin.debugInfo("onPlayerPickupItem");

		Item item = event.getItem();

		if (item == null) {
			plugin.debugInfo("onPlayerPickupItem item == null");
			return;
		}

		item.setItemStack(setExpirationDate(item.getItemStack(), item.getWorld(), null));
	}
	
}
