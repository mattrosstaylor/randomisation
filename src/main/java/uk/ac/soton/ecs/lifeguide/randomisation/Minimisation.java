package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class Minimisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(Minimisation.class);

	public Minimisation(Trial trial, DataManager database) {
		super(trial, database);
	}

	@Override
	protected Arm allocateHelper(Participant participant, String stratifiedEnum, List<Arm> openArms, Map<Arm, Integer> allocations) {

		List<Arm> arms = trial.getArms();
		Map<Arm, Double> scores = new HashMap<Arm, Double>();

		for (Arm arm: openArms) {
			scores.put(arm, 0.0);

			for (Variable variable: trial.getVariablesByType("minimisation")) {
				Double stat = getMinimisationStatistic(stratifiedEnum, arm, variable,participant.getResponse(variable.getName()));

				if (stat != null) {
					scores.put(arm, scores.get(arm) + stat*variable.getWeight());
				}
			}
			scores.put(arm, scores.get(arm)/arm.getWeight());
		}

		List<Arm> smallestArms = new ArrayList<Arm>();  //List that stores the index of those scores which have same value
		smallestArms.add(null); //Add the error return value to the list if there is something wrong happening
		double min = Double.MAX_VALUE;  //Init min to be the maximum double value
		
		for (Arm a: scores.keySet()) {
			if(scores.get(a) == min) {
				smallestArms.add(a);
			} 
			else if(scores.get(a) < min) {
				smallestArms.clear();   //New min value, so clear the list of min values
				smallestArms.add(a);    //Add the new index
				min = scores.get(a);
			}
		}

		List<Arm> optionArms;
		if (random.nextDouble() > parameters.get("certainty")) {
			optionArms = new ArrayList<Arm>();
			for (Arm a: scores.keySet()) {
				if (!smallestArms.contains(a)){
					optionArms.add(a);
				}
			}
			if (optionArms.size() == 0){
				optionArms = smallestArms;
			}
		}
		else {
			optionArms = smallestArms;
		}

		Arm arm = optionArms.get(new Random().nextInt(optionArms.size()));

		for (Variable variable: trial.getVariablesByType("minimisation")) {
			incrementMinimisationStatistic(stratifiedEnum, arm, variable, participant.getResponse(variable.getName()));
		}

		return arm;
	}

	private String getMiniString(String strataName, Arm arm, Variable variable, String value) {
		return getStatisticString(
			strataName,
			variable.getName() +" " +variable.getStratumNameForValue(value) +" |",
			arm.getName() +" count"
			);
	}

	private void incrementMinimisationStatistic(String strataName, Arm arm, Variable variable, String value) {
		String name = getMiniString(strataName, arm, variable, value);

		Double currentValue = 0.0;
		if (parameters.containsKey(name)) {
			currentValue = parameters.get(name);	
		}
		parameters.put(name,currentValue+1.0);
	}

	private Double getMinimisationStatistic(String strataName, Arm arm, Variable variable, String value) {
		String name = getMiniString(strataName, arm, variable, value);
		if (parameters.containsKey(name)) {
			return parameters.get(name);	
		}
		else {
			return 0.0;
		}
	}
}
