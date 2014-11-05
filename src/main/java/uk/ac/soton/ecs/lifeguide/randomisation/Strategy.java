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

	public static Strategy create(String strategyName) throws InvalidTrialException {
		try {
			// Attempt to load the named class. Use casting/not-found exceptions to detect failure.
			String className = STRATEGY_CLASS_PACKAGE + strategyName;
			Class<? extends Strategy> strategyClass = Class.forName(className).asSubclass(Strategy.class);

			// Do not allow the Strategy class itself.
			if (strategyClass.equals(Strategy.class)) {
				throw new ClassNotFoundException();
			}
			return strategyClass.newInstance();
		}
		catch (Exception e) {
			throw new InvalidTrialException("Allocation method not found: " + strategyName + ".");
		}
	}

	protected abstract Arm allocate(Trial trial, Participant participant, DataManager database) throws PersistenceException;

	protected abstract Map<String, Double> getInitialisedStats(Trial trial);

	protected String getAllocationStatisticName(String armName, String strataName) {
		String result = armName +" allocations";
		
		if (!strataName.equals("")) {
			result +=" (" +strataName +")";
		} 
		return result;
	}
}
