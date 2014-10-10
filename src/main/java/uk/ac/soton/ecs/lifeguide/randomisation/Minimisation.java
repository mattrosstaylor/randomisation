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
	//Map<String, Double> stats = null;

	public Minimisation() {
	}
/*
	public Minimisation(Trial trial) {
		this.stats = resetMap<String, Double>(trial, new StrategyMap<String, Double>());
	}

	private Map<String, Double> resetMap<String, Double>(Trial trial, StrategyMap<String, Double> stats) {

		for (Arm arm : trial.getArms()) {
			for (Attribute attribute : trial.getAttributes()) {
				for (int i = 0; i < attribute.getGroupCount(); i++) {
					String put_string = new String(arm.getName() + attribute.getName() + i);
					stats.putStatistic(put_string, 0.0f);
				}
			}
		}
		this.stats.putStatistic(keywordProbabilistic,1.0000000f);
		return this.stats;
	}

	private Map<String, Double> getStats(Trial trial, DataManager database) throws SQLException{
		Map<String, Double> stats1 = null;
		if (database != null) {
			stats1 = database.getStrategyMap<String, Double>(trial);
		} else {
			stats1 = this.stats;
//			logger.error("No database connector specified, will throw exception unless this is testing\n");
		}
		return stats1;
	}

	public void put_stat(StrategyMap<String, Double> stats) {
		this.stats = stats;
	}
*/

	public Arm getMinIntervention(Map<Arm, Double> scores) {
		
		ArrayList<Arm> listOfSameMinValue = new ArrayList<Arm>();  //List that stores the index of those scores which have same value
		listOfSameMinValue.add(null); //Add the error return value to the list if there is something wrong happening
		double min = Double.MAX_VALUE;  //Init min to be the maximum double value
		
		for (Arm a: scores.keySet()) {
			if(scores.get(a) == min) {
				listOfSameMinValue.add(a);
			} 
			else if(scores.get(a) < min) {
				listOfSameMinValue.clear();   //New min value, so clear the list of min values
				listOfSameMinValue.add(a);    //Add the new index
				min = scores.get(a);
			}
		}
		//If there is more than one value which have the smallest score, it will shuffle the list to get a random order of them.
		Collections.shuffle(listOfSameMinValue,new Random());
		//Return the 0th index as the chosen intervention
		return listOfSameMinValue.get(0);
	}

	private boolean haveNonInfinite(double [] score) {
		boolean ret_val = false;

		for(int i = 0; i < score.length;i++) {
			   if(Double.isInfinite(score[i]) == false) {
				   ret_val = true;
			   }
		}
		return ret_val;
	}

	private int getProbIntervention(int index, double[] score, double prob) {
		int ret_val = index; //The index which have the probability prob to be chosen
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < score.length;i++) {
			if(i != index) {
				list.add(i);
			}
		}
		Random rand = new Random();
		if(!(prob+1E-9 >1.0) && rand.nextDouble() > prob && haveNonInfinite(score)) {
			do {
			Collections.shuffle(list,rand);
			ret_val = list.get(0);
			}while(Double.isInfinite(score[ret_val]));
		}
		return ret_val;
	}

	public boolean canAllocateToArm(Trial trial, Arm arm) {
		boolean ret_val = true;
		if(arm.getParticipantLimit()) {
		   ret_val = false;
		}
		//if no limit
		if(ret_val == true) {
			return ret_val;
		}


		List<Attribute> attributes = trial.getAttributes();
		Attribute attribute = attributes.get(0);
		int groups = attribute.getNumberOfGroups();
		double score = 0;
		for(int i = 0; i < groups;i++) {
			String get_string =  arm.getName()+attribute.getName()+i;
			if(trial.getStatistics().get(get_string) != null) {
				score += trial.getStatistics().get(get_string);
			}
		}
		if(((int)score) < arm.getMaxParticipants()) {
			ret_val = true;
		}
		logger.debug("Arm "+arm.getName()+" has "+score+" participants\n");
		return ret_val;
	}

/*
	private String getStrataString(Trial trial, Participant participant) {
		String strata = "";

		if(trial.getStratifiedCount() > 1) {
			strata= Integer.toString(trial.getStratifiedEnumeration(participant));
		}
		return strata;
	}
*/
	// mrt - strata is redundant
	private String getStratStatString(String strata,Arm arm, Attribute attr, Double value) {
		//return arm.getName()+ " " +attr.getName() + attr.getGroupingNameForValue(value);
		return "(" +attr.getName() +" " +attr.getGroupingNameForValue(value) +") " +arm.getName();
	}



	public Arm getAllocation(Trial trial,Participant participant, DataManager database) {
		List<Arm> arms = trial.getArms();

		Map<Arm, Double> scores = new HashMap<Arm, Double>();

		//StrategyMap<String, Double> stats = getStats(trial,participant,database);

		for (Arm arm: arms) {
			scores.put(arm, 0.0);

			for (Attribute attr: trial.getAttributes()) {

				String get_string = getStratStatString(
					trial.getStrata(participant),
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

			//String logOutput ="Score "+score[index]+ " index " +index+" for arm: "+arm.getName()+"\n";
			//logger.debug(logOutput);
			

			// mrt - this is probably gay
		//	if(canAllocateToArm(trial,arm,stats) == false) {
		//		score[index] = Double.POSITIVE_INFINITY;
		//	}

		}

		Arm minimalArm = getMinIntervention(scores);

		return minimalArm;

		// mrt - removing probablistic bullshit
/*

		int old = index;
		if(index == -1) {
			logger.error("Minimisation returned -1, no intervention chosen\n");
			throw new AllocationException("Error no intervention chosen for participant, likely limits for groups have been met");
		} else {
			logger.debug("Minimisation determined " + arms.get(index).getName() +" to be the most suitable arm\n");
		}
		Double probabilistic = stats.getStatistic(keywordProbabilistic);
		if(probabilistic == null) {
			probabilistic = 1.00000f;
		}

		index = getProbIntervention(index,score,probabilistic);//The probabilistic choosing method

		if (index == -1) {
			logger.error("Probabilistic method returned -1, no intervention chosen\n");
		} else {

			if (index != old) {
				logger.debug("The probabilistic method did not choose the optimal arm p=" + this.stats.getStatistic(keywordProbabilistic) + "\n");
				logger.debug("Instead of choosing arm " + arms.get(old).getName() + " it chose " + arms.get(index).getName() + "\n");
			}
		}

		return index;
*/
	}

	@Override
	protected Arm allocateImplementation(Trial trial, Participant participant, DataManager database) throws PersistenceException {

		// mrt - this is just an error check - our data is legit, son!
		/*	for(Attribute attr : trial.getAttributes()) {
			if(attr.isGroupingFactor()) {
				if(participant.getResponses().get(attr.getName()) == null) {
					  logger.error(" Participants are missing "+ attr.getName() +" in their response data ");
					throw new PersistenceException("Participants are missing "+ attr.getName() +" in their response data ");
				}
			}
		}
		*/


		Arm arm = getAllocation(trial, participant, database);

		for (Attribute attr: trial.getAttributes()) {
			String put_string = getStratStatString(
				trial.getStrata(participant),
				arm,
				attr,
				participant.getResponse(attr.getName()));

			if(trial.getStatistics().get(put_string) != null) {
				trial.getStatistics().put(put_string, trial.getStatistics().get(put_string) + 1.0);
			} else {
				trial.getStatistics().put(put_string, 1.0);
			}
		}
		
		database.update(trial, participant, arm);



		//return index; // mrt - fuck youuuuuu!
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
		for (String strata: trial.getAllStrata()) {
			for (Arm arm : trial.getArms()) {
				for (Attribute attribute : trial.getAttributes()) {
					for (Grouping g : attribute.getGroupings()) {
						String put_string = getStratStatString(strata, arm, attribute, g.getMinimum());
						ret_val.put(put_string, 0.0);
					}
				}
			}
		}
		//ret_val.put(keywordProbabilistic, 1.0);
		return ret_val;
	}

	@Override
	protected void checkValidTrialImplementation(Trial trial) throws InvalidTrialException {
	}
}