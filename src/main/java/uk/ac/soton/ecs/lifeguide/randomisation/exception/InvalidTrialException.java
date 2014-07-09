package uk.ac.soton.ecs.lifeguide.randomisation.exception;

/**
 * Generic exception which represents an error while parsing a trial specification file.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
@SuppressWarnings("serial")
public class InvalidTrialException extends Exception {

	private int lineNumber;

	/**
	 * Constructs an InvalidTrialException with the given message. Line number defaults to 0.
	 *
	 * @param errorMsg The human-readable error message.
	 */
	public InvalidTrialException(String errorMsg){
		this(errorMsg, 0);
	}

	/**
	 * Constructs an InvalidTrialException with the given message and line number.
	 *
	 * @param errorMsg The human-readable error message.
	 * @param lineNum  The line of the specification file on which the error occurred.
	 */
	public InvalidTrialException(String errorMsg, int lineNum) {
		super(errorMsg);
		this.lineNumber = lineNum;
	}

	/**
	 * @return The error message, in the format [Line <line number>]: <error message>.
	 */
	@Override
	public String getMessage() {
		return "[Line " + lineNumber + "]: " + super.getMessage();
	}

	public void setLineNumber(int lineNumber){
		this.lineNumber = lineNumber;
	}
	
}
