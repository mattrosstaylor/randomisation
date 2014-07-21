package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
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
	protected int allocateImplementation(TrialDefinition trialDefinition,
										 Participant participant,
										 DBManager database) throws AllocationException {
		Statistics strategyStatistics = null;
		try {
			strategyStatistics = database.getStrategyStatistics(trialDefinition);
		} catch (SQLException e) {
			throw new AllocationException("SQL Exception for statistics: " + e.getMessage());
		}
		int stratifiedEnum = trialDefinition.getStratifiedEnumeration(participant);
		List<Integer> allocations = new ArrayList<Integer>(trialDefinition.getTreatments().size());
		for (int i = 0; i < trialDefinition.getTreatmentCount(); i++) {
			allocations.add(Math.round(strategyStatistics.getStatistic(stratifiedEnum + "_" + i + "_allocation")));
		}
		int sum = 0;
		List<Treatment> treatments = trialDefinition.getTreatments();
		for (Treatment treatment : treatments) {
			if (treatment.getParticipantLimit() > allocations.get(treatments.indexOf(treatment)))
				sum += treatment.getWeight();
		}

		//Trial full
		if (sum == 0) {
			logger.debug("Trial full.");
			return trialDefinition.getDefaultTreatmentIndex();
		}

		int roll, arm;
		roll = new Random().nextInt(sum);
		arm = 0;
		while (treatments.get(arm).getWeight() <= roll || allocations.get(arm) >= treatments.get(arm).getParticipantLimit()) {
			if (treatments.get(arm).getParticipantLimit() > allocations.get(arm)) {
				roll -= treatments.get(arm).getWeight();
			}
			arm++;
		}

		strategyStatistics.putStatistic(stratifiedEnum + "_" + arm + "_allocation", Float.valueOf(allocations.get(arm) + 1));
		try {
			database.update(trialDefinition, participant, strategyStatistics, arm);
		} catch (SQLException e) {
			throw new AllocationException("SQL exception on updating statistics: " + e.getMessage());
		}
		return arm;
	}

	@Override
	protected List<String> getRequiredParametersImplementation() {
		return new ArrayList<String>(0);
	}

	@Override
	protected Map<String, Float> getStoredParametersImplementation(TrialDefinition trialDefinition) {
		Map<String, Float> params = new HashMap<String, Float>();
		for (int i = 0; i < trialDefinition.getStratifiedCount(); i++)
			for (int j = 0; j < trialDefinition.getTreatmentCount(); j++)
				params.put(i + "_" + j + "_allocation", 0f);
		return params;
	}

	//@Override
	protected Map<String, Float> getStoredParametersImplementation() {
		return new HashMap<String, Float>();
	}

	@Override
	protected void checkValidTrialImplementation(TrialDefinition trialDefinition) throws InvalidTrialException {
	}

}
