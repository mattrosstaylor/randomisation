package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	protected final Trial trial;
	protected final DataManager database;
	protected final Map<String, Double> statistics;
	protected final Map<String, Double> parameters;

	public Strategy(Trial trial, DataManager database){
		this.trial = trial;
		this.database = database;
		this.statistics = trial.getStatistics();
		this.parameters = trial.getParameters();
	}

	protected abstract Arm allocate(Participant participant) throws PersistenceException;

	protected abstract Map<String, Double> getInitialisedStats(Trial trial);

	protected String getAllocationStatisticName(String armName, String strataName) {
		String result = armName +" allocations";
		
		if (!strataName.equals("")) {
			result +=" (" +strataName +")";
		} 
		return result;
	}
}
