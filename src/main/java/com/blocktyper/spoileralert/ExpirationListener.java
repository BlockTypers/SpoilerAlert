package com.blocktyper.spoileralert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExpirationListener implements Listener {

	protected SpoilerAlertPlugin plugin;

	public static final int DEFAULT_LIFE_SPAN_IN_DAYS = 7;

	public ExpirationListener(SpoilerAlertPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {
		plugin.debugInfo("onFurnaceSmeltEvent");

		ItemStack result = event.getResult();
		if (result == null) {
			plugin.debugInfo("onFurnaceSmeltEvent result == null");
			return;
		}

		final World world = event.getBlock().getWorld();

		// if the food is not expired set daysSourceExpired to null in case it
		// is 0, we want setExpirationDate() to ignore it
		Long daysSourceExpired = getDaysExpired(event.getSource(), world);
		daysSourceExpired = daysSourceExpired < 1 ? null : daysSourceExpired;

		event.setResult(setExpirationDate(result, world, daysSourceExpired));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		plugin.debugInfo("onPrepareItemCraft");

		ItemStack result = event.getInventory().getResult();
		if (result == null) {
			plugin.debugInfo("onPrepareItemCraft result == null");
			return;
		}

		final World world = event.getViewers().get(0).getWorld();
		ItemStack[] itemsInCraftingTable = event.getInventory().getMatrix();

		OptionalLong optionalLong = Arrays.asList(itemsInCraftingTable).stream()
				.filter(i -> getDaysExpired(i, world) > 0).mapToLong(i -> getDaysExpired(i, world)).max();

		// if the food is not expired set daysSourceExpired to null in case it
		// is 0, we want setExpirationDate() to ignore it
		Long daysSourceExpired = optionalLong != null && optionalLong.isPresent() ? optionalLong.getAsLong() : null;
		daysSourceExpired = daysSourceExpired < 1 ? null : daysSourceExpired;

		event.getInventory().setResult(setExpirationDate(result, world, daysSourceExpired));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {

		plugin.debugInfo("onPlayerPickupItem");

		Item item = event.getItem();

		if (item == null) {
			plugin.debugInfo("onPlayerPickupItem item == null");
			return;
		}

		item.setItemStack(setExpirationDate(item.getItemStack(), item.getWorld(), null));
	}

	private ItemStack setExpirationDate(ItemStack itemStack, World world, Long daysExpired) {
		plugin.debugInfo("setExpirationDate");

		if (itemStack == null) {
			plugin.debugInfo("setExpirationDate itemStack == null");
			return itemStack;
		}

		Optional<String> lifeSpan = plugin.getConfig().getStringList(ConfigKeyEnum.LIFE_SPANS.getKey()).stream()
				.filter(l -> l.startsWith(itemStack.getType().name())).findFirst();

		if (lifeSpan == null || !lifeSpan.isPresent()) {
			plugin.debugInfo("setExpirationDate lifeSpan == null || !lifeSpan.isPresent()");
			return itemStack;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();

		String perishableText = "[" + plugin.getLocalizedMessage(LocalizedMessageEnum.PERISHABLE.getKey()) + "]";

		String displayname = itemMeta.getDisplayName() != null ? itemMeta.getDisplayName()
				: WordUtils.capitalizeFully(itemStack.getType().name().replaceAll("_", " "));
		if (!displayname.endsWith(perishableText)) {
			itemMeta.setDisplayName(displayname + " " + ChatColor.RED + perishableText);
			itemStack.setItemMeta(itemMeta);
		}

		String expirationDateText = "[" + plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRATION_DATE.getKey())
				+ "]";

		boolean addLore = true;

		List<String> lore = itemMeta.getLore();

		if (lore != null && !lore.isEmpty()) {
			plugin.debugInfo("setExpirationDate lore != null && !lore.isEmpty()");
			long existingLoreCount = lore.stream().filter(l -> l.contains(expirationDateText)).count();

			plugin.debugInfo("onPlayerPickupItem existingLoreCount: " + existingLoreCount);
			addLore = existingLoreCount < 1;
			plugin.debugInfo("addLore: " + addLore);
		} else {
			plugin.debugInfo("setExpirationDate lore == null || lore.isEmpty()");
		}

		if (addLore) {

			int days = 0;

			if (daysExpired != null) {
				days = (daysExpired.intValue()+1)*-1;
			} else {
				String lifeSpanExpression = lifeSpan.get();
				String daysString = lifeSpanExpression.substring(lifeSpanExpression.indexOf("=") + 1);
				days = Integer.parseInt(daysString);
			}

			SpoilerAlertCalendar expirationDate = new SpoilerAlertCalendar(world);
			expirationDate.addDays(days);
			String text = ChatColor.RED + expirationDateText + ": (" + expirationDate.getDisplayDate() + ")";
			if (lore == null)
				lore = new ArrayList<>();
			lore.add(text);
			itemMeta.setLore(lore);
			itemStack.setItemMeta(itemMeta);
		}

		return itemStack;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
			return;
		}

		Player player = ((Player) event.getWhoClicked());
		
		if(plugin.getConfig().getBoolean(ConfigKeyEnum.SET_EXPIRATION_ON_INVENTORY_CLICK.getKey(), true)){
			event.setCurrentItem(setExpirationDate(event.getCurrentItem(), player.getWorld(), null));
		}

		Long daysExpired = getDaysExpired(event.getCurrentItem(), player.getWorld());

		if (daysExpired != null && daysExpired > 0) {
			int lifeSpanInDays = getLifeSpanIndays(event.getCurrentItem().getType());
			int buffMagnitude = getBuffMagnitude(daysExpired, lifeSpanInDays);

			event.getWhoClicked()
					.sendMessage(ChatColor.RED + "EXPIRED: " + daysExpired + " days! Danger level: " + buffMagnitude);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onFoodEat(PlayerItemConsumeEvent event) {

		Long daysExpired = getDaysExpired(event.getItem(), event.getPlayer().getWorld());

		if (daysExpired == null || daysExpired < 1)
			return;

		int lifeSpanInDays = getLifeSpanIndays(event.getItem().getType());

		int buffMagnitude = getBuffMagnitude(daysExpired, lifeSpanInDays);

		Long buffDuration = plugin.getConfig().getLong(ConfigKeyEnum.BASE_DEBUFF_DURATION_IN_SECONDS.getKey(), 30);
		buffDuration = buffDuration * (1 + (daysExpired / lifeSpanInDays));

		plugin.debugInfo("onFoodEat buffMagnitude: " + buffMagnitude);
		plugin.debugInfo("onFoodEat buffDuration: " + buffDuration);

		if(plugin.getConfig().getBoolean(ConfigKeyEnum.BLINDING_ENABLED.getKey(), true)){
			event.getPlayer().addPotionEffect(
					new PotionEffect(PotionEffectType.BLINDNESS, buffDuration.intValue() * 20, buffMagnitude));
		}
		
		if(plugin.getConfig().getBoolean(ConfigKeyEnum.CONFUSION_ENABLED.getKey(), true)){
			event.getPlayer().addPotionEffect(
					new PotionEffect(PotionEffectType.CONFUSION, buffDuration.intValue() * 20, buffMagnitude));
		}

		
		if(plugin.getConfig().getBoolean(ConfigKeyEnum.DECREASE_FOOD_LEVEL_ENABLED.getKey(), true)){
			Double newFoodLevel = event.getPlayer().getFoodLevel() * (1.0 / buffMagnitude);
			newFoodLevel = newFoodLevel < 0 ? 0 : newFoodLevel;
			event.getPlayer().setFoodLevel(newFoodLevel.intValue());
		}
		
		if(plugin.getConfig().getBoolean(ConfigKeyEnum.DECREASE_SATURATION_ENABLED.getKey(), true)){
			if (event.getPlayer().getSaturation() > 0) {
				event.getPlayer().setSaturation(event.getPlayer().getSaturation() * (1.0f / buffMagnitude));
			}
		}
		
		if(plugin.getConfig().getBoolean(ConfigKeyEnum.HARM_ENABLED.getKey(), true)){
			Long ageBeforeHarmProportion = plugin.getConfig()
					.getLong(ConfigKeyEnum.AGE_BEFORE_HARM_AS_PROPORTION_OF_SHELF_LIFE.getKey(), 3);
			plugin.debugInfo("onFoodEat ageBeforeHarmProportion: " + ageBeforeHarmProportion);
			plugin.debugInfo(
					"onFoodEat lifeSpanInDays*ageBeforeHarmProportion: " + lifeSpanInDays * ageBeforeHarmProportion);
			if (lifeSpanInDays * ageBeforeHarmProportion <= daysExpired) {
				int harmMagnifier = buffMagnitude;
				if(ageBeforeHarmProportion <= buffMagnitude){
					harmMagnifier = buffMagnitude/(ageBeforeHarmProportion.intValue());
				}else if(buffMagnitude > 1){
					buffMagnitude = buffMagnitude/2;
				}
				if(ageBeforeHarmProportion <= buffDuration){
					buffDuration = buffDuration/(ageBeforeHarmProportion.intValue());
				}else if(buffDuration > 1){
					buffDuration = buffDuration/2;
				}
				event.getPlayer().addPotionEffect(
						new PotionEffect(PotionEffectType.HARM, buffDuration.intValue() * 20, harmMagnifier));
			}
		}
		
	}

	private int getLifeSpanIndays(Material material) {
		Optional<String> lifeSpan = plugin.getConfig().getStringList(ConfigKeyEnum.LIFE_SPANS.getKey()).stream()
				.filter(l -> l.startsWith(material.name())).findFirst();

		int lifeSpanInDays = DEFAULT_LIFE_SPAN_IN_DAYS;
		if (lifeSpan != null && lifeSpan.isPresent()) {
			String lifeSpanExpression = lifeSpan.get();
			String daysString = lifeSpanExpression.substring(lifeSpanExpression.indexOf("=") + 1);
			lifeSpanInDays = Integer.parseInt(daysString);
		}

		return lifeSpanInDays;
	}

	private int getBuffMagnitude(long daysExpired, int lifeSpanInDays) {
		Long additionalMagnitude = (daysExpired / ((lifeSpanInDays < 4 ? 4 : lifeSpanInDays) / 4));
		int buffMagnitude = 1 + additionalMagnitude.intValue();
		return buffMagnitude;
	}

	private long getDaysExpired(ItemStack itemStack, World world) {

		if (itemStack == null) {
			return 0;
		}

		ItemMeta meta = itemStack.getItemMeta();
		String perishableText = "[" + plugin.getLocalizedMessage(LocalizedMessageEnum.PERISHABLE.getKey()) + "]";

		plugin.debugInfo("itemStack.getType().name(): " + itemStack.getType().name());

		if (meta == null || meta.getDisplayName() == null || !meta.getDisplayName().endsWith(perishableText)) {
			if (plugin.config().debugEnabled()) {
				if (meta == null)
					plugin.debugInfo("meta == null");
				else if (meta.getDisplayName() == null)
					plugin.debugInfo("meta.getDisplayName() == null");
				else if (!meta.getDisplayName().endsWith(perishableText))
					plugin.debugInfo("!meta.getDisplayName().endsWith(" + perishableText + ")");
			}

			return 0;
		}

		if (meta.getLore() == null || meta.getLore().isEmpty()) {
			plugin.debugInfo("getDaysExpired meta.getLore() == null || meta.getLore().isEmpty()");
			return 0;
		}

		String expirationDateText = "[" + plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRATION_DATE.getKey())
				+ "]";

		Optional<String> expirationOptional = meta.getLore().stream().filter(l -> l.contains(expirationDateText))
				.findFirst();

		if (expirationOptional == null || !expirationOptional.isPresent()) {
			plugin.debugInfo("getDaysExpired expirationOptional == null || !expirationOptional.isPresent()");
			return 0;
		}

		String expirationExpression = expirationOptional.get();

		String expirationString = expirationExpression.substring(expirationExpression.indexOf("(") + 1,
				expirationExpression.indexOf(")"));

		SpoilerAlertCalendar expirationDate = SpoilerAlertCalendar.getSpoilersCalendarFromDateString(expirationString);
		SpoilerAlertCalendar currentDate = new SpoilerAlertCalendar(world);

		long daysExpired = currentDate.getDay() - expirationDate.getDay();

		plugin.debugInfo((daysExpired > 0 ? "daysExpired: " : "daysUntilExpiration: ") + Math.abs(daysExpired));

		return daysExpired;
	}

}

/*
 * 
 * 
 * 
 */
