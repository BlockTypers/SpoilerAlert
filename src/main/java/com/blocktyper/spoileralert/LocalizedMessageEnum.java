package com.blocktyper.spoileralert;

public enum LocalizedMessageEnum {
	
	PERISHABLE("spoileralert.perishable"),
	CREATED_DATE("spoileralert.created.date"),
	EXPIRATION_DATE("spoileralert.expiration.date");


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
