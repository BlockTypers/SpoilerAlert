package com.blocktyper.spoileralert.listeners;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.blocktyper.spoileralert.ConfigKeyEnum;
import com.blocktyper.spoileralert.LocalizedMessageEnum;
import com.blocktyper.spoileralert.PerishableBlock;
import com.blocktyper.spoileralert.PerishableBlockRepo;
import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public abstract class SpoilerAlertListenerBase implements Listener {

	public static final int DEFAULT_LIFE_SPAN_IN_DAYS = 7;
	public static final String DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS = "DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS";
	public static final String DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS_DIMENTION_MAP = "DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS_DIMENTION_MAP";

	protected static PerishableBlockRepo perishableBlockRepo;

	protected SpoilerAlertPlugin plugin;

	public SpoilerAlertListenerBase(SpoilerAlertPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		initPerishableBlockRepo();
	}

	/*
	 * SET EXPIRATION DATE
	 */
	protected ItemStack setExpirationDate(ItemStack itemStack, World world, Long daysExpired) {

		if (itemStack == null) {
			plugin.debugInfo("[setExpirationDate] itemStack == null");
			return itemStack;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();

		Optional<String> lifeSpan = null;

		if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getDisplayName() != null) {
			plugin.debugInfo("[setExpirationDate] checking recipe shelf-lives: "
					+ itemStack.getItemMeta().getDisplayName() + ")");
			lifeSpan = plugin.getConfig().getStringList(ConfigKeyEnum.RECIPE_LIFE_SPANS.getKey()).stream()
					.filter(l -> l.startsWith(itemStack.getItemMeta().getDisplayName())).findFirst();
		}

		if (lifeSpan == null || !lifeSpan.isPresent() || lifeSpan.get() == null) {
			plugin.debugInfo("[setExpirationDate] checking material shelf-lives: " + itemStack.getType().name() + ")");
			lifeSpan = plugin.getConfig().getStringList(ConfigKeyEnum.LIFE_SPANS.getKey()).stream()
					.filter(l -> l.startsWith(itemStack.getType().name())).findFirst();
		} else {
			plugin.debugInfo(
					"[setExpirationDate] using recipe shelf-life:  " + itemStack.getItemMeta().getDisplayName() + ")");
		}

		if (daysExpired == null && (lifeSpan == null || !lifeSpan.isPresent())) {
			plugin.debugInfo("[setExpirationDate] daysExpired == null && (lifeSpan == null || !lifeSpan.isPresent())");
			return itemStack;
		}

		String expirationDateText = "[" + plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRATION_DATE.getKey())
				+ "]";

		boolean addLore = true;

		List<String> lore = itemMeta.getLore();

		if (lore != null && !lore.isEmpty()) {
			plugin.debugInfo("[setExpirationDate] lore != null && !lore.isEmpty()");
			long existingLoreCount = lore.stream().filter(l -> l.contains(expirationDateText)).count();

			plugin.debugInfo("[setExpirationDate] existingLoreCount: " + existingLoreCount);
			addLore = existingLoreCount < 1;
			plugin.debugInfo("addLore: " + addLore);
		} else {
			plugin.debugInfo("[setExpirationDate] lore == null || lore.isEmpty()");
		}

		if (addLore) {

			int days = 0;

			if (daysExpired != null) {
				plugin.debugInfo("[setExpirationDate] USING MANUAL daysExpired");
				days = (daysExpired.intValue()) * -1;
			} else {

				plugin.debugInfo("[setExpirationDate] NOT USING MANUAL daysExpired");
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

	/*
	 * GET LIFESPAN
	 */
	protected int getLifeSpanIndays(Material material) {
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

	/*
	 * GET BUFF MAGNITUDE
	 */
	protected int getBuffMagnitude(long daysExpired, int lifeSpanInDays) {
		Long additionalMagnitude = (daysExpired / ((lifeSpanInDays < 4 ? 4 : lifeSpanInDays) / 4));
		int buffMagnitude = 1 + additionalMagnitude.intValue();
		return buffMagnitude;
	}

	/*
	 * GET DAYS EXPIRED
	 */
	protected long getDaysExpiredZeroOutNulls(ItemStack itemStack, World world) {
		Long daysExpired = getDaysExpired(itemStack, world);
		return daysExpired != null ? daysExpired : 0;
	}

	protected Long getDaysExpired(ItemStack itemStack, World world) {
		String expirationString = getExpirationDateText(itemStack);
		return getDaysExpired(expirationString, world);
	}

	protected Long getDaysExpired(String expirationString, World world) {

		if (expirationString == null) {
			return null;
		}

		SpoilerAlertCalendar expirationDate = SpoilerAlertCalendar.getSpoilersCalendarFromDateString(expirationString);
		SpoilerAlertCalendar currentDate = new SpoilerAlertCalendar(world);

		long daysExpired = currentDate.getDay() - (expirationDate.getDay() - 1);

		plugin.debugInfo((daysExpired > 0 ? "daysExpired: " : "daysUntilExpiration: ") + Math.abs(daysExpired));

		return daysExpired;
	}

	/*
	 * GET EXPIRATION DATE TEXT
	 */
	protected String getExpirationDateText(ItemStack itemStack) {
		if (itemStack == null) {
			return null;
		}

		ItemMeta meta = itemStack.getItemMeta();

		plugin.debugInfo("[getExpirationDateText] itemStack.getType().name(): " + itemStack.getType().name());

		if (meta.getLore() == null || meta.getLore().isEmpty()) {
			plugin.debugInfo("[getExpirationDateText] meta.getLore() == null || meta.getLore().isEmpty()");
			return null;
		}

		String expirationDateText = "[" + plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRATION_DATE.getKey())
				+ "]";

		Optional<String> expirationOptional = meta.getLore().stream().filter(l -> l.contains(expirationDateText))
				.findFirst();

		if (expirationOptional == null || !expirationOptional.isPresent()) {
			plugin.debugInfo("[[getExpirationDateText] expirationOptional == null || !expirationOptional.isPresent()");
			return null;
		}

		String expirationExpression = expirationOptional.get();

		String expirationString = expirationExpression.substring(expirationExpression.indexOf("(") + 1,
				expirationExpression.indexOf(")"));

		return expirationString;
	}

	protected void initPerishableBlockRepo() {
		if (perishableBlockRepo == null) {
			perishableBlockRepo = plugin.getTypeData(DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS,
					PerishableBlockRepo.class);

			if (perishableBlockRepo == null || perishableBlockRepo.getMap() == null) {
				perishableBlockRepo = new PerishableBlockRepo();
				perishableBlockRepo.setMap(new HashMap<String, PerishableBlock>());
				updatePerishableBlockRepo();
			}
		}
	}

	protected void updatePerishableBlockRepo() {
		plugin.setData(DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS, perishableBlockRepo, true);
	}

	protected PerishableBlock createPerishableBlock(Block block, SpoilerAlertCalendar expirationDate) {
		PerishableBlock perishableBlock = new PerishableBlock();
		perishableBlock
				.setId(block.getWorld().getName() + ":" + block.getX() + "," + block.getY() + "," + block.getZ());
		perishableBlock.setX(block.getX());
		perishableBlock.setY(block.getY());
		perishableBlock.setZ(block.getZ());
		perishableBlock.setExpirationDate(expirationDate != null ? expirationDate.getDisplayDate() : null);
		perishableBlock.setWorld(block.getWorld().getName());
		return perishableBlock;
	}

	protected SpoilerAlertCalendar getExistingExpirationDate(Block block) {
		initPerishableBlockRepo();
		SpoilerAlertCalendar expirationDate = null;

		PerishableBlock perishableBlock = createPerishableBlock(block, null);

		if (!perishableBlockRepo.getMap().containsKey(perishableBlock.getId())) {
			plugin.debugInfo(
					"[onBlockBreakEvent] !perishableBlockRepo.getMap().containsKey(" + perishableBlock.getId() + ")");
			return null;
		}

		perishableBlock = perishableBlockRepo.getMap().get(perishableBlock.getId());

		if (perishableBlock == null || perishableBlock.getExpirationDate() == null) {
			plugin.debugInfo(
					"[onBlockBreakEvent] perishableBlock == null || perishableBlock.getExpirationDate() == null");
			return null;
		}

		expirationDate = SpoilerAlertCalendar.getSpoilersCalendarFromDateString(perishableBlock.getExpirationDate());

		if (expirationDate == null) {
			plugin.debugInfo("[onBlockBreakEvent] spoilerAlertCalendar == null");
			return null;
		}

		return expirationDate;
	}

	protected void makePlayerSick(Long daysExpired, ItemStack item, Player player) {
		if (daysExpired == null || daysExpired < 1)
			return;

		int lifeSpanInDays = getLifeSpanIndays(item.getType());

		int buffMagnitude = getBuffMagnitude(daysExpired, lifeSpanInDays);

		Long buffDuration = plugin.getConfig().getLong(ConfigKeyEnum.BASE_DEBUFF_DURATION_IN_SECONDS.getKey(), 30);
		buffDuration = buffDuration * (1 + (daysExpired / lifeSpanInDays));

		plugin.debugInfo("[makePlayerSick] buffMagnitude: " + buffMagnitude);
		plugin.debugInfo("[makePlayerSick] buffDuration: " + buffDuration);

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.BLINDING_ENABLED.getKey(), true)) {
			player.addPotionEffect(
					new PotionEffect(PotionEffectType.BLINDNESS, buffDuration.intValue() * 20, buffMagnitude));
		}

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.CONFUSION_ENABLED.getKey(), true)) {
			player.addPotionEffect(
					new PotionEffect(PotionEffectType.CONFUSION, buffDuration.intValue() * 20, buffMagnitude));
		}

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.DECREASE_FOOD_LEVEL_ENABLED.getKey(), true)) {
			Double newFoodLevel = player.getFoodLevel() * (1.0 / buffMagnitude);
			newFoodLevel = newFoodLevel < 0 ? 0 : newFoodLevel;
			player.setFoodLevel(newFoodLevel.intValue());
		}

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.DECREASE_SATURATION_ENABLED.getKey(), true)) {
			if (player.getSaturation() > 0) {
				player.setSaturation(player.getSaturation() * (1.0f / buffMagnitude));
			}
		}

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.HARM_ENABLED.getKey(), true)) {
			Long ageBeforeHarmProportion = plugin.getConfig()
					.getLong(ConfigKeyEnum.AGE_BEFORE_HARM_AS_PROPORTION_OF_SHELF_LIFE.getKey(), 3);
			plugin.debugInfo("[makePlayerSick] ageBeforeHarmProportion: " + ageBeforeHarmProportion);
			plugin.debugInfo("[makePlayerSick] lifeSpanInDays*ageBeforeHarmProportion: "
					+ lifeSpanInDays * ageBeforeHarmProportion);
			if (lifeSpanInDays * ageBeforeHarmProportion <= daysExpired) {
				int harmMagnifier = buffMagnitude;
				if (ageBeforeHarmProportion <= buffMagnitude) {
					harmMagnifier = buffMagnitude / (ageBeforeHarmProportion.intValue());
				} else if (buffMagnitude > 1) {
					buffMagnitude = buffMagnitude / 2;
				}
				if (ageBeforeHarmProportion <= buffDuration) {
					buffDuration = buffDuration / (ageBeforeHarmProportion.intValue());
				} else if (buffDuration > 1) {
					buffDuration = buffDuration / 2;
				}
				player.addPotionEffect(
						new PotionEffect(PotionEffectType.HARM, buffDuration.intValue() * 20, harmMagnifier));
			}
		}
	}

	protected void sendExpiredMessage(ItemStack item, Player player) {
		Long daysExpired = getDaysExpired(item, player.getWorld());
		sendExpiredMessage(daysExpired, item.getType(), player);
	}

	protected void sendExpiredMessage(Long daysExpired, Material type, Player player) {
		if (daysExpired != null && daysExpired > 0) {
			int lifeSpanInDays = getLifeSpanIndays(type);
			int buffMagnitude = getBuffMagnitude(daysExpired, lifeSpanInDays);

			String expiredMessage = plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRED_MESSAGE.getKey(), player);
			expiredMessage = new MessageFormat(expiredMessage)
					.format(new Object[] { daysExpired + "", buffMagnitude + "" });

			player.sendMessage(ChatColor.RED + expiredMessage);
		}
	}
}
