package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.Set;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public interface Statistics {
	public void putStatistic(String name, Float value);

	public Float getStatistic(String name);

	public Set<String> getAllNames();
}
