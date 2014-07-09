package uk.ac.soton.ecs.lifeguide.randomisation.exception;

/**
 * Generic exception which represents an error while parsing user XML data.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
@SuppressWarnings("serial")
public class InvalidUserDataException extends Exception {

	private String element;

	/**
	 * Constructs an InvalidUserDataException with the given message.
	 *
	 * @param errorMsg The human-readable error message.
	 */
	public InvalidUserDataException(String errorMsg, String element) {
		super(errorMsg);
		this.element = element;
	}

	/**
	 * @return The error message, giving the problem description and the XML element responsible.
	 */
	@Override
	public String getMessage() {
		return super.getMessage() + "\nError caused by element: " + element;
	}

}
