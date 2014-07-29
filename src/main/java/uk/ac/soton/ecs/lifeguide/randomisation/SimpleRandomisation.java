package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * Implementation for the simple randomisation algorithm.
 * The algorithm itself can be thought of as a coin flips in the sense it is unbiased in any way.
 * The probabilistic model includes different weighting between treatment arms.
 * </p>
 * <b>Parameters:</b> The method does not have any required or stored parameters.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see Strategy
 * @since 1.7
 */
public class SimpleRandomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(SimpleRandomisation.class);

	@Override
	protected Arm allocateImplementation(Trial trial,
										 Participant participant,
										 DataManager database) throws AllocationException {
		Map<String, Float> strategyStatistics;

		strategyStatistics = trial.getStatistics();
		int stratifiedEnum = trial.getStratifiedEnumeration(participant);
		List<Integer> allocations = new ArrayList<Integer>(trial.getArms().size());
		for (Arm a : trial.getArms()) {
			String enumString = stratifiedEnum + "_" + a.getName() + "_allocation";
			Float strategyStatistic = strategyStatistics.get(enumString);
			int roundedVal = Math.round(strategyStatistic);
			allocations.add(roundedVal);
		}
		int sum = 0;
		List<Arm> arms = trial.getArms();
		for (Arm treatment : arms) {
			if (treatment.getMaxParticipants() > allocations.get(arms.indexOf(treatment)))
				sum += treatment.getWeight();
		}

		//Trial full
		if (sum == 0) {
			logger.debug("Trial full.");
			return trial.getDefaultArm();
		}

		int roll, arm;
		roll = new Random().nextInt(sum);
		arm = 0;
		while (arms.get(arm).getWeight() <= roll || allocations.get(arm) >= arms.get(arm).getMaxParticipants()) {
			if (arms.get(arm).getMaxParticipants() > allocations.get(arm)) {
				roll -= arms.get(arm).getWeight();
			}
			arm++;
		}

		strategyStatistics.put(stratifiedEnum + "_" + arms.get(arm).getName() + "_allocation", Float.valueOf(allocations.get(arm) + 1));
		try {
			database.update(trial, participant, arms.get(arm));
		} catch (PersistenceException e) {
			throw new AllocationException("SQL exception on updating statistics: " + e.getMessage());
		}
		return arms.get(arm);
	}

	@Override
	protected List<String> getRequiredParametersImplementation() {
		return new ArrayList<String>(0);
	}

	@Override
	protected Map<String, Float> getStoredParametersImplementation(Trial trial) {
		Map<String, Float> params = new HashMap<String, Float>();
		for (int i = 0; i < trial.getStratifiedCount(); i++) {
			for (Arm a : trial.getArms()) { // mrt changed from index to your mom
				params.put(i + "_" + a.getName() + "_allocation", 0f);
			}
		}
		return params;
	}

	//@Override
	protected Map<String, Float> getStoredParametersImplementation() {
		return new HashMap<String, Float>();
	}

	@Override
	protected void checkValidTrialImplementation(Trial trial) throws InvalidTrialException {
	}

}
