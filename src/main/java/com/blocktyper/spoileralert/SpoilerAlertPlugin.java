package com.blocktyper.spoileralert;

import java.util.ResourceBundle;

import com.blocktyper.plugin.BlockTyperPlugin;
import com.blocktyper.spoileralert.commands.HungerCommand;
import com.blocktyper.spoileralert.commands.SetDayCommand;
import com.blocktyper.spoileralert.commands.SpoilDateCommand;
import com.blocktyper.spoileralert.listeners.CraftListener;
import com.blocktyper.spoileralert.listeners.FoodEatListener;
import com.blocktyper.spoileralert.listeners.FurnaceListener;
import com.blocktyper.spoileralert.listeners.InventoryClickListener;
import com.blocktyper.spoileralert.listeners.PickupListener;
import com.blocktyper.spoileralert.listeners.BlockBreakListener;
import com.blocktyper.spoileralert.listeners.BlockPlaceListener;
import com.blocktyper.spoileralert.listeners.CakeListener;

public class SpoilerAlertPlugin extends BlockTyperPlugin {

	public static final String RESOURCE_NAME = "com.blocktyper.spoileralert.resources.SpoilerAlertMessages";
	
	public SpoilerAlertPlugin() {
		super();
	}

	public void onEnable() {
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
	}

	// begin localization
	private ResourceBundle bundle = null;

	public ResourceBundle getBundle() {
		if (bundle == null)
			bundle = ResourceBundle.getBundle(RESOURCE_NAME, locale);
		return bundle;
	}

	// end localization
}
