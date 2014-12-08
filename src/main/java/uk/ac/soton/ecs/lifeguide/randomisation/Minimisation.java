package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class Minimisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(Minimisation.class);

	private Random rand = new Random();

	public Minimisation(Trial trial, DataManager database) {
		super(trial, database);
	}

	@Override
	protected Arm allocate(Participant participant) throws PersistenceException {

		List<Arm> arms = trial.getArms();
		Map<Arm, Double> scores = new HashMap<Arm, Double>();

		for (Arm arm: arms) {
			if (trial.getStatistics().get(getAllocationStatisticName(arm.getName(), "")) < arm.getMaxParticipants()) {

				scores.put(arm, 0.0);

				for (Variable variable: trial.getVariables()) {

					String get_string = getStratStatString(
						arm,
						variable, 
						participant.getResponse(variable.getName())
					);
					Double stat = trial.getStatistics().get(get_string);

					if (stat != null) {
						scores.put(arm, scores.get(arm) + stat*variable.getWeight());
					}
				}
				scores.put(arm, scores.get(arm)/arm.getWeight());
			}
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
		if (rand.nextDouble() > trial.getParameters().get("certainty")) {
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


		for (Variable variable: trial.getVariables()) {
			String put_string = getStratStatString(
				arm,
				variable,
				participant.getResponse(variable.getName()));

			trial.getStatistics().put(put_string, trial.getStatistics().get(put_string) + 1.0);
		}

		trial.getStatistics().put(
			getAllocationStatisticName(arm.getName(),""),
			trial.getStatistics().get(getAllocationStatisticName(arm.getName(),"")) + 1.0
		);
		
		database.update(trial, participant, arm);

		return arm;
	}


	private String getStratStatString(Arm arm, Variable variable, String value) {
		return "(" +variable.getName() +" " +variable.getStratumNameForValue(value) +") " +arm.getName();
	}


	@Override
	protected Map<String, Double> getInitialisedStats(Trial trial) {
		Map<String, Double> stats = new HashMap<String, Double>();
		for (Arm arm : trial.getArms()) {
			stats.put(getAllocationStatisticName(arm.getName(), ""), 0.0);
			for (Variable variable : trial.getVariables()) {
				for (Stratum s : variable.getStrata()) {
					String put_string = getStratStatString(arm, variable, s.getValidValue());
					stats.put(put_string, 0.0);
				}
			}
		}
		return stats;
	}
}
