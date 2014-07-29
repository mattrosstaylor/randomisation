package uk.ac.soton.ecs.lifeguide.randomisation;

/**
 * An object which defines a range of values. Any participants whose response
 * for a given attribute falls into this range are assigned to this group.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class Group {

	private String name;
	private float rangeMin;
	private float rangeMax;

	/**
	 * Constructs a Group object with the specified name and range.
	 *
	 * @param name     The group's name (for readability purposes).
	 * @param rangeMin The lower bound on the group's values.
	 * @param rangeMax The upper bound on the group's values.
	 */
	public Group(String name, float rangeMin, float rangeMax) {
		this.name = name;
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}

	public String getName() {
		return name;
	}

	public float getRangeMin() {
		return rangeMin;
	}

	public float getRangeMax() {
		return rangeMax;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRangeMin(float rangeMin) {
		this.rangeMin = rangeMin;
	}

	public void setRangeMax(float rangeMax) {
		this.rangeMax = rangeMax;
	}

	public void setRange(float rangeMin, float rangeMax) {
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}

	public String toString() {
		if (rangeMin == -Float.MAX_VALUE) return "< " + rangeMax;
		if (rangeMax == Float.MAX_VALUE) return "> " + rangeMin;
		return rangeMin + " to " + rangeMax;
	}

}
