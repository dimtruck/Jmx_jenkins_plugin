package jmx.pub.jmxpublisher;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.util.ChartUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportContainer{

	public List<Report> getListOfPreviousReports(AbstractBuild<?, ?> build,
            final long currentTimestamp) {
		List<Report> previousReports = new ArrayList<Report>();
		List<? extends AbstractBuild<?, ?>> builds = build.getProject().getBuilds();

		for (AbstractBuild<?, ?> currentBuild : builds) {
			Report report = getReportForBuild(currentBuild);
			if (report != null && (report.getTimestamp() != currentTimestamp || builds.size() == 1))
				previousReports.add(report);
		}
		return previousReports;
	}

	public List<Report> getExistingReportsList(AbstractProject<?,?> project) {
		List<Report> reports = new ArrayList<Report>();
		if(project != null){
			List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();

			for (AbstractBuild<?, ?> currentBuild : builds) {
				Report report = getReportForBuild(currentBuild);
				if (report != null)
					reports.add(report);
			}
		}
		return reports;
	}

	public Map<ChartUtil.NumberOnlyBuildLabel, Double> getMapAverageForAllReports(List<Report> reports,String metricName) {
		Map<ChartUtil.NumberOnlyBuildLabel, Double> averages = new TreeMap<ChartUtil.NumberOnlyBuildLabel, Double>();
		for (Report report : reports) {
			double value = report.getAverageForMetric(metricName);
			if (value >= 0) {
				ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(report.getBuild());
				averages.put(label, value);
			}
		}
		return averages;
	}

	
	public double getAverageForAllReports(List<Report> reports, String metricName){
		if(reports != null && metricName != null){
			long calculatedSum = 0;
			if(reports.size() > 0){
				for (Report report : reports)
					calculatedSum += report.getAverageForMetric(metricName);
				
				return calculatedSum / reports.size();
			}			
			return calculatedSum;
		}
		return -1;
	}
	
	public double getAverageForPreviousReport(AbstractBuild<?, ?> build, String metricName){
		AbstractBuild<?,?> previousBuild = build.getPreviousBuild();
		if(previousBuild != null){
			Report report = getReportForBuild(previousBuild);
			return report.getAverageForMetric(metricName);
		} else
			return -1;
		
	}
	
	public Report getReportForBuild(AbstractBuild<?,?> currentBuild){
		if(currentBuild != null){
			JMXPublisherBuildAction performanceBuildAction = currentBuild.getAction(JMXPublisherBuildAction.class);
			if (performanceBuildAction != null)
				return performanceBuildAction.getBuildActionResultsDisplay().getReport();
		} 
		return null;
	}
}
