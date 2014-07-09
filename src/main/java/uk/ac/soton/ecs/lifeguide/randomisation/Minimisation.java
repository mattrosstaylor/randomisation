package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;
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
 * <b>certainty</b> - Floating point value between 0.0 to 1.0, indicating the probability that minimisation
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
	Statistics stats = null;

	public Minimisation() {
	}

	public Minimisation(TrialDefinition trialDefinition) {
		this.stats = resetStatistics(trialDefinition, new StrategyStatistics());
	}

	private Statistics resetStatistics(TrialDefinition trialDefinition, StrategyStatistics stats) {

		for (Treatment treatment : trialDefinition.getTreatments()) {
			for (Attribute attribute : trialDefinition.getAttributes()) {
				for (int i = 0; i < attribute.getGroupCount(); i++) {
					String put_string = new String(treatment.getName() + attribute.getAttributeName() + i);
					stats.putStatistic(put_string, 0.0f);
				}
			}
		}
		this.stats.putStatistic(keywordProbabilistic,1.0000000f);
		return this.stats;
	}

	private Statistics getStats(TrialDefinition trialDefinition, DBConnector dbConnector) throws SQLException{
		Statistics stats1 = null;
		if (dbConnector != null) {
			stats1 = dbConnector.getStrategyStatistics(trialDefinition);
		} else {
			stats1 = this.stats;
//			logger.error("No database connector specified, will throw exception unless this is testing\n");
		}
		return stats1;
	}

	public void put_stat(StrategyStatistics stats) {
		this.stats = stats;
	}

	public int getMinIntervention(double[] score) {
		if(score.length <= 0) {
			logger.error("Minimisation method got no scores to choose from\n");
		}
		ArrayList<Integer> listOfSameMinValue = new ArrayList<Integer>();  //List that stores the index of those scores which have same value
		listOfSameMinValue.add(-1); //Add the error return value to the list if there is something wrong happening
		int ret_index = -1;    //The return index, -1 as default to indicate a fault, even though it will always be changed
		double min = Float.MAX_VALUE;  //Init min to be the maximum float value
		for(int i = 0; i < score.length;i++) {
			if(score[i] == min) {
				listOfSameMinValue.add(i);
			} else if(score[i] < min) {
				listOfSameMinValue.clear();   //New min value, so clear the list of min values
				listOfSameMinValue.add(i);    //Add the new index
				min = score[i];
			}
		}
		//If there is more than one value which have the smallest score, it will shuffle the list to get a random order of them.
		Collections.shuffle(listOfSameMinValue,new Random());
		//Return the 0th index as the chosen intervention
		ret_index = listOfSameMinValue.get(0);
		return ret_index;
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

	private int getProbIntervention(int index, double[] score, float prob) {
		int ret_val = index; //The index which have the probability prob to be chosen
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < score.length;i++) {
			if(i != index) {
				list.add(i);
			}
		}
		Random rand = new Random();
		if(!(prob+1E-9 >1.0) && rand.nextFloat() > prob && haveNonInfinite(score)) {
			do {
			Collections.shuffle(list,rand);
			ret_val = list.get(0);
			}while(Double.isInfinite(score[ret_val]));
		}
		return ret_val;
	}

	public boolean canAllocateToTreatment(TrialDefinition trialDefinition, Treatment treatment,Statistics stats) {
		boolean ret_val = true;
		if(treatment.hasParticipantLimit()) {
		   ret_val = false;
		}
		//if no limit
		if(ret_val == true) {
			return ret_val;
		}


		List<Attribute> attributes = trialDefinition.getAttributes();
		Attribute attribute = attributes.get(0);
		int groups = attribute.getGroupCount();
		float score = 0;
		for(int i = 0; i < groups;i++) {
			 String get_string =  treatment.getName()+attribute.getAttributeName()+i;
			if(stats.getStatistic(get_string) != null) {
				score += stats.getStatistic(get_string);
			}
		}
		if(((int)score) < treatment.getParticipantLimit()) {
			ret_val = true;
		}
		logger.debug("Treatment "+treatment.getName()+" has "+score+" participants\n");
		return ret_val;
	}

	private String getStrataString(TrialDefinition trialDefinition, Participant participant) {
		String strata = "";

		if(trialDefinition.getStratifiedCount() > 1) {
			strata= Integer.toString(trialDefinition.getStratifiedEnumeration(participant));
		}
		return strata;
	}

	private String getStratStatString(String strata,Treatment treatment, Attribute attr,Float index) {
		String ret_str =strata+treatment.getName()+attr.getAttributeName();
		    if(index != null) {
		      ret_str+=attr.getGroupIndex(index);
		    }
		return ret_str;
	}

	public int getAllocation(TrialDefinition trialDefinition,Participant participant, DBConnector dbConnector) throws AllocationException  {
		List<Treatment> treatments = trialDefinition.getTreatments();

		Map<String,Float> responses = participant.getResponses();

		double[] score = new double[treatments.size()];
		if(treatments.size() <= 0) {
			logger.error("Allocation method could not encounter any treatments\n");
		}
		int index = 0;

		//StrategyStatistics stats = getStats(trialDefinition,participant,dbConnector);

		for(Treatment treatment: treatments) {
			score[index] = 0.0f;

			for(String attrs:responses.keySet()) {
				Attribute attr = trialDefinition.getAttributeByName(attrs);

				String get_string = getStratStatString(getStrataString(trialDefinition,participant),treatment,attr,responses.get(attrs));

				if(this.stats.getStatistic(get_string) != null) {
					score[index] += this.stats.getStatistic(get_string)*attr.getWeight();
				}
			}
			score[index] /= treatment.getWeight();
			String logOutput ="Score "+score[index]+ " index " +index+" for treatment: "+treatment.getName()+"\n";
			logger.debug(logOutput);
			if(canAllocateToTreatment(trialDefinition,treatment,stats) == false) {
				score[index] = Double.POSITIVE_INFINITY;
			}
			index++;

		}
		index = getMinIntervention(score);
		int old = index;
		if(index == -1) {
			logger.error("Minimisation returned -1, no intervention chosen\n");
			throw new AllocationException("Error no intervention chosen for participant, likely limits for groups have been met");
		} else {
			logger.debug("Minimisation determined " + treatments.get(index).getName() +" to be the most suitable treatment\n");
		}
		Float probabilistic = stats.getStatistic(keywordProbabilistic);
		if(probabilistic == null) {
			probabilistic = 1.00000f;
		}

		index = getProbIntervention(index,score,probabilistic);//The probabilistic choosing method

		if (index == -1) {
			logger.error("Probabilistic method returned -1, no intervention chosen\n");
		} else {

			if (index != old) {
				logger.debug("The probabilistic method did not choose the optimal treatment p=" + this.stats.getStatistic(keywordProbabilistic) + "\n");
				logger.debug("Instead of choosing treatment " + treatments.get(old).getName() + " it chose " + treatments.get(index).getName() + "\n");
			}
		}

		return index;
	}

	@Override
	protected int allocateImplementation(TrialDefinition trialDefinition, Participant participant, DBConnector dbConnector) throws AllocationException {


		for(Attribute attr : trialDefinition.getAttributes()) {
			if(attr.isGroupingFactor()) {
				if(participant.getResponses().get(attr.getAttributeName()) == null) {
					  logger.error(" Participants are missing "+ attr.getAttributeName() +" in their response data ");
					throw new AllocationException("Participants are missing "+ attr.getAttributeName() +" in their response data ");
				}
			}
		}




		 try {
			 this.stats = getStats(trialDefinition, dbConnector);
			} catch (SQLException e) {
				throw new AllocationException("SQL Exception when loading statistics: " + e.getMessage());
			}

		int index = getAllocation(trialDefinition, participant, dbConnector);
		if (index <= -1) {
			logger.error("Allocation method returned -1, no intervention chosen for participant " + participant.getId() + " in intervention" + trialDefinition.getTrialName() + "\n");

		}


		Treatment treatment = trialDefinition.getTreatments().get(index);
		for (String attrs : participant.getResponses().keySet()) {
			Attribute attr = trialDefinition.getAttributeByName(attrs);

			String put_string = getStratStatString(
				getStrataString(trialDefinition,participant),
				treatment,
				attr,
				participant.getResponses().get(attrs));

			//String put_string = treatment.getName() + attr.getAttributeName() + attr.getGroupIndex(participant.getResponses().get(attrs));
			if(stats.getStatistic(put_string) != null) {
				stats.putStatistic(put_string, stats.getStatistic(put_string) + 1.f);
			} else {
				stats.putStatistic(put_string, 1.f);
			}
		}

		if (dbConnector != null) {
			try {
				dbConnector.update(trialDefinition, participant, this.stats, index);
			} catch (SQLException e) {
				throw new AllocationException("SQL Exception when updating statistics: " + e.getMessage());
			}
		}



		return index;
	}

	/**
	 * The method is called statically trough {@link #getRequiredParameters(Class)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be provided in the {@link uk.ac.soton.ecs.lifeguide.randomisation.TrialDefinition} object.
	 */
	@Override
	protected List<String> getRequiredParametersImplementation() {
		List<String> ret_val = new LinkedList<String>();
		ret_val.add(keywordProbabilistic);
		return ret_val;
	}

	/**
	 * The method is called statically trough {@link #getStoredParameters(Class, TrialDefinition)}  and
	 * should not be called in any other way.
	 *
	 * @return The names of all the parameters for that class that need to be stored on the data source, mapped to their
	 *         default values.
	 */
	@Override
	protected Map<String, Float> getStoredParametersImplementation(TrialDefinition trialDefinition) {
		Map<String, Float> ret_val = new HashMap<String, Float>();
		String strata = "";
		for(int s = 0; s < trialDefinition.getStratifiedCount(); s++) {
		if(trialDefinition.getStratifiedCount() > 1) {
			strata = Integer.toString(s);
		}
		for (Treatment treatment : trialDefinition.getTreatments()) {
			for (Attribute attribute : trialDefinition.getAttributes()) {
				for (int i = 0; i < attribute.getGroupCount(); i++) {

					String put_string = getStratStatString(
					strata,
					treatment,
					attribute,
					null)
						+i;
					ret_val.put(put_string, 0.0f);
				}
			}
		}
		}
		ret_val.put(keywordProbabilistic, 1.0f);
		return ret_val;
	}

	@Override
	protected void checkValidTrialImplementation(TrialDefinition trialDefinition) throws InvalidTrialException {
	}
}