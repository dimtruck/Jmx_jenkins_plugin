package jmx.pub.jmxpublisher;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
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
    private PrintStream logger;    
    
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

    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /***
     * Takes in jmx path, which will point to the file where jmx data was uploaded from remote server
     * @param jmxPath - path
     */
    @DataBoundConstructor
    public PluginPublisher(String jmxPath) {
        this.jmxPath = jmxPath;
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
    	logger.println("get results from build");
    	JmxOutputWriterParser parser = new JmxOutputWriterParser(this.jmxPath, logger);
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
    	for(String metricName : metricNameSet){
        	double totalAverage = reports.getAverageForAllReports(previousReports, metricName);
        	double currentAverage = report.getAverageForMetric(metricName);
        	double previousAverage = reports.getAverageForPreviousReport(build, metricName);
        	
        	if(currentAverage > previousAverage && build.getPreviousBuild() != null){
        		//build.setResult(hudson.model.Result.FAILURE);
        		logger.println(metricName + " got worse!");
        	}
    	}
    	
    	return true;
    }
}
