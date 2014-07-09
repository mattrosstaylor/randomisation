package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.lifeguide.randomisation.DBConnector;
import uk.ac.soton.ecs.lifeguide.randomisation.LifeGuideAPI;
import uk.ac.soton.ecs.lifeguide.randomisation.Participant;
import uk.ac.soton.ecs.lifeguide.randomisation.Statistics;
import uk.ac.soton.ecs.lifeguide.randomisation.Strategy;
import uk.ac.soton.ecs.lifeguide.randomisation.StrategyStatistics;
import uk.ac.soton.ecs.lifeguide.randomisation.TrialDefinition;
import uk.ac.soton.ecs.lifeguide.randomisation.app.LocalDBLoader.ParticipantData;


public class LocalDBConnector implements DBConnector{

	private static final String DATA_DIR = "data/";
	
	private static final String TRIAL_PATH_FILE = DATA_DIR + "trialpaths.txt";
	private static final String ALLOCATION_FILE = DATA_DIR + "allocations.txt";
	private static final String STATISTICS_FILE = DATA_DIR + "statistics.txt";
	
	private Map<String, String> trialPaths;						// Trial name -> trial path
	private Map<String, TrialDefinition> trials;				// Trial name -> trial definition
	private Map<String, Map<Integer, Integer>> allocations;		// Trial name -> (Patient ID -> allocation group ID)
	private Map<String, Statistics> statistics;					// Trial name -> statistics
	private Map<Integer, Integer> stratGroups;					// Participant ID -> stratified group
	private Map<Integer, Participant> participants;				// Participant ID -> participant
	
	@Override
	public boolean connect(){
		trialPaths = LocalDBLoader.loadTrialPaths(TRIAL_PATH_FILE);
		trials = LocalDBLoader.loadTrials(trialPaths);
		statistics = LocalDBLoader.loadStatistics(STATISTICS_FILE);
		participants = new HashMap<Integer, Participant>();
		
		ParticipantData participantData = LocalDBLoader.loadParticipants(ALLOCATION_FILE);
		allocations = participantData.allocations;
		stratGroups = participantData.stratGroups;
		
		return true;
	}

	@Override
	public boolean disconnect(){
		LocalDBWriter.writeLocalDB(this, TRIAL_PATH_FILE, ALLOCATION_FILE, STATISTICS_FILE);
		
		trials.clear();
		allocations.clear();
		allocations.clear();
		statistics.clear();
		stratGroups.clear();
		participants.clear();
		
		return true;
	}

	@Override
	public boolean registerTrial(TrialDefinition trialDefinition){
		if(trialExists(trialDefinition))
			return false;
		
		// Get the default parameter values.
		Map<String, Float> defaultParams = Strategy.getStoredParameters(trialDefinition.getStrategy(), trialDefinition);
		
		// If these parameter values have been overridden, update them.
		Map<String, Float> trialParams = trialDefinition.getStrategyParams();
		for(String trialParam: trialParams.keySet())
			if(defaultParams.containsKey(trialParam))
				defaultParams.put(trialParam, trialParams.get(trialParam));
		
		trials.put(trialDefinition.getTrialName(), trialDefinition);
		statistics.put(trialDefinition.getTrialName(), new StrategyStatistics(defaultParams));
		
		return true;
	}
	
	public boolean registerTrial(TrialDefinition trialDefinition, String filePath){
		boolean registered = registerTrial(trialDefinition);
		
		if(registered)
			trialPaths.put(trialDefinition.getTrialName(), filePath);
		
		return registered;
	}

	@Override
	public boolean trialExists(TrialDefinition trialDefinition){
		return trialExists(trialDefinition.getTrialName());
	}
	
	public boolean trialExists(String trialName){
		return trials.containsKey(trialName) && trials.get(trialName) != null;
	}

	@Override
	public int getCount(TrialDefinition trialDefinition, Map<String, Integer> args){
		return 0;
	}

	@Override
	public Statistics getStrategyStatistics(TrialDefinition trialDefinition){
		return statistics.get(trialDefinition.getTrialName());
	}
	
	public int getStratifiedGroup(int participantID){
		return stratGroups.get(participantID);
	}

	@Override
	public boolean update(TrialDefinition trialDefinition, Participant participant, Statistics strategyStatistics, int treatment){
		if(!allocations.containsKey(trialDefinition.getTrialName()))
			allocations.put(trialDefinition.getTrialName(), new HashMap<Integer, Integer>());
		
		allocations.get(trialDefinition.getTrialName()).put(participant.getId(), treatment);
		statistics.put(trialDefinition.getTrialName(), strategyStatistics);
		return true;
	}
	
	// Return the highest participant ID for a given trial.
	public int getMaxID(String trialName){
		if(!trialExists(trialName) || !allocations.containsKey(trialName))
			return -1;

		Map<Integer, Integer> participantMap = allocations.get(trialName);
		int maxID = -1;
		for(Integer id: participantMap.keySet()){
			if(id > maxID)
				maxID = id;
		}
		
		return maxID;
	}

	@Override
	public boolean registerStrategy(String strategy, String className){
		return false;
	}

	@Override
	public boolean strategyExists(String strategy){
		return false;
	}

	@Override
	public void setLifeGuideAPI(LifeGuideAPI lifeGuideAPI){}

	public void addParticipant(TrialDefinition trialDefinition, Participant participant){
		participants.put(participant.getId(), participant);
		stratGroups.put(participant.getId(), trialDefinition.getStratifiedEnumeration(participant));
	}
	
	@Override
	public Participant getParticipant(int id){
		return participants.get(id);
	}

	@Override
	public TrialDefinition getTrialDefinition(String name){
		return trials.get(name);
	}

	@Override
	public Set<String> getTrialDefinitionNames(){
		return trials.keySet();
	}
	
	public Map<String, String> getFilePaths(){
		return trialPaths;
	}
	
	public Map<String, Map<Integer, Integer>> getAllocations(){
		return allocations;
	}
	
	public Map<String, Statistics> getStatistics(){
		return statistics;
	}
	
	public void deleteTrial(String trialName){
		trialPaths.remove(trialName);
		trials.remove(trialName);
		allocations.remove(trialName);
		statistics.remove(trialName);
	}

}
