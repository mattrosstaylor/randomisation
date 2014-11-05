package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Implementation for the taves minimisation algorithm.
 * <p>Minimisation is an adaptive sampling technique first described by Taves in 1974
 * and shortly after by Pocock and Simon in 1975. Its goal is to minimise the
 * imbalance between groups based on several prognostic factors.
 * Given a new participant which must be allocated, it will look at the
 * participant's prognostic factors, and for each group it calculates a sum based
 * on these factors. It will then allocate the participant to the group which has
 * the lowest sum of them all. The sum calculated for each group can be regarded as,
 * for each factor, how many participants in the group have the same factor.
 * Thus, allocating to the group with the least sum indicates that the group has the
 * lowest number of individuals who are similar to the participant to be allocated with regard to the prognostic factors.</p>
 * <p>As this method is completely deterministic it have been criticised that it completely
 * removes the blinding as a person with the knowledge of previous allocations can predict,
 * given a participant, where that participant will be allocated, to solve this, the outcome can be set
 * to be probabilistic, meaning given probability p, there is p chance that minimisation will allocate
 * the participant to the most suitable group, otherwise it will allocate the participant to one of the
 * non-optimal remaining groups.</p>
 * <b>Optional parameters:</b>
 * <p>certainty
 * <b>certainty</b> - Doubleing point value between 0.0 to 1.0, indicating the probability that minimisation
 * allocates to the optimal group, * default value is 1.0.
 * </p>
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see Strategy
 * @since 1.7
 */
public class Minimisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(Minimisation.class);

	final String keywordProbabilistic = "certainty";
	private Random rand = new Random();

	public Arm getAllocatedArm(Trial trial, Map<Arm, Double> scores) {
		
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
		if (rand.nextDouble() > trial.getParameters().get(keywordProbabilistic)) {
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

		return optionArms.get(new Random().nextInt(optionArms.size()));
	}

	private String getStratStatString(Arm arm, Attribute attr, Double value) {
		return "(" +attr.getName() +" " +attr.getGroupingNameForValue(value) +") " +arm.getName();
	}

	public Arm getAllocation(Trial trial,Participant participant, DataManager database) {
		List<Arm> arms = trial.getArms();

		Map<Arm, Double> scores = new HashMap<Arm, Double>();

		for (Arm arm: arms) {
			if (trial.getStatistics().get(getAllocationStatisticName(arm.getName(), "")) < arm.getMaxParticipants()) {

				scores.put(arm, 0.0);

				for (Attribute attr: trial.getAttributes()) {

					String get_string = getStratStatString(
						arm,
						attr, 
						participant.getResponse(attr.getName())
					);
					Double stat = trial.getStatistics().get(get_string);

					if (stat != null) {
						scores.put(arm, scores.get(arm) + stat*attr.getWeight());
					}
				}
				scores.put(arm, scores.get(arm)/arm.getWeight());
			}
		}

		Arm allocatedArm = getAllocatedArm(trial, scores);

		return allocatedArm;
	}

	@Override
	protected Arm allocateImplementation(Trial trial, Participant participant, DataManager database) throws PersistenceException {

		Arm arm = getAllocation(trial, participant, database);

		for (Attribute attr: trial.getAttributes()) {
			String put_string = getStratStatString(
				arm,
				attr,
				participant.getResponse(attr.getName()));

			trial.getStatistics().put(put_string, trial.getStatistics().get(put_string) + 1.0);
		}

		trial.getStatistics().put(
			getAllocationStatisticName(arm.getName(),""),
			trial.getStatistics().get(getAllocationStatisticName(arm.getName(),"")) + 1.0
		);
		
		database.update(trial, participant, arm);

		return arm;
	}

	/**
	 * The method is called statically trough {@link #getRequiredParameters(Class)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be provided in the {@link uk.ac.soton.ecs.lifeguide.randomisation.Trial} object.
	 */
	@Override
	protected List<String> getRequiredParametersImplementation() {
		List<String> ret_val = new LinkedList<String>();
		ret_val.add(keywordProbabilistic);
		return ret_val;
	}

	/**
	 * The method is called statically trough {@link #getStoredParameters(Class, Trial)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be stored on the data source, mapped to their
	 *         default values.
	 */
	@Override
	protected Map<String, Double> getStoredParametersImplementation(Trial trial) {
		Map<String, Double> ret_val = new HashMap<String, Double>();
		for (Arm arm : trial.getArms()) {
			ret_val.put(getAllocationStatisticName(arm.getName(), ""), 0.0);
			for (Attribute attribute : trial.getAttributes()) {
				for (Grouping g : attribute.getGroupings()) {
					String put_string = getStratStatString(arm, attribute, g.getMinimum());
					ret_val.put(put_string, 0.0);
				}
			}
		}
		return ret_val;
	}

	@Override
	protected void checkValidTrialImplementation(Trial trial) throws InvalidTrialException {
	}
}