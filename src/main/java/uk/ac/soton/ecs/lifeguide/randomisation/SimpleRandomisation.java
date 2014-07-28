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
	protected int allocateImplementation(Trial trialDefinition,
										 Participant participant,
										 DataManager database) throws AllocationException {
		Map<String, Float> strategyStatistics;

		strategyStatistics = trialDefinition.getStatistics();
		int stratifiedEnum = trialDefinition.getStratifiedEnumeration(participant);
		List<Integer> allocations = new ArrayList<Integer>(trialDefinition.getArms().size());
		for (int i = 0; i < trialDefinition.getArmCount(); i++) {
			allocations.add(Math.round(strategyStatistics.get(stratifiedEnum + "_" + i + "_allocation")));
		}
		int sum = 0;
		List<Arm> treatments = trialDefinition.getArms();
		for (Arm treatment : treatments) {
			if (treatment.getMaxParticipants() > allocations.get(treatments.indexOf(treatment)))
				sum += treatment.getWeight();
		}

		//Trial full
		if (sum == 0) {
			logger.debug("Trial full.");
			return trialDefinition.getDefaultArmIndex();
		}

		int roll, arm;
		roll = new Random().nextInt(sum);
		arm = 0;
		while (treatments.get(arm).getWeight() <= roll || allocations.get(arm) >= treatments.get(arm).getMaxParticipants()) {
			if (treatments.get(arm).getMaxParticipants() > allocations.get(arm)) {
				roll -= treatments.get(arm).getWeight();
			}
			arm++;
		}

		strategyStatistics.put(stratifiedEnum + "_" + arm + "_allocation", Float.valueOf(allocations.get(arm) + 1));
		try {
			database.update(trialDefinition, participant, strategyStatistics, arm);
		} catch (PersistenceException e) {
			throw new AllocationException("SQL exception on updating statistics: " + e.getMessage());
		}
		return arm;
	}

	@Override
	protected List<String> getRequiredParametersImplementation() {
		return new ArrayList<String>(0);
	}

	@Override
	protected Map<String, Float> getStoredParametersImplementation(Trial trialDefinition) {
		Map<String, Float> params = new HashMap<String, Float>();
		for (int i = 0; i < trialDefinition.getStratifiedCount(); i++)
			for (int j = 0; j < trialDefinition.getArmCount(); j++)
				params.put(i + "_" + j + "_allocation", 0f);
		return params;
	}

	//@Override
	protected Map<String, Float> getStoredParametersImplementation() {
		return new HashMap<String, Float>();
	}

	@Override
	protected void checkValidTrialImplementation(Trial trialDefinition) throws InvalidTrialException {
	}

}
