package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;

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
	public void connect() throws PersistenceException {
		trialsTable = new HashMap<String, TrialDefinition>();
		strategyTable = new HashMap<String, Class<? extends Strategy>>(5);
		statisticsTable = new HashMap<String, Statistics>();
		patientsTable = new HashMap<Integer, Integer>();
	}

	@Override
	public void disconnect() throws PersistenceException {
		trialsTable.clear();
		strategyTable.clear();
		statisticsTable.clear();
		patientsTable.clear();
	}

	@Override
	public void registerTrial(TrialDefinition trialDefinition) throws PersistenceException {
		if (trialExists(trialDefinition)) {
			throw new PersistenceException("could you BE any more lame");
		}
		trialsTable.put(trialDefinition.getTrialName(), trialDefinition);

		Map<String, Float> params = Strategy.getStoredParameters(trialDefinition.getStrategy(), trialDefinition);
		Map<String, Float> trialParams = trialDefinition.getStrategyParams();
		for (String param : trialParams.keySet()) {
			if (params.containsKey(param)) {
				params.put(param, trialParams.get(param));
			}
		}
		Statistics statistics = new StrategyStatistics(params);
		statisticsTable.put(trialDefinition.getTrialName(), statistics);
	}

	@Override
	public boolean trialExists(TrialDefinition trialDefinition) {
		if (trialsTable.get(trialDefinition.getTrialName()) != null)
			return true;
		else
			return false;
	}

	/*@Override
	public int getCount(TrialDefinition trialDefinition, Map<String, Integer> args) {
		return 0;
	}*/ // mrt - fuck you

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
