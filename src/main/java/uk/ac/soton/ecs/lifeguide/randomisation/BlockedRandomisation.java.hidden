package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Implementation for the blocked randomisation algorithm.
 * <p>The algorithm assigns blocks of given size and within that block it guarantees
 * that the participants would be equally distributed. If the treatments arms have
 * weights the distribution within a block will reflect this factor.
 * The block size is not necessarily fixed, but in fact can be probabilistic too.
 * This removes extra the bias of being able to predict the last participant allocation
 * if the block size is known prior and knowing the number of participants so far.</p>
 * <b>Required parameters:</b>
 * <p>
 * <b>blocksize</b> - the size of single block.</br>
 * </p>
 * <p>
 * <b>delta</b> - the variation of the generated block sizes. </br>
 * </p>
 * <b>Stored parameters:</b>
 * <p>
 * <b>sum</b> - the size of the weights at the begging of the trial. This are used in order to preserve consistent
 * block sizes even after some group have reached it's limit. In order to this if we take k=blocksize/sum, then
 * when a group reach its limit we adjust the actualsize = k*(sum of weights of non filled allocation groups) </b>
 * </p>
 * <b>Additional stored parameters per stratified group 'i':</b>
 * <p>
 * <b>i_actualsize</b> - the actual block size of the current block in which allocation is done. In multiples of sum of weights.</br>
 * <b>i_counter</b> - the position in which we are in the current block. </br>
 * <b>i_seed</b> - the seed used for the current block used for shuffling.
 * <b>i_j_allocation</b> - the number of people allocated to treatment 'j'.
 * </p>
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see Strategy
 * @since 1.7
 */
public class BlockedRandomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(BlockedRandomisation.class);

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
		int stratifiedEnum = -1;
		try{
			stratifiedEnum = trialDefinition.getStratifiedEnumeration(participant);
		} catch(NullPointerException e){
			throw new AllocationException("Participant could not be allocated. Missing responses for key attributes.");
		}

		List<Integer> allocations = new ArrayList<Integer>(trialDefinition.getTreatments().size());

		for (int i = 0; i < trialDefinition.getTreatments().size(); i++)
			allocations.add(Math.round(strategyStatistics.getStatistic(stratifiedEnum + "_" + i + "_allocation")));

		int arm = -1;
		int sum = 0;
		List<Treatment> treatments = trialDefinition.getTreatments();
		for (Treatment treatment : treatments)
			if (treatment.getParticipantLimit() > allocations.get(treatments.indexOf(treatment)))
				sum += treatment.getWeight();
		//Trial full
		if (sum == 0) {
			logger.debug("Trial full.");
			int defTreatment = trialDefinition.getDefaultTreatmentIndex();
			if(defTreatment == -1){
				throw new AllocationException("All intervention groups are full. Participant not allocated.");
			}
			return defTreatment;
		}
		int blockSize, delta, sum_in, actualSize, seed, counter;
		try {
			blockSize = (int) Math.round(strategyStatistics.getStatistic("blocksize"));
			delta = (int) Math.round(strategyStatistics.getStatistic("delta"));
			sum_in = Float.floatToRawIntBits(strategyStatistics.getStatistic("sum"));
			blockSize = blockSize / sum_in;
			delta = delta / sum_in;
			actualSize = (int) Math.round(strategyStatistics.getStatistic(stratifiedEnum + "_actualsize"));
			seed = Float.floatToRawIntBits(strategyStatistics.getStatistic(stratifiedEnum + "_seed"));
			counter = actualSize == 0 ? -1 : (int) Math.round(strategyStatistics.getStatistic(stratifiedEnum + "_counter"));
		} catch (Exception e) {
			throw new AllocationException("Some of the stored parameters for blocked randomisation was not found in the statistics returned by the database.");
		}
		List<Integer> block;
		//Algorithm

		while (arm == -1) {
			//Allocate new block if previous full
			if (counter == -1 || actualSize <= counter) {
				if (delta == -1 && blockSize > 2)
					delta = blockSize / 2;
				blockSize = blockSize - delta + new Random().nextInt(2 * delta + 1);
				actualSize = blockSize * sum;
				logger.debug("Sum of weights: {}, size of block by sum: {}.", sum, blockSize);
				block = new ArrayList<Integer>(actualSize);
				for (int i = 0; i < treatments.size(); i++)
					for (int j = 0; j < treatments.get(i).getWeight() * blockSize; j++)
						if (allocations.get(i) - j + 1 <= treatments.get(i).getParticipantLimit())
							block.add(i);
				logger.debug("Actual block size: {}.", block.size());
				seed = new Random().nextInt();
				Collections.shuffle(block, new Random(seed));
				logger.debug("Block shuffle:\n<{}>", block);
				counter = 0;
				strategyStatistics.putStatistic(stratifiedEnum + "_actualsize", Float.valueOf(actualSize));
				strategyStatistics.putStatistic(stratifiedEnum + "_seed", Float.intBitsToFloat(seed));
			} else {
				block = new ArrayList<Integer>(actualSize);
				for (int i = 0; i < treatments.size(); i++)
					for (int j = 0; j < treatments.get(i).getWeight() * actualSize / sum; j++)
						if (allocations.get(i) - j + 1 <= treatments.get(i).getParticipantLimit())
							block.add(i);
				Collections.shuffle(block, new Random(seed));
			}
			while (true) {
				try {
					arm = block.get(counter);
				} catch (Exception e) {
					logger.error(e.getMessage());
					throw new AllocationException("Something went wrong when trying to assigning arm: " + e.getMessage());
				}
				counter++;
				//If selected treatment have not reached limit finish
				if (treatments.get(arm).getParticipantLimit() > allocations.get(arm))
					break;
				arm = -1;
				//If all selected treatments left in the block are full allocate new one
				if (counter >= actualSize)
					break;
			}
		}

		strategyStatistics.putStatistic(stratifiedEnum + "_counter", Float.valueOf(counter));
		strategyStatistics.putStatistic(stratifiedEnum + "_" + arm + "_allocation", Float.valueOf(allocations.get(arm) + 1));
		try {
			database.update(trialDefinition, participant, strategyStatistics, arm);
		} catch (SQLException e) {
			throw new AllocationException("SQL Exception when updating statistics: " + e.getMessage());
		}
		return arm;
	}


	@Override
	protected List<String> getRequiredParametersImplementation() {
		List<String> names = new ArrayList<String>(2);
		names.add("blocksize");
		names.add("delta");
		return names;
	}

	@Override
	protected Map<String, Float> getStoredParametersImplementation(TrialDefinition trialDefinition) {
		int treatments = trialDefinition.getTreatmentCount();
		int strata = trialDefinition.getStratifiedCount();
		Map<String, Float> names = new HashMap<String, Float>(2 + (3 + treatments) * strata);
		names.put("blocksize", 10f);
		names.put("delta", 5f);
		for (int i = 0; i < strata; i++) {
			names.put(i + "_actualsize", 0f);
			names.put(i + "_seed", Float.intBitsToFloat(new Random().nextInt()));
			names.put(i + "_counter", 0f);
			for (int j = 0; j < treatments; j++)
				names.put(i + "_" + j + "_allocation", 0f);
		}
		int totalRatio = 0;
		for (Treatment treatment : trialDefinition.getTreatments())
			totalRatio += treatment.getWeight();
		names.put("sum", Float.intBitsToFloat(totalRatio));
		return names;
	}


	@Override
	protected void checkValidTrialImplementation(TrialDefinition trialDefinition) throws InvalidTrialException {
		// Ensure that the allocation weights precisely divide into the block size.
		int totalRatio = 0;

		for (Treatment treatment : trialDefinition.getTreatments())
			totalRatio += treatment.getWeight();

		if (trialDefinition.getStrategyParam("blocksize") != null) {
			if (trialDefinition.getStrategyParam("blocksize") % totalRatio != 0)
				throw new InvalidTrialException("The treatment group ratio does not divide evenly into the block size.");
			//trialDefinition.getStrategyParams().put("blocksize", trialDefinition.getStrategyParam("blocksize") / totalRatio);
		}

		if (trialDefinition.getStrategyParam("delta") != null) {
			if (trialDefinition.getStrategyParam("delta") % totalRatio != 0)
				throw new InvalidTrialException("The treatment group ratio does not divide evenly into the block size.");
			//trialDefinition.getStrategyParams().put("delta", trialDefinition.getStrategyParam("delta") / totalRatio);

		}
	}

}
