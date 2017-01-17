package com.blocktyper.spoileralert;

public enum LocalizedMessageEnum {
	
	EXPIRED_MESSAGE("spoileralert-expired-message"),
	EXPIRATION_DATE("spoileralert-expiration-date"),
	CAKE_HAS_NO_EXPIRATION_DATE("spoileralert-cake-has-no-expiration-date"),
	HIT_CAKES_FIRST("spoileralert-hit-cakes-first"),
	CAKE_NOT_EXPIRED("spoileralert-cake-not-expired"),
	DATE("spoileralert-date");


	private String key;

	private LocalizedMessageEnum(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
