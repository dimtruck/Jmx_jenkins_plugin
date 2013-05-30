package jmx.pub.jmxpublisher;

import java.io.Serializable;

public class Statistic implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2548315722354424214L;
	private float metricValue;
	private long timestamp;

	 public Statistic(float metricValue, long timestamp) {
	     this.metricValue = metricValue;
	     this.timestamp = timestamp;
	 }

	/**
	 * @return the metricValue
	 */
	public float getMetricValue() {
		return metricValue;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
}

