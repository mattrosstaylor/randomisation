package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class StrategyStatistics implements Statistics {

    private Map<String, Float> statistics;

    public StrategyStatistics(Map<String, Float> statistics) {
        this.statistics = statistics;
    }

    public StrategyStatistics() {
        statistics = new HashMap<String, Float>();
    }

    public void putStatistic(String name, Float value) {
        statistics.put(name, value);
    }

    public Float getStatistic(String name) {
        return statistics.get(name);
    }

    public Set<String> getAllNames() {
        return statistics.keySet();
    }
}
