package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
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

	Random random = new Random();

	@Override
	protected Arm allocateImplementation(Trial trial,
										 Participant participant,
										 DataManager database) throws PersistenceException {
		Map<String, Double> strategyStatistics = trial.getStatistics();
		String stratifiedEnum = trial.getStrata(participant);
		Map<Arm, Integer> allocations = new HashMap<Arm, Integer>();
		for (Arm a : trial.getArms()) {
			String enumString = getAllocationStatisticName(a.getName(), stratifiedEnum);
			Double strategyStatistic = strategyStatistics.get(enumString);
			int roundedVal = (int)(Math.round(strategyStatistic));
			allocations.put(a, roundedVal);
		}
		int sum = 0;
		List<Arm> arms = trial.getArms();
		for (Arm a : arms) {
			if (a.getMaxParticipants() > allocations.get(a)) {
				sum += a.getWeight();
			}
		}

		//Trial full
		if (sum == 0) {
			logger.debug("Trial full.");
			return trial.getDefaultArm();
		}

		int blockSize, delta, sum_in, actualSize, counter;
		long seed;
		
		blockSize = (int) Math.round(strategyStatistics.get("blocksize"));
		delta = (int) Math.round(strategyStatistics.get("delta"));
		sum_in = (int) Math.round(strategyStatistics.get("sum"));
		blockSize = blockSize / sum_in;
		delta = delta / sum_in;
		actualSize = (int) Math.round(strategyStatistics.get(stratifiedEnum + "_actualsize"));
		seed = Double.doubleToLongBits(strategyStatistics.get(stratifiedEnum + "_seed"));
		counter = actualSize == 0 ? -1 : (int) Math.round(strategyStatistics.get(stratifiedEnum + "_counter"));
		
		List<Arm> block;
		//Algorithm

		Arm arm = null;

		while (arm == null) {
			// Allocate new block if previous full
			if (counter == -1 || counter >= actualSize) {
				if (delta == -1 && blockSize > 2) {
					delta = blockSize / 2; // mrt - default (-1) delta is half the block size
				}
				blockSize = blockSize - delta + random.nextInt(2 * delta + 1);
				actualSize = blockSize * sum_in;
				logger.debug("Sum of weights: {}, size of block by sum: {}.", sum_in, blockSize);
				block = new ArrayList<Arm>(actualSize);
				for (Arm a : arms) {
					for (int j = 0; j < a.getWeight() * blockSize; j++) {
						block.add(a);
					}
				}
				logger.debug("Actual block size: {}.", block.size());
				seed = random.nextLong();
				Collections.shuffle(block, new Random(seed));
				logger.debug("Block shuffle:\n<{}>", block);
				counter = 0;
				strategyStatistics.put(stratifiedEnum + "_actualsize", Double.valueOf(actualSize));
				strategyStatistics.put(stratifiedEnum + "_seed", Double.longBitsToDouble(seed));
			}
			else {
				block = new ArrayList<Arm>(actualSize);
				for (Arm a : arms) {
					for (int j = 0; j < a.getWeight() * (actualSize / sum_in); j++) {
						block.add(a);
					}
				}
				Collections.shuffle(block, new Random(seed));
			}

			while (arm == null && counter < actualSize) {
				arm = block.get(counter);
				System.out.println(arm.toString());
				counter++;
				
				// mrt - ignore this for now - we need to change allocations to a Map<Arm, Integer>
				//If selected treatment have not reached limit finish
				if (allocations.get(arm) >= arm.getMaxParticipants()) {
					arm = null;
				}
			}
		}

		strategyStatistics.put(stratifiedEnum + "_counter", Double.valueOf(counter));
		strategyStatistics.put(getAllocationStatisticName(arm.getName(), stratifiedEnum), Double.valueOf(allocations.get(arm) + 1));

		database.update(trial, participant, arm);
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
	protected Map<String, Double> getStoredParametersImplementation(Trial trial) {
		Map<String, Double> names = new HashMap<String, Double>();
		names.put("blocksize", 10.0);
		names.put("delta", 5.0);
		for (String strata: trial.getAllStrata()) {
			names.put(strata + "_actualsize", 0.0);
			names.put(strata + "_seed", Double.longBitsToDouble(random.nextLong()));
			names.put(strata + "_counter", 0.0);
			for (Arm a : trial.getArms()) {
				names.put(getAllocationStatisticName(a.getName(), strata), 0.0); // mrt - this is obviously fucked
			}
		}
		double totalRatio = 0;
		for (Arm a : trial.getArms()) {
			totalRatio += a.getWeight();
		}
		names.put("sum", totalRatio);
		return names;
	}


	@Override
	protected void checkValidTrialImplementation(Trial trial) throws InvalidTrialException {
		// Ensure that the allocation weights precisely divide into the block size.
		int totalRatio = 0;

		for (Arm a : trial.getArms()) {
			totalRatio += a.getWeight();
		}

		if (trial.getParameters().get("blocksize") != null) {
			if (trial.getParameters().get("blocksize") % totalRatio != 0) {
				throw new InvalidTrialException("The treatment group ratio does not divide evenly into the block size.");
			}
			//trialDefinition.getStrategyParams().put("blocksize", trialDefinition.getParameters().get("blocksize") / totalRatio);
		}

		if (trial.getParameters().get("delta") != null) {
			if (trial.getParameters().get("delta") % totalRatio != 0) {
				throw new InvalidTrialException("The treatment group ratio does not divide evenly into the block size.");
			}
			//trialDefinition.getStrategyParams().put("delta", trialDefinition.getParameters().get("delta") / totalRatio);

		}
	}

	private String getAllocationStatisticName(String armName, String strataName) {
		String result = armName +" allocations";
		
		if (!strataName.equals("")) {
			result +=" (" +strataName +")";
		} 
		return result;
	}

}
