package com.blocktyper.spoileralert;

import java.util.ResourceBundle;

import com.blocktyper.plugin.BlockTyperPlugin;

public class SpoilerAlertPlugin extends BlockTyperPlugin {

	

	public static final String RESOURCE_NAME = "com.blocktyper.spoileralert.resources.SpoilerAlertMessages";
	

	public void onEnable() {
		super.onEnable();
		new ExpirationListener(this);
		new SpoilDateCommand(this);
		
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
