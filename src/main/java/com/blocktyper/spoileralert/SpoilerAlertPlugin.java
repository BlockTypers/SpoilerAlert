package com.blocktyper.spoileralert;

import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.entity.HumanEntity;

import com.blocktyper.spoileralert.commands.HungerCommand;
import com.blocktyper.spoileralert.commands.SetDayCommand;
import com.blocktyper.spoileralert.commands.SpoilDateCommand;
import com.blocktyper.spoileralert.listeners.CraftListener;
import com.blocktyper.spoileralert.listeners.FoodEatListener;
import com.blocktyper.spoileralert.listeners.FurnaceListener;
import com.blocktyper.spoileralert.listeners.InventoryClickListener;
import com.blocktyper.spoileralert.listeners.InventoryOpenListener;
import com.blocktyper.spoileralert.listeners.PickupListener;
import com.blocktyper.spoileralert.listeners.PlayerTradeListener;
import com.blocktyper.v1_16_5.BlockTyperBasePlugin;
import com.blocktyper.v1_16_5.config.BlockTyperConfig;
import com.blocktyper.v1_16_5.recipes.IRecipe;
import com.blocktyper.spoileralert.listeners.BlockBreakListener;
import com.blocktyper.spoileralert.listeners.BlockPlaceListener;
import com.blocktyper.spoileralert.listeners.CakeListener;

public class SpoilerAlertPlugin extends BlockTyperBasePlugin {
	
	public static final String RECIPES_KEY = "MAGIC_DOORS_RECIPE_KEY";
	
	public static final String NBT_DATE_FORMAT = "MM/dd/yyyy";
	
	public static final String DEFAULT_INTERNATIONAL_DATE_FORMAT = "d/M/y";

	public static BlockTyperConfig CONFIG;
	public static final String RESOURCE_NAME = "com.blocktyper.spoileralert.resources.SpoilerAlertMessages";
	
	public SpoilerAlertPlugin() {
		super();
		useOnPickupTranslationListener = false;
	}

	public void onEnable() {
		CONFIG = config();
		super.onEnable();
		registerListeners();
		new SpoilDateCommand(this);
		new SetDayCommand(this);
		new HungerCommand(this);

	}

	private void registerListeners() {
		new BlockBreakListener(this);
		new BlockPlaceListener(this);
		new CakeListener(this);
		new CraftListener(this);
		new FoodEatListener(this);
		new FurnaceListener(this);
		new InventoryClickListener(this);
		new PickupListener(this);
		new InventoryOpenListener(this);
		
		if(getConfig().getBoolean(ConfigKeyEnum.BLOCK_SPOILED_TRADES.getKey(), true)){
			new PlayerTradeListener(this);
		}
	}

	// begin localization
	@Override
	public ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle(RESOURCE_NAME, locale);
	}
	// end localization

	@Override
	public String getRecipesNbtKey() {
		return RECIPES_KEY;
	}
	
	public String getPlayerDateFormat(HumanEntity player){
		String playerLocale = getPlayerHelper().getLocale(player);
		
		String dateFormat = getConfig().getString(ConfigKeyEnum.DATE_FORMAT.getKey() + "." + playerLocale, null);
		
		if(dateFormat == null){
			String playerLanguage = getPlayerHelper().getLanguage(player);
			dateFormat = getConfig().getString(ConfigKeyEnum.DATE_FORMAT.getKey() + "." + playerLanguage, DEFAULT_INTERNATIONAL_DATE_FORMAT);
		}
		
		if(dateFormat == null || dateFormat.isEmpty()){
			dateFormat = DEFAULT_INTERNATIONAL_DATE_FORMAT;
		}
		
		return dateFormat;
	}

	@Override
	public IRecipe bootstrapRecipe(IRecipe recipe) {
		return recipe;
	}
}
