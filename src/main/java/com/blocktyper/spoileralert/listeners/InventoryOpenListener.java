package com.blocktyper.spoileralert.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class InventoryOpenListener extends SpoilerAlertListenerBase {

	public InventoryOpenListener(SpoilerAlertPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void inventoryOpenEvent(InventoryOpenEvent event) {

		if (event.getInventory() == null || event.getInventory().getContents() == null) {
			return;
		}

		HumanEntity player = event.getPlayer();

		if (player == null && event.getInventory().getViewers() != null
				&& !event.getInventory().getViewers().isEmpty()) {
			player = event.getInventory().getViewers().get(0);
		}

		if (player == null) {
			return;
		}

		List<ItemStack> newContents = new ArrayList<>();
		for (ItemStack item : event.getInventory().getContents()) {
			if (item != null) {
				newContents.add(setExpirationDate(item, player.getWorld(), null, player));
			} else {
				newContents.add(item);
			}
		}
		event.getInventory().setContents(newContents.toArray(new ItemStack[newContents.size()]));
	}
}
