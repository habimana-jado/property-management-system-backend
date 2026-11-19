package rw.afriteck.pms.common.tenant;

import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class TenantContext {

    /** Used when no hotel is selected. Matches no real row, so tenant data reads as empty. */
    public static final UUID NO_TENANT = new UUID(0L, 0L);

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID hotelId) {
        CURRENT.set(Objects.requireNonNull(hotelId));
    }

    public static Optional<UUID> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static UUID require() {
        UUID id = CURRENT.get();
        if (id == null) {
            throw new ForbiddenException("NO_TENANT", "No hotel is selected for this request");
        }
        return id;
    }

    /** For path ids: a foreign hotel id looks the same as one that doesn't exist. */
    public static void requireCurrent(UUID hotelId) {
        if (!require().equals(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
    }

    public static void clear() {
        CURRENT.remove();
    }
}
