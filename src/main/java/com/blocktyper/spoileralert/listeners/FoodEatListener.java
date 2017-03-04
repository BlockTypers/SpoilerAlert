package com.blocktyper.spoileralert.listeners;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.blocktyper.spoileralert.ConfigKeyEnum;
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
		
		List<String> immunePermissions = getConfig().getStringList(ConfigKeyEnum.IMMUNE_PERMISSIONS.getKey());
		if(playerIsImmune(event.getPlayer(), immunePermissions)){
			return;
		}

		Long daysExpired = getDaysExpired(event.getItem(), event.getPlayer().getWorld(), event.getPlayer());

		if (daysExpired == null || daysExpired < 1)
			return;
		

		makePlayerSick(daysExpired, event.getItem(), event.getPlayer());
	}
	
	public boolean playerIsImmune(HumanEntity player, List<String> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return false;
		}
		
		for (String permission : permissions) {
			if (player.hasPermission(permission)) {
				return true;
			}
		}

		return false;
	}
}

/*
 * 
 * 
 * 
 */
