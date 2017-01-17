package com.blocktyper.spoileralert.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class InventoryClickListener extends SpoilerAlertListenerBase {

	public InventoryClickListener(SpoilerAlertPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	/*
	 * ON INVENTORY CLICK
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
			return;
		}

		Player player = ((Player) event.getWhoClicked());

		// for language conversion only
		event.setCurrentItem(setExpirationDate(event.getCurrentItem(), player.getWorld(), null, event.getWhoClicked()));

		sendExpiredMessage(event.getCurrentItem(), player);
	}

}
