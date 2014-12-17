package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimpleRandomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(SimpleRandomisation.class);

	public SimpleRandomisation(Trial trial, DataManager database) {
		super(trial, database);
	}

	protected Arm allocateHelper(Participant participant, String stratifiedEnum, List<Arm> openArms, Map<Arm, Integer> allocations) {
		int openArmsWeightSum = 0;
		for (Arm openArm: openArms) {
			openArmsWeightSum += openArm.getWeight();
		}

		int roll = random.nextInt(openArmsWeightSum);
		Arm arm = null;
		Iterator<Arm> armIter = openArms.iterator();

		do {
			arm = armIter.next();
			roll -= arm.getWeight();
		} while (roll >= 0);

		return arm;
	}
}
