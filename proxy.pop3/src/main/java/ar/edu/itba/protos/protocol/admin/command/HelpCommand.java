package ar.edu.itba.protos.protocol.admin.command;

import java.util.Arrays;
import java.util.stream.Collectors;

import ar.edu.itba.protos.protocol.admin.AdminProtocolToken;
import ar.edu.itba.protos.protocol.admin.CommandException;

public class HelpCommand implements Command {

    @Override
    public String execute(final String... params) throws CommandException {
        return "Available commands:\r\n" + Arrays.stream(AdminProtocolToken.values())
                .filter(token -> token != AdminProtocolToken.ERR)
                .map(token -> String.format("- %s: %s", token.toString(), String.join(", ", token.getParams())))
                .collect(Collectors.joining("\r\n"));
    }

}
