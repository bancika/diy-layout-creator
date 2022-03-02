package com.github.romankh3.image.comparison.exception;

/**
 * {@link RuntimeException} that is thrown in case of an image getting failures.
 */
public class ImageNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@link ImageNotFoundException} with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a call to
     * Throwable.initCause(java.lang.Throwable).
     *
     * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage()
     *        method.
     */
    public ImageNotFoundException(String message) {
        super(message);
    }
}
