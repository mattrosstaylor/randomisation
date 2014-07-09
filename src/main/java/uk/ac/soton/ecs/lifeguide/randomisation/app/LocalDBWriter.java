package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.lifeguide.randomisation.Statistics;


public class LocalDBWriter{

	public static void writeLocalDB(LocalDBConnector dbConnector,
									String trialFilePath,
									String allocationFilePath,
									String statisticsFilePath){
		
		try{
			writeFilePaths(dbConnector, trialFilePath);
			writeAllocations(dbConnector, allocationFilePath);
			writeStatistics(dbConnector, statisticsFilePath);
		} catch(IOException e){
			// GUI is closing, no use showing error to user.
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void writeFilePaths(LocalDBConnector dbConnector, String trialFilePath) throws IOException{
		Map<String, String> filePaths = dbConnector.getFilePaths(); 
		File filePathFile = new File(trialFilePath);
		if(filePathFile.exists())
			filePathFile.delete();
		filePathFile.createNewFile();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(trialFilePath));
		
		for(String path: filePaths.values()){
			writer.append(path);
			writer.newLine();
		}
		
		writer.close();
	}
	
	public static void writeAllocations(LocalDBConnector dbConnector, String allocationFilePath) throws IOException{
		Map<String, Map<Integer, Integer>> allocations = dbConnector.getAllocations();
		File allocationFile = new File(allocationFilePath);
		if(allocationFile.exists())
			allocationFile.delete();
		allocationFile.createNewFile();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(allocationFile));
		
		for(String trialName: allocations.keySet()){
			writer.append(trialName);
			writer.newLine();
			Map<Integer, Integer> allocs = allocations.get(trialName);
			for(Integer participantID: allocs.keySet()){
				String allocLine = participantID + ",";
				allocLine += dbConnector.getStratifiedGroup(participantID) + ",";
				allocLine += allocs.get(participantID);
				
				writer.append(allocLine);
				writer.newLine();
			}
		}
		
		writer.close();
	}
	
	public static void writeStatistics(LocalDBConnector dbConnector, String statisticsFilePath) throws IOException{
		Map<String, Statistics> statistics = dbConnector.getStatistics();
		File statisticsFile = new File(statisticsFilePath);
		if(statisticsFile.exists())
			statisticsFile.delete();
		statisticsFile.createNewFile();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(statisticsFile));
		
		for(String trialName: statistics.keySet()){
			writer.append(trialName);
			writer.newLine();
			
			Statistics trialStats = statistics.get(trialName);
			Set<String> trialStatNames = trialStats.getAllNames();
			
			for(String statName: trialStatNames){
				writer.append(statName + "=");
				writer.append(trialStats.getStatistic(statName).toString());
				writer.newLine();
			}
		}
		
		writer.close();
	}
	
}
