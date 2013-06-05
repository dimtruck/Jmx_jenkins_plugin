package jmx.pub.jmxpublisher;

public enum DirectionEnum {
	INCREASE("Increase"), 
	DECREASE("Decrease");

	private final String description;

	DirectionEnum(String description) {
		this.description = description;
	}

	public String description() {
		return this.description;
	}
}
