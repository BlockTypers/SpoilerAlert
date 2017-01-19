package com.blocktyper.spoileralert.listeners;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.blocktyper.helpers.InvisibleLoreHelper;
import com.blocktyper.nbt.NBTItem;
import com.blocktyper.spoileralert.ConfigKeyEnum;
import com.blocktyper.spoileralert.LocalizedMessageEnum;
import com.blocktyper.spoileralert.PerishableBlock;
import com.blocktyper.spoileralert.PerishableBlockRepo;
import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public abstract class SpoilerAlertListenerBase implements Listener {

	public static final int DEFAULT_LIFE_SPAN_IN_DAYS = 7;
	public static final String DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS = "DATA_KEY_SPOILER_ALERT_PERISHABLE_BLOCKS";

	public static final String NBT_SPOILER_ALERT_EXPIRATION_DATE = "SPOILER_ALERT_EXPIRATION_DATE";
	public static final String INVISIBLE_PREFIX_SPOILER_ALERT_EXPIRATION_DATE = "SP_EXP#";

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

	protected ItemStack setExpirationDate(ItemStack itemStack, World world, Long daysExpired, HumanEntity player) {

		if (itemStack == null) {
			return itemStack;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();

		Optional<String> lifeSpan = null;

		NBTItem nbtItemForExistingCheck = new NBTItem(itemStack);

		if (nbtItemForExistingCheck.hasKey(SpoilerAlertPlugin.RECIPES_KEY)) {
			lifeSpan = plugin.getConfig().getStringList(ConfigKeyEnum.RECIPE_LIFE_SPANS.getKey()).stream()
					.filter(l -> l.contains(nbtItemForExistingCheck.getString(SpoilerAlertPlugin.RECIPES_KEY)))
					.findFirst();
		}

		if (lifeSpan == null || !lifeSpan.isPresent() || lifeSpan.get() == null) {
			lifeSpan = plugin.getConfig().getStringList(ConfigKeyEnum.LIFE_SPANS.getKey()).stream()
					.filter(l -> l.startsWith(itemStack.getType().name())).findFirst();
		}

		if (daysExpired == null && (lifeSpan == null || !lifeSpan.isPresent())) {
			return itemStack;
		}

		List<String> lore = itemMeta.getLore();

		if (!nbtItemForExistingCheck.hasKey(NBT_SPOILER_ALERT_EXPIRATION_DATE)) {

			// strip legacy lore
			if (lore != null && !lore.isEmpty()) {
				String legacyExpirationDateText = "["
						+ plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRATION_DATE.getKey()) + "]";
				long existingLoreCount = lore.stream().filter(l -> l.contains(legacyExpirationDateText)).count();

				if (existingLoreCount > 0) {
					lore = lore.stream().filter(l -> !l.contains(legacyExpirationDateText))
							.collect(Collectors.toList());
					itemMeta.setLore(lore);
					itemStack.setItemMeta(itemMeta);
				}
			}

			return getItemWithNbtTagExpirationDate(player, itemStack, itemMeta, lore, daysExpired, lifeSpan.get());
		}

		// Language conversion
		if (lore != null && !lore.isEmpty()) {
			lore = lore.stream().filter(l -> !loreLineContainsInvisExpirationDatePrefix(l))
					.collect(Collectors.toList());
		}
		if (lore == null)
			lore = new ArrayList<>();

		try {
			String expirationDateNbtString = nbtItemForExistingCheck.getString(NBT_SPOILER_ALERT_EXPIRATION_DATE);
			Date existingExpirationDate = getDateFromNbtString(expirationDateNbtString);
			String formattedExpirationDate = getStringfromDate(existingExpirationDate, player);
			daysExpired = getDaysExpired(expirationDateNbtString, world);
			lore.add(getExpirationDateLoreLine(player, (daysExpired == null || daysExpired < 1 ? "" : ChatColor.RED) + formattedExpirationDate));
			itemMeta.setLore(lore);
			itemStack.setItemMeta(itemMeta);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return itemStack;
	}

	private boolean loreLineContainsInvisExpirationDatePrefix(String loreLine) {
		return loreLine != null && InvisibleLoreHelper.convertToVisibleString(loreLine)
				.contains(INVISIBLE_PREFIX_SPOILER_ALERT_EXPIRATION_DATE);
	}

	private ItemStack getItemWithNbtTagExpirationDate(HumanEntity player, ItemStack itemStack, ItemMeta itemMeta,
			List<String> lore, Long daysExpired, String lifeSpanExpression) {

		int days = 0;

		if (daysExpired != null) {
			days = (daysExpired.intValue()) * -1;
		} else {
			String daysString = lifeSpanExpression.substring(lifeSpanExpression.indexOf("=") + 1);
			days = Integer.parseInt(daysString);
		}

		String expDateAsNbtString = "";
		String formattedDateString = "";

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.USE_REAL_DATES.getKey(), false)) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_YEAR, days);
			Date calDate = cal.getTime();
			expDateAsNbtString = getNbtStringfromDate(calDate);
			formattedDateString = getStringfromDate(calDate, player);
		} else {
			SpoilerAlertCalendar expirationDate = new SpoilerAlertCalendar(player.getWorld());
			expirationDate.addDays(days);
			expDateAsNbtString = expirationDate.getNbtDateString();
			formattedDateString = expirationDate.getDateString(player, plugin);
		}

		if (lore == null)
			lore = new ArrayList<>();

		lore.add(getExpirationDateLoreLine(player, (daysExpired == null || daysExpired < 1 ? "" : ChatColor.RED) + formattedDateString));
		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);

		NBTItem nbtItem = new NBTItem(itemStack);
		nbtItem.setString(NBT_SPOILER_ALERT_EXPIRATION_DATE, expDateAsNbtString);
		return nbtItem.getItem();
	}

	private String getExpirationDateLoreLine(HumanEntity player, String formattedDateString) {
		String invis = InvisibleLoreHelper.convertToInvisibleString(INVISIBLE_PREFIX_SPOILER_ALERT_EXPIRATION_DATE);
		String expirationDateText = plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRATION_DATE.getKey(), player);
		String loreFormat = "{0}{1}: {2}";
		String loreLine = new MessageFormat(loreFormat)
				.format(new Object[] { invis, expirationDateText, formattedDateString + ChatColor.RESET });
		return loreLine;
	}

	/*
	 * GET LIFESPAN
	 */
	protected int getLifeSpanIndays(Material material, HumanEntity player) {
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
	protected long getDaysExpiredZeroOutNulls(ItemStack itemStack, World world, HumanEntity player) {
		Long daysExpired = getDaysExpired(itemStack, world, player);
		return daysExpired != null ? daysExpired : 0;
	}

	protected Long getDaysExpired(ItemStack itemStack, World world, HumanEntity player) {
		String expirationString = getExpirationDateText(itemStack, player);
		return getDaysExpired(expirationString, world);
	}

	private Calendar getRoundedCal(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);

		cal.clear();
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.YEAR, year);
		return cal;
	}

	private Date getDateFromNbtString(String dateString) throws ParseException {
		String dateFormat = SpoilerAlertPlugin.NBT_DATE_FORMAT;
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date date = sdf.parse(dateString);
		return date;
	}

	private String getNbtStringfromDate(Date date) {
		String dateFormat = SpoilerAlertPlugin.NBT_DATE_FORMAT;
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		String dateString = sdf.format(date);
		return dateString;
	}

	private String getStringfromDate(Date date, HumanEntity player) {
		String dateFormat = plugin.getPlayerDateFormat(player);
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		String dateString = sdf.format(date);
		return dateString;
	}

	public int daysBetween(Date d1, Date d2) {
		return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}

	protected Long getDaysExpired(String expirationString, World world) {

		if (expirationString == null || expirationString.isEmpty()) {
			return null;
		}

		long daysExpired = 0;

		if (plugin.getConfig().getBoolean(ConfigKeyEnum.USE_REAL_DATES.getKey(), false)) {
			try {
				Date expDate = getDateFromNbtString(expirationString);
				Calendar expCal = getRoundedCal(expDate);
				Calendar nowCal = getRoundedCal(new Date());
				daysExpired = daysBetween(expCal.getTime(), nowCal.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			SpoilerAlertCalendar expirationDate = SpoilerAlertCalendar
					.getSpoilersCalendarFromDateString(expirationString);
			if (expirationDate == null)
				return null;

			SpoilerAlertCalendar currentDate = new SpoilerAlertCalendar(world);

			daysExpired = currentDate.getDay() - (expirationDate.getDay() - 1);
		}

		return daysExpired;
	}

	/*
	 * GET EXPIRATION DATE TEXT
	 */
	protected String getExpirationDateText(ItemStack itemStack, HumanEntity player) {
		if (itemStack == null) {
			return null;
		}

		NBTItem nbtItem = new NBTItem(itemStack);
		return nbtItem.getString(NBT_SPOILER_ALERT_EXPIRATION_DATE);
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
		perishableBlock.setExpirationDate(expirationDate != null ? expirationDate.getNbtDateString() : null);
		perishableBlock.setWorld(block.getWorld().getName());
		return perishableBlock;
	}

	protected SpoilerAlertCalendar getExistingExpirationDate(Block block) {
		initPerishableBlockRepo();
		SpoilerAlertCalendar expirationDate = null;

		PerishableBlock perishableBlock = createPerishableBlock(block, null);

		if (!perishableBlockRepo.getMap().containsKey(perishableBlock.getId())) {
			return null;
		}

		perishableBlock = perishableBlockRepo.getMap().get(perishableBlock.getId());

		if (perishableBlock == null || perishableBlock.getExpirationDate() == null) {
			return null;
		}

		expirationDate = SpoilerAlertCalendar.getSpoilersCalendarFromDateString(perishableBlock.getExpirationDate());

		if (expirationDate == null) {
			return null;
		}

		return expirationDate;
	}

	protected void makePlayerSick(Long daysExpired, ItemStack item, Player player) {
		if (daysExpired == null || daysExpired < 1)
			return;

		int lifeSpanInDays = getLifeSpanIndays(item.getType(), player);

		int buffMagnitude = getBuffMagnitude(daysExpired, lifeSpanInDays);

		Long buffDuration = plugin.getConfig().getLong(ConfigKeyEnum.BASE_DEBUFF_DURATION_IN_SECONDS.getKey(), 30);
		buffDuration = buffDuration * (1 + (daysExpired / lifeSpanInDays));

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
		Long daysExpired = getDaysExpired(item, player.getWorld(), player);
		sendExpiredMessage(daysExpired, item.getType(), player);
	}

	protected void sendExpiredMessage(Long daysExpired, Material type, Player player) {
		if (daysExpired != null && daysExpired > 0) {
			int lifeSpanInDays = getLifeSpanIndays(type, player);
			int buffMagnitude = getBuffMagnitude(daysExpired, lifeSpanInDays);

			String expiredMessage = plugin.getLocalizedMessage(LocalizedMessageEnum.EXPIRED_MESSAGE.getKey(), player);
			expiredMessage = new MessageFormat(expiredMessage)
					.format(new Object[] { daysExpired + "", buffMagnitude + "" });

			player.sendMessage(ChatColor.RED + expiredMessage);
		}
	}
}
