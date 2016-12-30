package com.blocktyper.spoileralert.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class FoodEatListener extends SpoilerAlertListenerBase {

	public FoodEatListener(SpoilerAlertPlugin plugin) {
		super(plugin);
	}

	/*
	 * ON FOOD EAT
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onFoodEat(PlayerItemConsumeEvent event) {

		Long daysExpired = getDaysExpired(event.getItem(), event.getPlayer().getWorld());

		if (daysExpired == null || daysExpired < 1)
			return;
		

		makePlayerSick(daysExpired, event.getItem(), event.getPlayer());
	}
}

/*
 * 
 * 
 * 
 */
