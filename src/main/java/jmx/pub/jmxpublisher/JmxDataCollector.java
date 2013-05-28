package jmx.pub.jmxpublisher;

import java.io.PrintStream;

import hudson.model.AbstractBuild;

public class JmxDataCollector {
	AbstractBuild<?,?> build;
	PrintStream logger;
	JmxOutputWriterParser parser;
	
	public JmxDataCollector(JmxOutputWriterParser parser, AbstractBuild<?,?> build, PrintStream logger){
		this.build = build;
		this.logger = logger;	
		this.parser = parser;
	}
	
	public Report createReport(){
		long buildStartTime = build.getTimeInMillis();
		if(this.logger != null)
			this.logger.println("Create report in data collector");
		Report report = new Report(buildStartTime, this.logger);
		report.addData(this.parser.fetchJmxData(buildStartTime));
		return report;
	}

}
