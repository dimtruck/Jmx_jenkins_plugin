package jmx.pub.jmxpublisher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Will parse jmx tab delimited data
 * Example:
 * localhost_9999.sun_management_MemoryImpl.HeapMemoryUsage_committed      291110912       1368811502588
 * localhost_9999.sun_management_MemoryImpl.HeapMemoryUsage_init   32692928        1368811502588
 * localhost_9999.sun_management_MemoryImpl.HeapMemoryUsage_max    620756992       1368811502588
 * localhost_9999.sun_management_MemoryImpl.HeapMemoryUsage_used   118949192       1368811502588
 * localhost_9999.sun_management_MemoryImpl.NonHeapMemoryUsage_committed   75956224        1368811502588
 * localhost_9999.sun_management_MemoryImpl.NonHeapMemoryUsage_init        24313856        1368811502588
 * localhost_9999.sun_management_MemoryImpl.NonHeapMemoryUsage_max 224395264       1368811502588
 * localhost_9999.sun_management_MemoryImpl.NonHeapMemoryUsage_used        39773216        1368811502588
 * Will parse into a map of metrics with each metric having a list of statistics
 * Each statistic has a value/time stamp
 * 
 * @author dimi5963
 *
 */
public class JmxOutputWriterParser {

	private String jmxPath;
	private PrintStream logger;
	private BufferedReader br = null;
	
	public JmxOutputWriterParser(String jmxPath,PrintStream logger){
		this.jmxPath = jmxPath;
		this.logger = logger;
	}
	
	public Map<String, List<Statistic>> fetchJmxData(){
		return fetchJmxData(-1);
	}

	public boolean isJmxPathValid(){
		boolean isValid = false;
		if(this.jmxPath != null){
			try{
				logger.println("attempting to open " + this.jmxPath);
				br = null;
				br = new BufferedReader(new FileReader(this.jmxPath));
			}catch(IOException ioe){
				logger.println("could not fetch data. ");
				ioe.printStackTrace(logger);
				isValid = false;
			}finally{
				try{
					if(br != null){
						br.close();
						isValid = true;
					}
				}catch(IOException ioe){
					logger.println("could not close the reader. ");
					ioe.printStackTrace(logger);
					isValid = false;
				}
			}
		}
		return isValid;
	}
	
	/**
	 * Fetch jmx data that's set up by jmx output writer (by jmxtrans library)
	 * @param logger
	 * @param buildStartTime
	 * @return list of metric entries split by metrics
	 */
	public Map<String, List<Statistic>> fetchJmxData(long buildStartTime){
		Map<String,List<Statistic>> metricData = new HashMap<String,List<Statistic>>();
		if(this.jmxPath != null){
			br = null;
			try{
				String currentLine;
				br = new BufferedReader(new FileReader(this.jmxPath));
				while((currentLine = br.readLine()) != null){
					//parse data here
					String[] metricEntry = currentLine.split("\\t", -1);
					if(metricEntry != null && metricEntry.length == 3){
						//expected entry
						String metricName = metricEntry[0];
						Statistic statistic = new Statistic(Float.parseFloat(metricEntry[1]), Long.parseLong(metricEntry[2]));
						if(!metricData.containsKey(metricName)){
							List<Statistic> statisticList = new ArrayList<Statistic>();
							statisticList.add(statistic);
							metricData.put(metricName, statisticList);
						} else{
							List<Statistic> statisticList = metricData.get(metricName);
							statisticList.add(statistic);
							metricData.put(metricName, statisticList);
						}
					}
				}
			}catch(IOException ioe){
				logger.println("could not fetch data. ");
				ioe.printStackTrace(logger);
			}finally{
				try{
					if(br != null)
						br.close();
				}catch(IOException ioe){
					logger.println("could not close the reader. ");
					ioe.printStackTrace(logger);
				}
			}
		}
		return metricData;
	}

}
