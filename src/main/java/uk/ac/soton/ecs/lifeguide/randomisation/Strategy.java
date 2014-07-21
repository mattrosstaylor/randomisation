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
 * @see TrialDefinition
 * @see Participant
 * @see DBConnector
 * @since 1.7
 */
public abstract class Strategy {

	private static final Logger logger = LoggerFactory.getLogger(Strategy.class);

	private static final ReentrantLock lock = new ReentrantLock(true);
	private static final Map<Class<? extends Strategy>, Strategy> factory = new HashMap<Class<? extends Strategy>, Strategy>();

	/**
	 * The method allocates a participant of the trial to a treatment.
	 * The method is static and synchronous globally through {@link ReentrantLock}.
	 * This is the only method that should be invoked for allocation regardless
	 * of the actual implementation class of the strategy algorithm.
	 *
	 * @param trialName     The simple name of the trial, registered at the data source.
	 * @param participantId The ID of the participant which is to be allocated.
	 * @param dbConnector   A reference to an implementation of the {@link DBConnector} interface
	 * @return <code>int</code> representing the allocated arm number, starting from 0 or
	 *         <code>-1</code> if it was not possible to allocate the patient to any treatment. This is used when all
	 *         the treatments have reached their limit of participant and the trial should be terminated.
	 * @throws AllocationException if something goes wrong while allocating. A meaningful message should be returned. // mrt - fuck you
	 */
	public static int allocate(String trialName,
							   int participantId,
							   DBConnector dbConnector) throws AllocationException, PersistenceException, InvalidTrialException {
		int arm = 0;
		Participant participant = null;
		TrialDefinition trialDefinition = null;
		try {
			participant = dbConnector.getParticipant(participantId);
			trialDefinition = dbConnector.getTrialDefinition(trialName);
		}
		catch (IllegalArgumentException e) {
			throw new AllocationException("A participant with such ID does not exist.");
		}

		lock.lock();
		try {
			if (factory.get(trialDefinition.getStrategy()) == null) {
				factory.put(trialDefinition.getStrategy(), trialDefinition.getStrategy().newInstance());
			}
			arm = factory.get(trialDefinition.getStrategy()).allocateImplementation(trialDefinition, participant, dbConnector);
		}
		catch (Exception e) { // mrt - change this to a better exception later
			logger.error("Exception: ", e.fillInStackTrace());
			throw new AllocationException("Something went terribly wrong with usage of reflection methods.");
		}
		finally {
			lock.unlock();
		}
		logger.trace("Assigning treatment arm: {}.", arm);

		return arm;
	}

	/**
	 * @param cls The concrete class of the strategy implementation querying about.
	 * @return The names of all the parameters for that class that need to be provided in the {@link TrialDefinition} object.
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
	public static Map<String, Float> getStoredParameters(Class<? extends Strategy> cls, TrialDefinition trialDefinition) {
		try {
			if (factory.get(cls) == null)
				factory.put(cls, cls.newInstance());
			return factory.get(cls).getStoredParametersImplementation(trialDefinition);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Main method used for allocating a participant to a concrete treatment arm.
	 * The method is called statically through {@link #allocate(String, int, DBConnector)} and
	 * should not be called in any other way.
	 *
	 * @param trialDefinition The {@link TrialDefinition} object for which allocation is done.
	 * @param participant     The concrete {@link Participant} to be allocated.
	 * @param dbConnector     The {@link DBConnector} that will allow any data access.
	 * @return The treatment arm that the patient is allocated to.
	 */
	protected abstract int allocateImplementation(TrialDefinition trialDefinition, Participant participant, DBConnector dbConnector) throws AllocationException;

	/**
	 * The method is called statically through {@link #getRequiredParameters(Class)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be provided in the {@link TrialDefinition} object.
	 */
	protected abstract List<String> getRequiredParametersImplementation();

	/**
	 * The method is called statically through {@link #getStoredParameters(Class, TrialDefinition)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be stored on the data source, mapped to their
	 *         default values.
	 */
	protected abstract Map<String, Float> getStoredParametersImplementation(TrialDefinition trialDefinition);


	/**
	 * This method is called when loading in any trial which wishes to use this strategy. Allows a specific Strategy
	 * subclass to implement checks specific to their allocation process. If a check does not pass, this method
	 * throws an InvalidTrialException with a message describing the problem with the trial's parameters.
	 *
	 * @param trialDefinition The {@link TrialDefinition} object which should be validated.
	 */
	protected static void checkValidTrial(TrialDefinition trialDefinition) throws InvalidTrialException {
		try {
			if (factory.get(trialDefinition.getStrategy()) == null)
				factory.put(trialDefinition.getStrategy(), trialDefinition.getStrategy().newInstance());

			factory.get(trialDefinition.getStrategy()).checkValidTrialImplementation(trialDefinition);
		} catch (Exception e) { // mrt - change this to a better exception later
			logger.error("Exception: ", e.fillInStackTrace());
		}
	}

	protected abstract void checkValidTrialImplementation(TrialDefinition trialDefinition) throws InvalidTrialException;

}
