package ar.edu.itba.protos.protocol.admin;

public class CommandException extends RuntimeException {

    private final String reason;

    public CommandException(final String reason) {
        super();
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return reason;
    }

    private static final long serialVersionUID = 4439070168423477689L;

}
