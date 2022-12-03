package indi.arrowyi.autoconfig.configmanager;

public class ConfigRuntimeException extends RuntimeException {
    public ConfigRuntimeException(String message) {
        super(message);
    }

    public ConfigRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
