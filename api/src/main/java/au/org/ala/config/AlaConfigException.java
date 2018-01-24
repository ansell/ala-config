/**
 * 
 */
package au.org.ala.config;

/**
 * Class of exceptions deriving from ALA Config
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class AlaConfigException extends RuntimeException {

	private static final long serialVersionUID = 1272023384358056291L;

	/**
	 * Create a new AlaConfigException with no message or cause.
	 */
	public AlaConfigException() {
	}

	/**
	 * Create a new AlaConfigException with the given message
	 * 
	 * @param message
	 *            The message to attach to the exception
	 */
	public AlaConfigException(String message) {
		super(message);
	}

	/**
	 * Create a new AlaConfigException with the given cause
	 * 
	 * @param cause
	 *            The cause to attach to the exception
	 */
	public AlaConfigException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a new AlaConfigException with the given message and cause
	 * 
	 * @param message
	 *            The message to attach to the exception
	 * @param cause
	 *            The cause to attach to the exception
	 */
	public AlaConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new AlaConfigException with the given message and cause and other
	 * variables.
	 * 
	 * @param message
	 *            The message to attach to the exception
	 * @param cause
	 *            The cause to attach to the exception
	 * @param enableSuppression
	 *            whether or not suppression is enabled or disabled
	 * @param writableStackTrace
	 *            whether or not the stack trace should be writable
	 */
	public AlaConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
