package rw.afriteck.pms.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Object id) {
        super("RESOURCE_NOT_FOUND", "%s with id %s not found".formatted(resourceName, id));
    }
}