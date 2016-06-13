package ar.edu.itba.protos.protocol.admin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.protocol.admin.command.Command;
import ar.edu.itba.protos.protocol.admin.command.CommandResult;
import ar.edu.itba.protos.protocol.admin.command.GetAllMappingsCommand;
import ar.edu.itba.protos.protocol.admin.command.GetDefaultMappingCommand;
import ar.edu.itba.protos.protocol.admin.command.MapDefaultCommand;
import ar.edu.itba.protos.protocol.admin.command.MapUserCommand;
import ar.edu.itba.protos.protocol.admin.command.UnMapUserCommand;

@Singleton
public class CommandExecutor {
    private final static Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private final static Map<AdminProtocolToken, Command> commands = new HashMap<>();
    public final static String EMPTY_CMD = "Empty command.";
    public final static String NO_SUCH_CMD = "No such command.";

    public CommandExecutor() {
    }

    @Inject
    protected CommandExecutor(final MapUserCommand mapUser, final UnMapUserCommand unmapUser,
            final GetAllMappingsCommand getAllMappings, final GetDefaultMappingCommand getDefaultMapping,
            final MapDefaultCommand setDefaultMapping) {

        bindCommand(AdminProtocolToken.MAP_USER, mapUser);
        bindCommand(AdminProtocolToken.UNMAP_USER, unmapUser);
        bindCommand(AdminProtocolToken.GET_ALL_MAPPINGS, getAllMappings);
        bindCommand(AdminProtocolToken.SET_DEFAULT_MAPPING, setDefaultMapping);
        bindCommand(AdminProtocolToken.GET_DEFAULT_MAPPING, getDefaultMapping);
    }

    public void bindCommand(final AdminProtocolToken symbol, final Command cmd) {
        logger.debug("Binding command {} to function {}", symbol.toString(), cmd);
        commands.put(symbol, cmd);
    }

    public Command unbindCommand(final AdminProtocolToken symbol) {
        return commands.remove(symbol);
    }

    public CommandResult execute(final String... line) {
        if (line.length == 0) {
            return CommandResult.err(EMPTY_CMD);
        }
        final AdminProtocolToken symbol = AdminProtocolToken.isCommand(line[0]);
        final String[] params = line.length > 1 ? Arrays.copyOfRange(line, 1, line.length) : new String[] {};

        if (params.length != symbol.getParams().length) {
            final String msg = String.format("Invalid number of parameters:\n" +
                    "Expected: %s\n" +
                    "Got:      %s", String.join(", ", symbol.getParams()), String.join(", ", params));
            return CommandResult.err(msg);
        }

        final Command cmd = commands.get(symbol);
        if (cmd == null) {
            logger.debug("Could not find command: <{}>", line[0]);
            return CommandResult.err(NO_SUCH_CMD);
        }
        return CommandResult.wrap(cmd, params);
    }

}
