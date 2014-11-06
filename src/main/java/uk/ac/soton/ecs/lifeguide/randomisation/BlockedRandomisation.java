package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class BlockedRandomisation extends Strategy {

	private static final Logger logger = LoggerFactory.getLogger(BlockedRandomisation.class);

	Random random = new Random();

	@Override
	protected Arm allocate(Trial trial, Participant participant, DataManager database) throws PersistenceException {
		Map<String, Double> strategyStatistics = trial.getStatistics();
		Map<String, Double> trialParameters = trial.getParameters();
		String stratifiedEnum = trial.getStrata(participant);

		Map<Arm, Integer> allocations = new HashMap<Arm, Integer>();
		for (Arm a : trial.getArms()) {
			String enumString = getAllocationStatisticName(a.getName(), stratifiedEnum);
			Double strategyStatistic = strategyStatistics.get(enumString);
			int roundedVal = (int)(Math.round(strategyStatistic));
			allocations.put(a, roundedVal);
		}
		int activeSum = 0, sum = 0;
		List<Arm> arms = trial.getArms();
		for (Arm a : arms) {
			if (a.getMaxParticipants() > allocations.get(a)) {
				activeSum += a.getWeight();
			}
			sum += a.getWeight();
		}

		if (activeSum == 0) {
			logger.debug("Trial full.");
			return trial.getDefaultArm();
		}

		int blockSize, delta, sum_in, actualSize, counter;
		long seed;
		
		blockSize = (int) Math.round(trialParameters.get("blocksize"));
		delta = (int) Math.round(trialParameters.get("delta"));
		blockSize = blockSize / sum;
		delta = delta / sum;
		actualSize = (int) Math.round(strategyStatistics.get(getStatisticName("size", stratifiedEnum)));
		seed = Double.doubleToLongBits(strategyStatistics.get(getStatisticName("seed", stratifiedEnum)));
		counter = actualSize == 0 ? -1 : (int) Math.round(strategyStatistics.get(getStatisticName("counter", stratifiedEnum)));
		
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
				actualSize = blockSize * sum;
				logger.debug("Sum of weights: {}, size of block by sum: {}.", sum, blockSize);
				block = new ArrayList<Arm>(actualSize);
				for (Arm a : arms) {
					for (int j = 0; j < a.getWeight() * blockSize; j++) {
						block.add(a);
					}
				}
				logger.debug("Actual block size: {}.", block.size());
				double serialisedSeed = random.nextDouble();
				seed = Double.doubleToLongBits(serialisedSeed);
				Collections.shuffle(block, new Random(seed));
				logger.debug("Block shuffle:\n<{}>", block);
				counter = 0;
				strategyStatistics.put(getStatisticName("size", stratifiedEnum), Double.valueOf(actualSize));
				strategyStatistics.put(getStatisticName("seed", stratifiedEnum), serialisedSeed);
			}
			else {
				block = new ArrayList<Arm>(actualSize);
				for (Arm a : arms) {
					for (int j = 0; j < a.getWeight() * (actualSize / sum); j++) {
						block.add(a);
					}
				}
				Collections.shuffle(block, new Random(seed));
			}

			while (arm == null && counter < actualSize) {
				arm = block.get(counter);
				counter++;

				if (allocations.get(arm) >= arm.getMaxParticipants()) {
					arm = null;
				}
			}
		}

		strategyStatistics.put(getStatisticName("counter", stratifiedEnum), Double.valueOf(counter));
		strategyStatistics.put(getAllocationStatisticName(arm.getName(), stratifiedEnum), Double.valueOf(allocations.get(arm) + 1));

		database.update(trial, participant, arm);
		return arm;
	}

	private String getStatisticName(String statName, String strataName) {
		String result = statName;
		
		if (!strataName.equals("")) {
			result = "(" +strataName +") " +result;
		} 
		return result;
	}

	@Override
	protected Map<String, Double> getInitialisedStats(Trial trial) {
		Map<String, Double> stats = new HashMap<String, Double>();
		for (String strata: trial.getAllStrata()) {
			stats.put(getStatisticName("size", strata), 0.0);
			stats.put(getStatisticName("seed", strata), 0.0);
			stats.put(getStatisticName("counter", strata), 0.0);
			for (Arm a : trial.getArms()) {
				stats.put(getAllocationStatisticName(a.getName(), strata), 0.0);
			}
		}
		return stats;
	}

}
