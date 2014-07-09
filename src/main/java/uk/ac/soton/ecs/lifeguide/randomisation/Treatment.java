package uk.ac.soton.ecs.lifeguide.randomisation;

/**
 * An object which represents a treatment arm within a {@link TrialDefinition}.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class Treatment {

	private String id;
	private int weight;
	private int maxParticipants;
	private boolean participantLimit;

	/**
	 * Constructs a Treatment with the given name (ID) and weight.
	 *
	 * @param id     The treatment's ID.
	 * @param weight The weight of this treatment, used to increase/decrease the
	 *               ratio of allocations for this treatment arm.
	 */
	public Treatment(String id, int weight) {
		this(id, weight, Integer.MAX_VALUE);
	}

	/**
	 * Constructs a Treatment with the given name (ID) and weight.
	 *
	 * @param id              The treatment's ID.
	 * @param weight          The weight of this treatment, used to increase/decrease the
	 *                        ratio of allocations for this treatment arm.
	 * @param maxParticipants The maximum number of participants which can be allocated
	 *                        to this treatment arm. Optional, see {@link #Treatment(String, int)}.
	 */
	public Treatment(String id, int weight, int maxParticipants) {
		this.id = id;
		this.weight = weight;
		this.maxParticipants = maxParticipants;
		this.participantLimit = maxParticipants < 0 ? false : true;
	}

	public String getName() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public int getParticipantLimit() {
		return maxParticipants;
	}

	public boolean hasParticipantLimit() {
		return participantLimit;
	}

	public void setName(String name) {
		this.id = name;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public void setMaxParticipants(int limit) {
		this.maxParticipants = limit;
		this.participantLimit = true;
	}

	public void useParticipantLimit(boolean enforceLimit) {
		participantLimit = enforceLimit;
	}

	private String limitString(Integer limit) {
		return (limit == Integer.MAX_VALUE) ? "none" : limit.toString();
	}

	public String toString() {
		String output = id + " ";
		output += "(Weight: " + weight;
		if (hasParticipantLimit())
			output += ", Limit: " + limitString(maxParticipants);
		output += ")";
		return output;
	}

}
