package jmx.pub.jmxpublisher;

import hudson.model.Action;
import hudson.model.*;
import hudson.util.StreamTaskListener;
import org.kohsuke.stapler.StaplerProxy;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;

public class JMXPublisherBuildAction implements Action, StaplerProxy {

	private final AbstractBuild<?, ?> build;
	private Report report;
	private transient WeakReference<JmxResultsDisplay> resultsDisplay;
	private PrintStream logger;

	public JMXPublisherBuildAction(AbstractBuild<?, ?> build, Report report, PrintStream logger) {
		this.build = build;
		this.report = report;
		this.logger = logger;
	}
   
	public final AbstractBuild<?, ?> getOwner() {
		return build;
	}
   
   public final AbstractBuild<?, ?> getBuild() {
       return build;
   }

   public String getIconFileName() {
      return "graph.gif";
   }

   public String getDisplayName() {
      return "JMX Publisher Build View";
   }

   public String getUrlName() {
      return "JMXPublisher";
   }
 	
	public JmxResultsDisplay getTarget() {
    		return getBuildActionResultsDisplay();
  	}

	public Report getReport(){
      return report;
	}

	public JmxResultsDisplay getBuildActionResultsDisplay() {
	   JmxResultsDisplay buildDisplay = null;
	   WeakReference<JmxResultsDisplay> wr = this.resultsDisplay;
	   if (wr != null) {
		   buildDisplay = wr.get();
		   if (buildDisplay != null)
			   return buildDisplay;
	   }

	   try {
		   buildDisplay = new JmxResultsDisplay(this, StreamTaskListener.fromStdout());
	   } catch (IOException e) {
	    	logger.println("Could not created the display");
	   }
	   
	   this.resultsDisplay = new WeakReference<JmxResultsDisplay>(buildDisplay);
	   return buildDisplay;
	}

	public void setBuildActionResultsDisplay(WeakReference<JmxResultsDisplay> resultsDisplay) {
	    this.resultsDisplay = resultsDisplay;
	}
}
