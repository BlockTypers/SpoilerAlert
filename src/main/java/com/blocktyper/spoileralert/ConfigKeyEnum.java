package com.blocktyper.spoileralert;

public enum ConfigKeyEnum {
	
	RECIPE_LIFE_SPANS("spoileralert-recipe-shelf-life"),
	LIFE_SPANS("spoileralert-shelf-life"),
	BASE_DEBUFF_DURATION_IN_SECONDS("spoileralert-base-debuf-duration-in-seconds"),
	BLINDING_ENABLED("spoileralert-blinding-enabled"),
	CONFUSION_ENABLED("spoileralert-confusion-enabled"),
	HARM_ENABLED("spoileralert-harm-enabled"),
	AGE_BEFORE_HARM_AS_PROPORTION_OF_SHELF_LIFE("spoileralert-age-before-harm-as-proportion-of-shelf-life"),
	DECREASE_FOOD_LEVEL_ENABLED("spoileralert-decrease-food-level-enabled"),
	DECREASE_SATURATION_ENABLED("spoileralert-decrease-saturation-enabled"),
	SET_EXPIRATION_ON_INVENTORY_CLICK("spoileralert-set-expiration-on-inventory-click"),
	DATE_FORMAT("spoileralert-date-format"),
	USE_REAL_DATES("spoileralert-use-real-dates");


	private String key;

	private ConfigKeyEnum(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
