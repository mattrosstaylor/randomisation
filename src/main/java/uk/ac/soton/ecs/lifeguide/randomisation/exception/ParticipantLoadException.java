package uk.ac.soton.ecs.lifeguide.randomisation.exception;

/**
 * Generic exception which represents an error while loading participants from a file.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class ParticipantLoadException extends Exception {

    private int lineNum;

    public ParticipantLoadException(String errorMsg, int lineNum) {
        super(errorMsg);
        this.lineNum = lineNum;
    }

    @Override
    public String getMessage() {
        return "[Line " + lineNum + "]: " + super.getMessage();
    }

}
