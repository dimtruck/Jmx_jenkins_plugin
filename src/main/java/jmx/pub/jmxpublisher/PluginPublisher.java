package jmx.pub.jmxpublisher;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.PrintStream;

import hudson.model.*;
import java.io.IOException;

public class PluginPublisher extends Recorder {

    private String jmxPath;
    private boolean isThresholdUsed;
    private PrintStream logger;    
    private JmxAttribute[] attributes;
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    	public DescriptorImpl(){
    		super(PluginPublisher.class);
    	}

        public String getDisplayName() {
            return "JMX Publisher plugin";
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
        
        public FormValidation doValidateMetricsConfiguration(@QueryParameter String jmxPath){
            if(jmxPath != null)
                return FormValidation.ok("Success");
            else
                return FormValidation.error("Please set the jmx path");
        }

	public DirectionEnum[] getThresholdDirection() {
        	    return DirectionEnum.values();
    	}
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /***
     * Takes in jmx path, which will point to the file where jmx data was uploaded from remote server
     * @param jmxPath - path
     */
    @DataBoundConstructor
    public PluginPublisher(String jmxPath, String isThresholdUsed, JmxAttribute[] attributes) {
        this.jmxPath = jmxPath;
        this.isThresholdUsed = Boolean.getBoolean(isThresholdUsed);
        this.attributes = attributes;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?,?> project) {
    	if(project != null)
    		if(project.getBuilds().size() > 0){
    			ReportContainer container = new ReportContainer();
    			Report report = container.getReportForBuild(project.getLastSuccessfulBuild());
    			if(report != null){
    				Set<String> metricNameSet = report.getMetricNames();
    				List<String> metricNameList = new ArrayList<String>();
    				for(String metricName: metricNameSet)
    					metricNameList.add(metricName);
    				String metricName = (String)metricNameList.toArray()[0];
    				return new JMXPublisherFreestyleProjectAction(project, metricName, metricNameList, container);
    			} else{
        			return new JMXPublisherFreestyleProjectAction(project, "None", new ArrayList<String>(), container);
        		}
    		} else{
    			ReportContainer container = new ReportContainer();
    			return new JMXPublisherFreestyleProjectAction(project, "None", new ArrayList<String>(), container);
    		}
    	return null;
    }

    public BuildStepMonitor getRequiredMonitorService() {
    	return BuildStepMonitor.NONE;
    }

    public String getJmxPath() {
    	return jmxPath;
    }

    public boolean getIsThresholdUsed(){
	return isThresholdUsed;
    }

    public JmxAttribute[] getAttributes(){
	return attributes;
    }
    
    /***
     * loads the logger and adds the build action to the build.
     * @param build - running build
     * @param launcher
     * @param listener - for logging purposes
     */
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) 
    		throws InterruptedException, IOException {
    	PrintStream logger = listener.getLogger();
    	logger.println("get results from build for file: " + this.jmxPath);
	String directoryPath = build.getWorkspace().getRemote();
	logger.println("the workspace directory is " + directoryPath);
    	JmxOutputWriterParser parser = new JmxOutputWriterParser(directoryPath + File.separator + this.jmxPath, logger);
    	if(!parser.isJmxPathValid()){
        	logger.println("jmx path is invalid.");
    		if(build.getResult().isBetterOrEqualTo(hudson.model.Result.UNSTABLE))
    			build.setResult(hudson.model.Result.FAILURE);
    		return true;
    	}
    	logger.println("collect results");
    	JmxDataCollector dataCollector = new JmxDataCollector(parser, build, logger);
    	Report report = dataCollector.createReport();
    	
    	JMXPublisherBuildAction buildAction = new JMXPublisherBuildAction(build, report, logger);
    	build.addAction(buildAction);
    	
    	logger.println("Report retrieved.  Now to validate if performance changed.  " +
    			"This will take into account current average, previous build average and overall average.");
    	ReportContainer reports = new ReportContainer();
    	List<Report> previousReports = reports.getListOfPreviousReports(build, report.getTimestamp());
    	logger.println("Number of reports: " + previousReports.size());
    	
    	logger.println("Iterate through all metrics in question.");
    	Set<String> metricNameSet = report.getMetricNames();

	//here, check if threshold has been set.  
	//If it has been, then verify whether the metric name exists in the threshold list.  
	//If it does, check if the threshold set is > % diff between changes

	if(getIsThresholdUsed()){
		List<JmxAttribute> metricsWithThreshold = new ArrayList<JmxAttribute>();
		for(JmxAttribute thresholdAttribute : getAttributes()){
			logger.println(thresholdAttribute.getName());
			logger.println(thresholdAttribute.getPercent());
			logger.println(thresholdAttribute.getThresholdDirection());
			for(String metricName : metricNameSet)
				if(metricName.toUpperCase().contains(thresholdAttribute.getName().toUpperCase()))
					metricsWithThreshold.add(new JmxAttribute(metricName,thresholdAttribute.getPercent(),thresholdAttribute.getThresholdDirection()));
	    	for(JmxAttribute attribute : metricsWithThreshold){
	    	    	double totalAverage = reports.getAverageForAllReports(previousReports, attribute.getName());
        		double currentAverage = report.getAverageForMetric(attribute.getName());
        		double previousAverage = reports.getAverageForPreviousReport(build, attribute.getName());
        	
        		if(currentAverage > previousAverage && build.getPreviousBuild() != null){
        			build.setResult(hudson.model.Result.FAILURE);
        			logger.println(attribute.getName() + " got worse!");
        		}
    		}
		}
	}

    	
    	return true;
    }
}
