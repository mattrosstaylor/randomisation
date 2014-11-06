package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimpleRandomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(SimpleRandomisation.class);

	@Override
	protected Arm allocate(Trial trial, Participant participant, DataManager database) throws PersistenceException {
		Map<String, Double> strategyStatistics = trial.getStatistics();
		String stratifiedEnum = trial.getStrata(participant);

		Map<Arm, Integer> allocations = new HashMap<Arm, Integer>();
		int openArmsWeightSum = 0;
		List<Arm> openArms = new ArrayList<Arm>();
	
		for (Arm a : trial.getArms()) {
			String enumString = getAllocationStatisticName(a.getName(), stratifiedEnum);
			Double strategyStatistic = strategyStatistics.get(enumString);
			int roundedVal = (int)(Math.round(strategyStatistic));
			
			if (roundedVal < a.getMaxParticipants()) {
				openArmsWeightSum += a.getWeight();
				openArms.add(a);
				allocations.put(a, roundedVal);
			}
		}

		if (openArms.isEmpty()) {
			logger.debug("Trial full.");
			return trial.getDefaultArm();
		}

		int roll = new Random().nextInt(openArmsWeightSum);
		Arm arm = null;
		Iterator<Arm> armIter = openArms.iterator();

		do {
			arm = armIter.next();
			roll -= arm.getWeight();
		} while (roll >= 0);

		strategyStatistics.put(getAllocationStatisticName(arm.getName(), stratifiedEnum), Double.valueOf(allocations.get(arm) + 1));
		database.update(trial, participant, arm);

		return arm;
	}

	@Override
	protected Map<String, Double> getInitialisedStats(Trial trial) {
		Map<String, Double> stats = new HashMap<String, Double>();
		for (String strata : trial.getAllStrata()) {
			for (Arm a : trial.getArms()) {
				stats.put(getAllocationStatisticName(a.getName(), strata), 0.0);
			}
		}
		return stats;
	}

}
