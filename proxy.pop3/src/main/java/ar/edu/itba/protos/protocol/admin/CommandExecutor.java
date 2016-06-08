package ar.edu.itba.protos.protocol.admin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class CommandExecutor {
    private final static Map<AdminProtocolToken, Command> commands = new HashMap<>();
    public final static String EMPTY_CMD = "Empty command.";
    public final static String NO_SUCH_CMD = "No such command.";

    public static String excute(final String... line) {
        if (line.length == 0) {
            throw new CommandException(EMPTY_CMD);
        }
        final String[] params = line.length > 1 ? Arrays.copyOfRange(line, 1, line.length) : new String[] {};
        final AdminProtocolToken symbol = AdminProtocolToken.isCommand(line[0]);
        final Command cmd = commands.get(symbol);
        if (cmd == null) {
            throw new CommandException(NO_SUCH_CMD);
        }
        final String cmdResult = cmd.execute(params);
        if (cmdResult == null) {
            return "";
        }
        return cmdResult;
    }

    {
        commands.put(AdminProtocolToken.MAP_USER, (params) -> {
            return ":)";
        });
    }
}
