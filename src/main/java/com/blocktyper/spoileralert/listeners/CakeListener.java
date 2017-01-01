package com.blocktyper.spoileralert.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.LocalizedMessageEnum;
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
			String cakeHasNoExpirationDateText = plugin.getLocalizedMessage(LocalizedMessageEnum.CAKE_HAS_NO_EXPIRATION_DATE.getKey());
			
			if (!isEating)
				event.getPlayer().sendMessage(cakeHasNoExpirationDateText);
			return;
		}
		expirationDate.addDays(-1);

		Long daysSourceExpired = getDaysExpired(expirationDate.getDisplayDate(), event.getClickedBlock().getWorld());

		if (daysSourceExpired == null || daysSourceExpired < 1) {
			String cakeNotExpiredText = plugin.getLocalizedMessage(LocalizedMessageEnum.CAKE_NOT_EXPIRED.getKey());
			if (!isEating)
				event.getPlayer().sendMessage(cakeNotExpiredText + " [" + expirationDate.getDisplayDate() + "].");
			return;
		}
		
		sendExpiredMessage(daysSourceExpired, Material.CAKE, event.getPlayer());

		if (!isEating) {
			return;
		}
		
		if(event.getPlayer().getFoodLevel() >= 20){
			plugin.debugInfo("event.getPlayer().getFoodLevel() >= 20");
			plugin.debugInfo("  -" + event.getPlayer().getFoodLevel());
			return;
		}

		String hitCakesFirstText = plugin.getLocalizedMessage(LocalizedMessageEnum.HIT_CAKES_FIRST.getKey());
		event.getPlayer().sendMessage(ChatColor.RED + hitCakesFirstText);

		ItemStack cakeItemStack = new ItemStack(Material.CAKE);
		makePlayerSick(daysSourceExpired, cakeItemStack, event.getPlayer());

	}

}
