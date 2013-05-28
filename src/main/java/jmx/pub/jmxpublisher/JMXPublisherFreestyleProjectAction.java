package jmx.pub.jmxpublisher;

import hudson.model.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.remoting.jnlp.MainMenu;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

public class JMXPublisherFreestyleProjectAction implements Action {
   private AbstractProject<?,?> project;
   private String metricName;
   private ReportContainer reports;
   private List<String> metricNames;
   
   public String getIconFileName() {
      return "graph.gif";
   }
   
   public List<String> getAvailableMetricNames(){
	if(project != null){
		Report report = reports.getReportForBuild(project.getLastBuild());
		if(report != null){
			List<String> metricNameList = new ArrayList<String>();
			Set<String> metricNames = report.getMetricNames();
			for(String metricName: metricNames)
				metricNameList.add(metricName);
			if(metricNameList.size() > 0 && this.metricName.equals("None"))
				this.metricName = metricNameList.get(0);
			return metricNameList;
		}
	}
   	return this.metricNames;
   }

   public String getDisplayName() {
      return "JMX Publisher Project View";
   }

   public String getUrlName() {
      return "JMXPublisher";
   }

   public JMXPublisherFreestyleProjectAction(AbstractProject<?,?> project, 
		   String mainMetricName, List<String> metricNameList, ReportContainer reports) {
	   System.out.println("in JMXPublisherFreestyleProjectAction");
	   System.out.println(mainMetricName);
	   this.project = project;
	   this.metricName = mainMetricName;
	   this.reports = reports;
	   this.metricNames = metricNameList;
   }

   /**
    * Get the project
    * @return project object
    */
   public AbstractProject<?,?> getProject() {
	   return this.project;
   }


   /**
    * Graph of metric points over time.
    */
   public void doSummarizerGraphMainMetric(final StaplerRequest request,
                                           final StaplerResponse response) throws IOException {
	   System.out.println("in JMXPublisherFreestyleProjectAction.doSummarizerGraphMainMetric");
	   System.out.println(this.metricName);
	   final Map<ChartUtil.NumberOnlyBuildLabel, Double> averagesFromReports =
    		 reports.getMapAverageForAllReports(reports.getExistingReportsList(getProject()), this.metricName);
	   System.out.println(averagesFromReports.size());
	   createGraph(this.metricName, averagesFromReports,request,response);
   }

   /**
    * Graph of metric points over time, metric to plot set as request parameter.
    */
   public void doSummarizerGraphForMetric(final StaplerRequest request,
                                           final StaplerResponse response) throws IOException {
	   System.out.println("in JMXPublisherFreestyleProjectAction.doSummarizerGraphForMetric");
	   System.out.println(this.metricName);
	    final String metricName = request.getParameter("metricName");
	    final Map<ChartUtil.NumberOnlyBuildLabel, Double> averagesFromReports =
	    		reports.getMapAverageForAllReports(reports.getExistingReportsList(getProject()), metricName);
		   System.out.println(averagesFromReports.size());
	    createGraph(metricName, averagesFromReports,request,response);

   }
   
	private void createGraph(final String metricName, 
			final Map<ChartUtil.NumberOnlyBuildLabel, Double> averagesFromReports,
			final StaplerRequest request,
            final StaplerResponse response) throws IOException{
		final Graph graph = new GraphImpl(metricName + " Overall Graph") {

			protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet() {
				DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
						new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

	           for (ChartUtil.NumberOnlyBuildLabel label : averagesFromReports.keySet()) {
	             dataSetBuilder.add(averagesFromReports.get(label), metricName, label);
	           }

	           return dataSetBuilder;
	         }
	       };

	       graph.doPng(request, response);
   }

   private abstract class GraphImpl extends Graph {
     private final String graphTitle;

     protected GraphImpl(final String metricKey) {
       super(-1, 400, 300); // cannot use timestamp, since ranges may change
       this.graphTitle = stripTitle(metricKey);
     }

     private String stripTitle(final String metricKey) {
       return metricKey.substring(metricKey.lastIndexOf("|") + 1);
     }

     protected abstract DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet();

     protected JFreeChart createGraph() {
       final CategoryDataset dataset = createDataSet().build();

       final JFreeChart chart = ChartFactory.createLineChart(graphTitle, // title
           "Build Number #", // category axis label
           null, // value axis label
           dataset, // data
           PlotOrientation.VERTICAL, // orientation
           false, // include legend
           true, // tooltips
           false // urls
       );

       chart.setBackgroundPaint(Color.white);

       return chart;
     }
   }



}
