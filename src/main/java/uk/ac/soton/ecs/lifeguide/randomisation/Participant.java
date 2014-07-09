package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.Map;

/**
 * A storage object representing a given participant, with their ID and a map
 * of their responses for a specific trial.
 *
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class Participant {
	private int id = -1;
	private Map<String, Float> responses;

	/**
	 * @return The participant's ID.
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return A map of the participant's responses for a specific trial. The map is indexed
	 *         on attribute names, and stores a float value for the participant's response.
	 */
	public Map<String, Float> getResponses() {
		return responses;
	}

	public void setResponses(Map<String, Float> responses) {
		this.responses = responses;
	}

	/**
	 * @return The participant's response for a given attribute, removing the need to fetch
	 *         the entire map when getting a single attribute response.
	 */
	public Float getResponse(String attrName) {
		return responses.get(attrName);

	}

	public String toString() {
		String output = "Participant " + id + ":";
		for (String key : responses.keySet())
			output += " " + key + "=" + responses.get(key);
		return output;
	}

}
