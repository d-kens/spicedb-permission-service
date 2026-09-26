package dev.onyango.permission_service.spicedb;

import io.grpc.StatusRuntimeException;

/**
 * Raised by {@link SpiceDbClient} when a SpiceDB call fails, so callers never deal with gRPC types.
 */
public class SpiceDbException extends RuntimeException {

    public enum Reason {
        /** The request references an unknown type/relation/permission or is otherwise malformed. */
        INVALID_REQUEST,
        /** The relationship being created already exists. */
        CONFLICT,
        /** SpiceDB could not be reached, did not answer in time, or hit a transient conflict; safe to retry. */
        UNAVAILABLE,
        /** Anything else, including auth failures between this service and SpiceDB. */
        INTERNAL
    }

    private final Reason reason;

    public SpiceDbException(String message, StatusRuntimeException cause) {
        super(message + " -> " + cause.getStatus(), cause);
        this.reason = reasonFor(cause);
    }

    public Reason getReason() {
        return reason;
    }

    private static Reason reasonFor(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT, FAILED_PRECONDITION -> Reason.INVALID_REQUEST;
            case ALREADY_EXISTS -> Reason.CONFLICT;
            case UNAVAILABLE, DEADLINE_EXCEEDED, ABORTED -> Reason.UNAVAILABLE;
            default -> Reason.INTERNAL;
        };
    }
}
