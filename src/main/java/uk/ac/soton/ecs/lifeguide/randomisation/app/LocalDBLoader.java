package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.soton.ecs.lifeguide.randomisation.ParserUtils;
import uk.ac.soton.ecs.lifeguide.randomisation.Statistics;
import uk.ac.soton.ecs.lifeguide.randomisation.StrategyStatistics;
import uk.ac.soton.ecs.lifeguide.randomisation.TrialDefinition;
import uk.ac.soton.ecs.lifeguide.randomisation.TrialLoader;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;


public class LocalDBLoader{

	public static Map<String, String> loadTrialPaths(String storageFilePath){
		Map<String, String> paths = new HashMap<String, String>();
		
		try{
			// Creates the storage file if it doesn't yet exist
			File storageFile = new File(storageFilePath);
			storageFile.createNewFile();
			
			BufferedReader reader = new BufferedReader(new FileReader(storageFile));
			String line = "";
			
			while((line = reader.readLine()) != null){
				line = line.trim();

				paths.put(ParserUtils.getAlphanumericFileName(line), line);
			}
			
			reader.close();
		}catch(IOException e){
			// GUI not yet initialised, no use trying to output the error to the GUI.
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return paths;
	}
	
	public static Map<String, TrialDefinition> loadTrials(Map<String, String> trialFilePaths){
		Map<String, TrialDefinition> trials = new HashMap<String, TrialDefinition>();
		
		try{
			for(String filePath: trialFilePaths.values()){
				File trialFile = new File(filePath);
				if(!trialFile.exists())
					continue;
			
				TrialDefinition trialDef = TrialLoader.loadTrial(filePath);
				trials.put(trialDef.getTrialName(), trialDef);
			}
		}catch(InvalidTrialException e){
			// GUI not yet initialised, no use trying to output the error to the GUI.
			System.out.println(e.getMessage());
		}
		
		return trials;
	}
	
	// Essentially a Pair object, storing a map of allocations, and a map of stratified groups.
	public static class ParticipantData{
		// Allocations (trial name -> (participant ID -> treatment group)).
		public Map<String, Map<Integer, Integer>> allocations;
		// Stratified groups (participant ID -> stratified group ID).
		public Map<Integer, Integer> stratGroups;
	}

	public static ParticipantData loadParticipants(String allocationFilePath){
		ParticipantData participantData = new ParticipantData();
		participantData.allocations = new HashMap<String, Map<Integer, Integer>>();
		participantData.stratGroups = new HashMap<Integer, Integer>();

		try{
			File allocationFile = new File(allocationFilePath);
			allocationFile.createNewFile();
			
			BufferedReader reader = new BufferedReader(new FileReader(allocationFile));
			String line = "";
			String currentTrialName = "";
			
			while((line = reader.readLine()) != null){
				String[] lineSplit = line.split(",");

				if(lineSplit.length == 1){ // Start of a trial allocation list
					String trimLine = line.trim();
					
					if(trimLine.equals(""))
						continue;
					
					currentTrialName = trimLine;
					participantData.allocations.put(trimLine, new HashMap<Integer, Integer>());
					
				} else if(lineSplit.length == 3 && !currentTrialName.equals("")){ // Allocation element
					try{
						int participantID = Integer.parseInt(lineSplit[0].trim());
						int stratGroupID = Integer.parseInt(lineSplit[1].trim());
						int allocationID = Integer.parseInt(lineSplit[2].trim());
						
						participantData.allocations.get(currentTrialName).put(participantID, allocationID);
						participantData.stratGroups.put(participantID, stratGroupID);
					} catch(NumberFormatException e){
						reader.close();
						throw new IOException("Invalid line in the allocations file: " + line + ".");
					}
				} else {
					reader.close();
					throw new IOException("Invalid line in the allocations file: " + line + ".");
				}
			}
			
			reader.close();
		} catch(IOException e){
			// GUI not yet initialised, no use trying to output the error to the GUI.
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return participantData;
	}

	public static Map<String, Statistics> loadStatistics(String statisticsFilePath){
		Map<String, Statistics> statistics = new HashMap<String, Statistics>();
		
		try{
			File statisticsFile = new File(statisticsFilePath);
			statisticsFile.createNewFile();
			
			BufferedReader reader = new BufferedReader(new FileReader(statisticsFile));
			String line = "";
			String currentTrialName = "";
			
			while((line = reader.readLine()) != null){
				String[] lineSplit = line.split("=");

				if(lineSplit.length == 1){ // Start of a trial allocation list
					String trimLine = line.trim();
					
					if(trimLine.equals(""))
						continue;
					
					currentTrialName = trimLine;
					statistics.put(trimLine, new StrategyStatistics());
					
				} else if(lineSplit.length == 2 && !currentTrialName.equals("")){ // Allocation element
					try{
						float statisticValue = Float.parseFloat(lineSplit[1]);
						
						statistics.get(currentTrialName).putStatistic(lineSplit[0].trim(), statisticValue);
					} catch(NumberFormatException e){
						reader.close();
						throw new IOException("Invalid line in the statistics file: " + line + ".");
					}
				}
			}
			
			reader.close();
		} catch(IOException e){
			// GUI not yet initialised, no use trying to output the error to the GUI.
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return statistics;
	}
	
}
