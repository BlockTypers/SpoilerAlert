package com.blocktyper.spoileralert.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.blocktyper.spoileralert.ConfigKeyEnum;
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

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.SET_EXPIRATION_ON_INVENTORY_CLICK.getKey(), true)) {
			event.setCurrentItem(setExpirationDate(event.getCurrentItem(), player.getWorld(), null));
		}

		sendExpiredMessage(event.getCurrentItem(), player);
	}

}
