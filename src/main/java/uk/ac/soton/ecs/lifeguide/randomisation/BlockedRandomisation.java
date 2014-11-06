package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class BlockedRandomisation extends Randomisation {

	private static final Logger logger = LoggerFactory.getLogger(BlockedRandomisation.class);

	public BlockedRandomisation(Trial trial, DataManager database) {
		super(trial, database);
	}

	protected Arm allocateHelper(String stratifiedEnum, List<Arm> openArms, Map<Arm, Integer> allocations) {
		int totalWeight = trial.getTotalWeight();
		
		int actualSize = (int) Math.round(statistics.get(getStatisticName("size", stratifiedEnum)));
		long seed = Double.doubleToLongBits(statistics.get(getStatisticName("seed", stratifiedEnum)));
		int counter = (int) Math.round(statistics.get(getStatisticName("counter", stratifiedEnum)));
		
		List<Arm> block;
		Arm arm = null;

		while (arm == null) {
			// Allocate new block if previous full
			if (counter >= actualSize) {

				int blockSize = (int) Math.round(parameters.get("blocksize")) / totalWeight;
				int delta = (int) Math.round(parameters.get("delta")) / totalWeight;
				actualSize = (blockSize - delta + random.nextInt(2 * delta + 1)) * totalWeight;

				double serialisedSeed = random.nextDouble();
				seed = Double.doubleToLongBits(serialisedSeed);
				
				statistics.put(getStatisticName("size", stratifiedEnum), Double.valueOf(actualSize));
				statistics.put(getStatisticName("seed", stratifiedEnum), serialisedSeed);

				counter = 0;
			}

			block = new ArrayList<Arm>(actualSize);
			for (Arm a : trial.getArms()) {
				for (int i = 0; i < a.getWeight() * (actualSize / totalWeight); i++) {
					block.add(a);
				}
			}
			Collections.shuffle(block, new Random(seed));

			while (arm == null && counter < actualSize) {
				arm = block.get(counter);
				counter++;

				if (allocations.get(arm) >= arm.getMaxParticipants()) {
					arm = null;
				}
			}
		}
		statistics.put(getStatisticName("counter", stratifiedEnum), Double.valueOf(counter));
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
