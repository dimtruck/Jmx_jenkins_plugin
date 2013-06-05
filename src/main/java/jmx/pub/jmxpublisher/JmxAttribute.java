package jmx.pub.jmxpublisher;

public class JmxAttribute {
	private String name;
	private Float percent;
	private DirectionEnum thresholdDirection;

	public JmxAttribute(){}

	public JmxAttribute(String name, Float percent, String thresholdDirection){
		this.name = name;
		this.percent = percent;
		this.thresholdDirection = DirectionEnum.valueOf(thresholdDirection.toUpperCase());
	}

	public String getThresholdDirection(){
		return thresholdDirection.description();
	}

	public void setThresholdDirection(String thresholdDirection){
		System.out.println(thresholdDirection);
		this.thresholdDirection = DirectionEnum.valueOf(thresholdDirection.toUpperCase());
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		System.out.println(name);
		this.name = name;
	}
	public Float getPercent() {
		return percent;
	}
	public void setPercent(Float percent) {
		System.out.println(percent);
		this.percent = percent;
	}
}
