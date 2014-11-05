package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimpleRandomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(SimpleRandomisation.class);

	@Override
	protected Arm allocate(Trial trial,
										 Participant participant,
										 DataManager database) throws PersistenceException {
		Map<String, Double> strategyStatistics = trial.getStatistics();
		String stratifiedEnum = trial.getStrata(participant);
		List<Integer> allocations = new ArrayList<Integer>(trial.getArms().size()); //mrt - change this to a map
		for (Arm a : trial.getArms()) {
			String enumString = getAllocationStatisticName(a.getName(), stratifiedEnum);
			Double strategyStatistic = strategyStatistics.get(enumString);
			int roundedVal = (int)(Math.round(strategyStatistic));
			allocations.add(roundedVal);
		}
		int sum = 0;
		List<Arm> arms = trial.getArms();
		for (Arm a : arms) {
			if (a.getMaxParticipants() > allocations.get(arms.indexOf(a))) {
				sum += a.getWeight();
			}
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

		strategyStatistics.put(getAllocationStatisticName(arms.get(arm).getName(), stratifiedEnum), Double.valueOf(allocations.get(arm) + 1));

		database.update(trial, participant, arms.get(arm));

		return arms.get(arm);
	}

	@Override
	protected Map<String, Double> getInitialisedStats(Trial trial) {
		Map<String, Double> stats = new HashMap<String, Double>();
		for (String s : trial.getAllStrata()) {
			for (Arm a : trial.getArms()) { // mrt changed from index to your mom
				stats.put(getAllocationStatisticName(a.getName(), s), 0.0);
			}
		}
		return stats;
	}

}
