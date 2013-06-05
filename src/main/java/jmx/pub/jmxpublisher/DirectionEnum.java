package jmx.pub.jmxpublisher;

public enum DirectionEnum {
	INCREASE("Increased"), 
	DECREASE("Decreases"),
	NO_CHANGE("");

	private final String description;

	DirectionEnum(String description) {
		this.description = description;
	}

	public String description() {
		return this.description;
	}
}
