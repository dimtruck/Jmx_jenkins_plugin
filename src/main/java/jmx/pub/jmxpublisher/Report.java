package jmx.pub.jmxpublisher;

import hudson.model.AbstractBuild;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Report {
	private long timestamp;
	private PrintStream logger;
	private Map<String,List<Statistic>> metricData = new HashMap<String,List<Statistic>>();
	private Report lastBuildReport;
	private JMXPublisherBuildAction buildAction;
	
	public Report(long timestamp, PrintStream logger){
		this.timestamp = timestamp;
		this.logger = logger;
	}
	
	public void addData(Map<String,List<Statistic>> metricData){
		this.metricData = metricData;
	}

	public Map<String,List<Statistic>> getMetricData(){
		return this.metricData;
	}
	
	public List<Statistic> getMetricByName(String metricName){
		if(metricData != null && metricName != null && this.metricData.containsKey(metricName))
			return this.metricData.get(metricName);
		return new ArrayList<Statistic>();
	}
	
	public double getAverageForMetric(String metricName){
		if(metricData != null && metricName != null && this.metricData.containsKey(metricName)){
			List<Statistic> statisticList = this.metricData.get(metricName);
			float calculatedSum = 0;
			if(statisticList.size() > 0){
				for(Statistic statistic: statisticList)
					calculatedSum+=statistic.getMetricValue();
				
				return calculatedSum / statisticList.size();
			}
			return calculatedSum;
		}
		return -1;
	}

	public float getMaxForMetric(String metricName){
		if(metricData != null && metricName != null && this.metricData.containsKey(metricName)){
			List<Statistic> statisticList = this.metricData.get(metricName);
			float max = 0;
			if(statisticList.size() > 0){
				for(Statistic statistic: statisticList)
					max = statistic.getMetricValue() > max ? statistic.getMetricValue(): max;
				
				return max;
			}
			return max;
		}
		return -1;
	}

	public float getMinForMetric(String metricName){
		if(metricData != null && metricName != null && this.metricData.containsKey(metricName)){
			List<Statistic> statisticList = this.metricData.get(metricName);
			float min = 0;
			if(statisticList.size() > 0){
				for(Statistic statistic: statisticList)
					min = statistic.getMetricValue() < min ? statistic.getMetricValue(): min;
				
				return min;
			}
			return min;
		}
		return -1;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public AbstractBuild<?, ?> getBuild() {
		return buildAction.getBuild();
	}

	public JMXPublisherBuildAction getBuildAction() {
	    return buildAction;
	}

	public void setBuildAction(JMXPublisherBuildAction buildAction) {
	    this.buildAction = buildAction;
	}

	public void setLastBuildReport(Report lastBuildReport) {
	    this.lastBuildReport = lastBuildReport;
	}
	
	public Set<String> getMetricNames(){
		if(this.metricData != null)
			return this.metricData.keySet();
		else
			return new HashSet<String>();
	}
	
}

