package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public abstract class Strategy {

	private static final Logger logger = LoggerFactory.getLogger(Strategy.class);

	private static final String STRATEGY_CLASS_PACKAGE = "uk.ac.soton.ecs.lifeguide.randomisation.";

	public static Strategy create(Trial trial, DataManager database) throws InvalidTrialException {
		try {
			// Attempt to load the named class. Use casting/not-found exceptions to detect failure.
			String className = STRATEGY_CLASS_PACKAGE + trial.getStrategy();

			return Class.forName(className).asSubclass(Strategy.class).getConstructor(Trial.class, DataManager.class).newInstance(trial, database);
		}
		catch (Exception e) {
			throw new InvalidTrialException("Allocation method not found: " + trial.getStrategy() + ".");
		}
	}

	protected Random random = new Random();
	protected final Trial trial;
	protected final DataManager database;
	protected final Map<String, Double> parameters;

	public Strategy(Trial trial, DataManager database){
		this.trial = trial;
		this.database = database;
		this.parameters = trial.getParameters();
	}

	protected Arm allocate(Participant participant) throws PersistenceException {
		String stratifiedEnum = trial.getStrata(participant);

		Map<Arm, Integer> allocations = new HashMap<Arm, Integer>();
		List<Arm> openArms = new ArrayList<Arm>();
	
		for (Arm a : trial.getArms()) {
			Double strategyStatistic = getStatistic(stratifiedEnum, a.getName(), "allocations");
			int roundedVal = (int)(Math.round(strategyStatistic));
			
			if (roundedVal < a.getMaxParticipants()) {
				openArms.add(a);
			}
			allocations.put(a, roundedVal);
		}

		if (openArms.isEmpty()) {
			logger.debug("Trial full.");
			return trial.getDefaultArm();
		}

		Arm arm = allocateHelper(participant, stratifiedEnum, openArms, allocations);
		setStatistic(stratifiedEnum, arm.getName(), "allocations", Double.valueOf(allocations.get(arm) + 1));
		database.update(trial, participant, arm);
		return arm;
	}

	protected abstract Arm allocateHelper(Participant participant, String stratifiedEnum, List<Arm> openArms, Map<Arm, Integer> allocations);

	protected String getStatisticString(String strataName, String subName, String statisticName) {
		String result = "";
		if (!strataName.equals("")) {
			result += "("+strataName+") ";
		}
		if (!subName.equals("")) {
			result += subName +" ";
		}
		return result+statisticName;
	}

	protected Double getStatistic(String strataName, String subName, String statisticName) {
		String name = getStatisticString(strataName, subName, statisticName);
		if (parameters.containsKey(name)) {
			return parameters.get(name);	
		}
		else {
			return 0.0;
		}	
	}

	protected void setStatistic(String strataName, String subName, String statisticName, Double value) {
		String name = getStatisticString(strataName, subName, statisticName);
		parameters.put(name, value);
	}
}
