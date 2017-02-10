package com.blocktyper.spoileralert.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class PickupListener extends SpoilerAlertListenerBase {

	public PickupListener(SpoilerAlertPlugin plugin) {
		super(plugin);
	}

	/*
	 * ON PLAYER PICK UP
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {

		Item item = event.getItem();

		if (item == null) {
			return;
		}

		ItemStack newItemStack = setExpirationDate(item.getItemStack(), item.getWorld(), null, event.getPlayer());

		if (continuousTranslationEnabled()) {
			newItemStack = convertItemStackLanguage(newItemStack, event.getPlayer());
		}

		item.setItemStack(newItemStack);
	}

}
