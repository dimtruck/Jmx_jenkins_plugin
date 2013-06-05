package jmx.pub.jmxpublisher;

public class JmxAttribute {
	private String name;
	private Float percent;
	private DirectionEnum thresholdDirection;

	public JmxAttribute(){}

	public JmxAttribute(String name, Float percent, DirectionEnum thresholdDirection){
		System.out.println("in jmx attribute");
		System.out.println(name);
		System.out.println(percent);
		System.out.println(thresholdDirection);
		this.name = name;
		this.percent = percent;
		this.thresholdDirection = thresholdDirection;
	}

	public DirectionEnum getThresholdDirection(){
		return thresholdDirection;
	}

	public void setThresholdDirection(DirectionEnum thresholdDirection){
		this.thresholdDirection = thresholdDirection;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Float getPercent() {
		return percent;
	}
	public void setPercent(Float percent) {
		this.percent = percent;
	}
}
