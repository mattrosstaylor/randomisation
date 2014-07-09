package uk.ac.soton.ecs.lifeguide.randomisation;

/**
 * A wrapper interface for the concrete LifeGuide API used to retrieve
 * a {@link Participant} object from an id.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see DBConnector
 * @since 1.7
 */
public interface LifeGuideAPI {
    /**
     * @param id The id of the participant who is about to be retrieved
     * @return A {@link Participant} object constructed from the data retrieved from the LifeGuide database.
     * @throws IllegalArgumentException if a participant with the given id does not exist.
     */
    public Participant getParticipant(int id) throws IllegalArgumentException;
}
