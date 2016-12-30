package com.blocktyper.spoileralert.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class CakeListener extends SpoilerAlertListenerBase {

	public CakeListener(SpoilerAlertPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	/*
	 * ON EAT CAKE
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getClickedBlock().getType().equals(Material.CAKE_BLOCK)) {
			plugin.debugInfo("[onPlayerInteract] NOT CAKE");
			return;
		}

		boolean isEating = event.getAction().equals(Action.RIGHT_CLICK_BLOCK);

		SpoilerAlertCalendar expirationDate = getExistingExpirationDate(event.getClickedBlock());

		if (expirationDate == null) {
			if (!isEating)
				event.getPlayer().sendMessage("This cake has no expiration date.");
			return;
		}
		expirationDate.addDays(-1);

		Long daysSourceExpired = getDaysExpired(expirationDate.getDisplayDate(), event.getClickedBlock().getWorld());

		if (daysSourceExpired == null || daysSourceExpired < 1) {
			if (!isEating)
				event.getPlayer().sendMessage("This cake is not expired [" + expirationDate.getDisplayDate() + "].");
			return;
		}

		event.getPlayer()
				.sendMessage(ChatColor.RED + "This cake is expired [" + expirationDate.getDisplayDate() + "]!");

		if (!isEating) {
			return;
		}

		event.getPlayer()
				.sendMessage(ChatColor.RED + "Next time hit the cake first to find out if it is still good to eat.");

		ItemStack cakeItemStack = new ItemStack(Material.CAKE);
		makePlayerSick(daysSourceExpired, cakeItemStack, event.getPlayer());

	}

}
