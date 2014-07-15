package uk.ac.soton.ecs.lifeguide.randomisation.exception;

/**
 * Exception thrown by incorrect API usage.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @author Matt R Taylor (mrt@ecs.soton.ac.uk)
 * @since 1.7
 */
public class BadCommandException extends Exception {
	public BadCommandException(String message) {
		super(message);
	}

	public BadCommandException(String message, Exception e) {
		super(message, e);
	}
}
