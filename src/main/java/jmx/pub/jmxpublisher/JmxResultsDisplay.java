package jmx.pub.jmxpublisher;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.ModelObject;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

public class JmxResultsDisplay implements ModelObject {

	private transient JMXPublisherBuildAction buildAction;
	private static AbstractBuild<?, ?> currentBuild = null;
	private Report currentReport;

	  /**
	   * Parses the reports and build a {@link BuildActionResultsDisplay}.
	   *
	   * @throws java.io.IOException If a report fails to parse.
	   */
	  JmxResultsDisplay(final JMXPublisherBuildAction buildAction, TaskListener listener)
	      throws IOException {
		  this.buildAction = buildAction;

		  currentReport = this.buildAction.getReport();
		  currentReport.setBuildAction(buildAction);
		  addPreviousBuildReportToExistingReport();
	  }

	  public String getDisplayName() {
	    return "Jmx Publisher Display";
	  }


	  public AbstractBuild<?, ?> getBuild() {
	    return buildAction.getBuild();
	  }


	  public Report getReport() {
		  return currentReport;
	  }

	  private void addPreviousBuildReportToExistingReport() {
		  if (JmxResultsDisplay.currentBuild == null) {
			  JmxResultsDisplay.currentBuild = getBuild();
		  } else {
			  if (JmxResultsDisplay.currentBuild != getBuild()) {
				  JmxResultsDisplay.currentBuild = null;
				  return;
			  }
		  }

		  AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
		  if (previousBuild == null)
			  return;

		  JMXPublisherBuildAction previousPerformanceAction = previousBuild.getAction(JMXPublisherBuildAction.class);
		  if (previousPerformanceAction == null)
			  return;

	    JmxResultsDisplay previousBuildActionResults = previousPerformanceAction.getBuildActionResultsDisplay();
	    if (previousBuildActionResults == null)
	    	return;

	    Report lastReport = previousBuildActionResults.getReport();
	    getReport().setLastBuildReport(lastReport);
	  }

	  /**
	   * Graph of metric points over time.
	   */
	  public void doSummarizerGraph(StaplerRequest request, StaplerResponse response) throws IOException {

		  final String metricName = request.getParameter("metricName");
		  final List<Statistic> statisticList = this.currentReport.getMetricByName(metricName);

	    Graph graph = new GraphImpl(metricName, "ms") {
	      protected DataSetBuilder<String, Long> createDataSet() {
	        DataSetBuilder<String, Long> dataSetBuilder = new DataSetBuilder<String, Long>();

	        for (Statistic value : statisticList) {
	          dataSetBuilder.add(value.getMetricValue(), metricName, value.getTimestamp());
	        }

	        return dataSetBuilder;
	      }
	    };

	    graph.doPng(request, response);
	  }


	  private abstract class GraphImpl extends Graph {
	    private final String graphTitle;
	    private final String xLabel;

	    protected GraphImpl(final String metricName, final String frequency) {
	      super(-1, 400, 300); // cannot use timestamp, since ranges may change
	      this.graphTitle = metricName;
	      this.xLabel = "Time in " + frequency;
	    }

	    protected abstract DataSetBuilder<String, Long> createDataSet();

	    protected JFreeChart createGraph() {
	      CategoryDataset dataset = createDataSet().build();

	      final JFreeChart chart = ChartFactory.createLineChart(graphTitle, // title
	          xLabel, // category axis label
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
