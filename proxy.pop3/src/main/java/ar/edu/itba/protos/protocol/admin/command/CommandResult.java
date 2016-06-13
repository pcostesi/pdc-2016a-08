package ar.edu.itba.protos.protocol.admin.command;

import java.util.Optional;
import java.util.function.Function;

import ar.edu.itba.protos.protocol.admin.CommandException;

public class CommandResult {
    private enum CommandResultMode {
        ERR, OK;
    }

    private final String result;
    private final CommandResultMode mode;

    private CommandResult(final String result, final CommandResultMode mode) {
        this.result = result != null ? result : "";
        this.mode = mode;
    }

    public static CommandResult ok(final String result) {
        return new CommandResult(result, CommandResultMode.OK);
    }

    public static CommandResult err(final String result) {
        return new CommandResult(result, CommandResultMode.ERR);
    }

    public static CommandResult wrap(final Command cmd, final String... params) {
        try {
            return CommandResult.ok(cmd.execute(params));
        } catch (final CommandException e) {
            return CommandResult.err(e.getMessage());
        }
    }

    public <T> Optional<T> then(final Function<String, T> action) {
        if (mode == CommandResultMode.OK) {
            return Optional.ofNullable(action.apply(result));
        }
        return Optional.empty();
    }

    public <T> Optional<T> otherwise(final Function<String, T> action) {
        if (mode == CommandResultMode.ERR) {
            return Optional.ofNullable(action.apply(result));
        }
        return Optional.empty();
    }

    public String getMessage() {
        return String.format("%s %d\r\n%s\r\n\r\n", mode.name(), result.length(), result);
    }

    public boolean isOk() {
        return mode == CommandResultMode.OK;
    }

    public String getOriginalMessage() {
        return result;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
