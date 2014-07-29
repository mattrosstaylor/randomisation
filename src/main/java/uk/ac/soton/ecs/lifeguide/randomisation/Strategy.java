package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the superclass for any allocation strategy algorithm to be implemented.
 * Provides a static unified invocation of the methods of any of its subclasses.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see Trial
 * @see Participant
 * @see DataManager
 * @since 1.7
 */
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
		catch (ClassNotFoundException e) {
			throw new InvalidTrialException("Allocation method not found: " + strategyName + ".");
		}
		catch (ClassCastException e) {
			throw new InvalidTrialException("Allocation method not found: " + strategyName + ".");
		}
		catch (InstantiationException e) {
			throw new InvalidTrialException("Allocation method not found: " + strategyName + ".");
		}
		catch (IllegalAccessException e) {
			throw new InvalidTrialException("Allocation method not found: " + strategyName + ".");
		}
	}

	private static final ReentrantLock lock = new ReentrantLock(true);
	private static final Map<Class<? extends Strategy>, Strategy> factory = new HashMap<Class<? extends Strategy>, Strategy>();

	/**
	 * @param cls The concrete class of the strategy implementation querying about.
	 * @return The names of all the parameters for that class that need to be provided in the {@link Trial} object.
	 *         If no parameters are to be required should return an empty {@link List}.
	 */
	public static List<String> getRequiredParameters(Class<? extends Strategy> cls) {
		try {
			if (factory.get(cls) == null)
				factory.put(cls, cls.newInstance());
			return factory.get(cls).getRequiredParametersImplementation();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * @param cls The concrete class of the strategy implementation querying about.
	 * @return The names of all the fixed parameters for that class that need to be stored on the data source,
	 *         mapped to their default values. If no parameters are to be stored should return an empty {@link Map}.
	 */
	public static Map<String, Float> getStoredParameters(Class<? extends Strategy> cls, Trial trial) {
		try {
			if (factory.get(cls) == null)
				factory.put(cls, cls.newInstance());
			return factory.get(cls).getStoredParametersImplementation(trial);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Main method used for allocating a participant to a concrete treatment arm.
	 * The method is called statically through {@link #allocate(String, int, DataManager)} and
	 * should not be called in any other way.
	 *
	 * @param trial The {@link Trial} object for which allocation is done.
	 * @param participant     The concrete {@link Participant} to be allocated.
	 * @param database     The {@link DataManager} that will allow any data access.
	 * @return The treatment arm that the patient is allocated to.
	 */
	protected abstract Arm allocateImplementation(Trial trial, Participant participant, DataManager database) throws AllocationException;

	/**
	 * The method is called statically through {@link #getRequiredParameters(Class)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be provided in the {@link Trial} object.
	 */
	protected abstract List<String> getRequiredParametersImplementation();

	/**
	 * The method is called statically through {@link #getStoredParameters(Class, Trial)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be stored on the data source, mapped to their
	 *         default values.
	 */
	protected abstract Map<String, Float> getStoredParametersImplementation(Trial trial);


	/**
	 * This method is called when loading in any trial which wishes to use this strategy. Allows a specific Strategy
	 * subclass to implement checks specific to their allocation process. If a check does not pass, this method
	 * throws an InvalidTrialException with a message describing the problem with the trial's parameters.
	 *
	 * @param trial The {@link Trial} object which should be validated.
	 */
	protected static void checkValidTrial(Trial trial) throws InvalidTrialException {
		try {
			Strategy.create(trial.getStrategy()).checkValidTrialImplementation(trial);
		} catch (Exception e) { // mrt - change this to a better exception later
			logger.error("Exception: ", e.fillInStackTrace());
		}
	}

	protected abstract void checkValidTrialImplementation(Trial trial) throws InvalidTrialException;

}
