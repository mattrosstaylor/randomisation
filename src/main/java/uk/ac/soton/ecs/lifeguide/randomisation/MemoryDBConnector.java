package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class for simulating a {@link DBConnector} in memory.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class MemoryDBConnector implements DBConnector {

	private Map<String, TrialDefinition> trialsTable;
	private Map<String, Class<? extends Strategy>> strategyTable;
	private Map<String, Statistics> statisticsTable;
	private Map<Integer, Integer> patientsTable;
	private LifeGuideAPI lifeGuideAPI;

	@Override
	public boolean connect() {
		trialsTable = new HashMap<String, TrialDefinition>();
		strategyTable = new HashMap<String, Class<? extends Strategy>>(5);
		statisticsTable = new HashMap<String, Statistics>();
		patientsTable = new HashMap<Integer, Integer>();
		return true;
	}

	@Override
	public boolean disconnect() {
		trialsTable.clear();
		strategyTable.clear();
		statisticsTable.clear();
		patientsTable.clear();
		return true;
	}

	@Override
	public boolean registerTrial(TrialDefinition trialDefinition) {
		if (trialExists(trialDefinition))
			return false;
		trialsTable.put(trialDefinition.getTrialName(), trialDefinition);

		Map<String, Float> params = Strategy.getStoredParameters(trialDefinition.getStrategy(), trialDefinition);
		Map<String, Float> trialParams = trialDefinition.getStrategyParams();
		for (String param : trialParams.keySet())
			if (params.containsKey(param))
				params.put(param, trialParams.get(param));
		Statistics statistics = new StrategyStatistics(params);
		statisticsTable.put(trialDefinition.getTrialName(), statistics);
		return true;
	}

	@Override
	public boolean trialExists(TrialDefinition trialDefinition) {
		if (trialsTable.get(trialDefinition.getTrialName()) != null)
			return true;
		else
			return false;
	}

	@Override
	public int getCount(TrialDefinition trialDefinition, Map<String, Integer> args) {
		return 0;
	}

	@Override
	public Statistics getStrategyStatistics(TrialDefinition trialDefinition) {
		return statisticsTable.get(trialDefinition.getTrialName());
	}

	@Override
	public boolean update(TrialDefinition trialDefinition, Participant participant, Statistics strategyStatistics, int treatment) {
		patientsTable.put(participant.getId(), treatment);
		statisticsTable.put(trialDefinition.getTrialName(), strategyStatistics);
		return true;
	}

	@Override
	public boolean registerStrategy(String strategy, String className) {
		if (strategyExists(strategy))
			return false;
		try {
			strategyTable.put(strategy, Class.forName(className).asSubclass(Strategy.class));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return true;
	}

	@Override
	public boolean strategyExists(String strategy) {
		if (strategyTable.get(strategy) != null)
			return true;
		else
			return false;
	}

	@Override
	public void setLifeGuideAPI(LifeGuideAPI lifeGuideAPI) {
		this.lifeGuideAPI = lifeGuideAPI;
	}

	@Override
	public Participant getParticipant(int id) {
		return lifeGuideAPI.getParticipant(id);
	}

	@Override
	public TrialDefinition getTrialDefinition(String name) {
		return trialsTable.get(name);
	}

	@Override
	public Set<String> getTrialDefinitionNames() {
		return trialsTable.keySet();
	}
}
