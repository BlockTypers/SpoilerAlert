package com.blocktyper.spoileralert.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class FurnaceListener extends SpoilerAlertListenerBase {
	
	
	public FurnaceListener(SpoilerAlertPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	/*
	 * ON FURNACE SMELT
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {
		ItemStack result = event.getResult();
		if (result == null) {
			return;
		}

		final World world = event.getBlock().getWorld();

		// if the food is not expired set daysSourceExpired to null in case it
		// is 0, we want setExpirationDate() to ignore it
		Long daysSourceExpired = getDaysExpired(event.getSource(), world, null);
		daysSourceExpired = daysSourceExpired == null || daysSourceExpired < 1 ? null : daysSourceExpired;

		event.setResult(setExpirationDate(result, world, daysSourceExpired, null));
	}
}
