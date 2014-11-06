package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public abstract class Randomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(Randomisation.class);

	Random random = new Random();

	public Randomisation(Trial trial, DataManager database) {
		super(trial, database);
	}

	@Override
	protected Arm allocate(Participant participant) throws PersistenceException {
		String stratifiedEnum = trial.getStrata(participant);

		Map<Arm, Integer> allocations = new HashMap<Arm, Integer>();
		List<Arm> openArms = new ArrayList<Arm>();
	
		for (Arm a : trial.getArms()) {
			String enumString = getAllocationStatisticName(a.getName(), stratifiedEnum);
			Double strategyStatistic = statistics.get(enumString);
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

		Arm arm = allocateHelper(stratifiedEnum, openArms, allocations);

		statistics.put(getAllocationStatisticName(arm.getName(), stratifiedEnum), Double.valueOf(allocations.get(arm) + 1));
		database.update(trial, participant, arm);
		return arm;
	}

	protected abstract Arm allocateHelper(String stratifiedEnum, List<Arm> openArms, Map<Arm, Integer> allocations);
}