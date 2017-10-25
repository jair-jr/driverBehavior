package br.com.j2.apm;

public class APMException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2806301154461164619L;

	public APMException() {
		super();
	}

	public APMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public APMException(String message, Throwable cause) {
		super(message, cause);
	}

	public APMException(String message) {
		super(message);
	}

	public APMException(Throwable cause) {
		super(cause);
	}
	
}