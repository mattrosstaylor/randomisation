package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A used to generate {@link Participant} objects, which random responses for
 * each of the attributes in a given {@link TrialDefinition}.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class ParticipantGenerator implements LifeGuideAPI {

	private static int idTracker = 0;

	private List<Participant> participants;
	private TrialDefinition trialDefinition;

	/**
	 * Constructs a ParticipantGenerator object. Using this class as an object
	 * ensures that the generated participants are stored in an internal list.
	 *
	 * @param trialDefinition The trial on which to base the participant generation.
	 */
	public ParticipantGenerator(TrialDefinition trialDefinition) {
		participants = new ArrayList<Participant>();
		this.trialDefinition = trialDefinition;
	}

	/**
	 * @param id The ID of the requested participant.
	 * @return the participant with the specified ID. If no participant with that
	 *         ID is stored in this ParticipantGenerator instance, and the ID has already been
	 *         used (i.e. it is less than the current value of the ID generator), this returns null.
	 *         Otherwise, participants are generated and stored until the requested ID is reached,
	 *         and that participant is returned.
	 */
	@Override
	public Participant getParticipant(int id) {
		if (id > participants.size() - 1)
			for (int i = participants.size(); i <= id; i++)
				participants.add(generate(trialDefinition));

		return participants.get(id);
	}

	/**
	 * Generates a new participant for a given trial, stores them, then returns them.
	 *
	 * @param trialDefinition The trial in which to generate a participant.
	 * @return A new participant, with random responses for each of the specified trial's
	 *         attributes.
	 */
	public Participant getNewParticipant(TrialDefinition trialDefinition) {
		this.trialDefinition = trialDefinition;
		return getParticipant(++idTracker);
	}

	/**
	 * Returns a statically generated participant for a given trial. Equivalent to
	 * {@link #getNewParticipant(TrialDefinition)}, but does not store the generated participant.
	 *
	 * @param trialDefinition he trial in which to generate a participant.
	 * @return A new participant, with random responses for each of the specified trial's
	 *         attributes.
	 */
	public static Participant generate(TrialDefinition trialDefinition) {
		Participant participant = new Participant();
		Map<String, Float> responses = new HashMap<String, Float>();

		List<Attribute> attributes = trialDefinition.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.isRawValue()) {
				int groupIndex = (int) (Math.random() * attribute.getGroupCount());
				Group g = attribute.getRanges().get(groupIndex);
				float val = (float) (Math.random() * (g.getRangeMax() - g.getRangeMin())) + g.getRangeMin();
				responses.put(attribute.getAttributeName(), val);
			} else {
				responses.put(attribute.getAttributeName(), (float) ((int) (Math.random() * attribute.getGroupCount())));
			}
		}

		participant.setId(idTracker++);
		participant.setResponses(responses);
		return participant;
	}

	/**
	 * Set the value of the ID generator. Generated participants will be allocated IDs starting at
	 * this value, incrementing by one for each generated participant.
	 *
	 * @param startID The value on which to start the ID generator (i.e. the ID given to the next
	 *                generated participant).
	 */
	public static void setStartID(int startID) {
		idTracker = startID;
	}
}
