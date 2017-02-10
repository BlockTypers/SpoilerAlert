package com.blocktyper.spoileralert.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.LocalizedMessageEnum;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class PlayerTradeListener extends SpoilerAlertListenerBase {

	public PlayerTradeListener(SpoilerAlertPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void stopSpoiledTrades(final InventoryClickEvent event) {
		boolean spoiledFoodFound = false;
		if (event.getClickedInventory() != null && event.getClickedInventory().getContents() != null
				&& event.getClickedInventory().getType() == InventoryType.MERCHANT) {

			HumanEntity player = event.getWhoClicked();

			if (event.getRawSlot() == 2) {
				for (ItemStack item : event.getClickedInventory().getContents()) {
					if (expired(item, player)) {
						spoiledFoodFound = true;
						event.getClickedInventory().remove(item);
						getPlayerHelper().tryToFitItemInPlayerInventory(item, player);
					}
				}
			} else if (event.getRawSlot() < 2) {

				if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_ONE
						|| event.getAction() == InventoryAction.PICKUP_HALF
						|| event.getAction() == InventoryAction.PICKUP_SOME) {
					return;
				}

				if (expired(event.getCurrentItem(), player)) {
					spoiledFoodFound = true;
				}

				if (!spoiledFoodFound && expired(event.getCursor(), player)) {
					spoiledFoodFound = true;
				}
			}

			if (spoiledFoodFound) {
				player.sendMessage(ChatColor.RED + getLocalizedMessage(LocalizedMessageEnum.CANT_TRADE_SPOILED_GOODS.getKey(), player));
				event.setCancelled(true);
			}
		}
	}
	
	private boolean expired(ItemStack item, HumanEntity player){
		if (item != null) {
			Long daysExpired = getDaysExpired(item, player.getWorld(), player);

			if (daysExpired != null && daysExpired > 0) {
				return true;
			}
		}
		return false;
	}
}
