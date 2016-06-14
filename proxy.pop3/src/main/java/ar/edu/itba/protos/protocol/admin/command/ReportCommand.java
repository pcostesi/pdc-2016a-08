package ar.edu.itba.protos.protocol.admin.command;

import ar.edu.itba.protos.protocol.admin.CommandException;

public class ReportCommand implements Command {

    @Override
    public String execute(final String... params) throws CommandException {
        throw new CommandException("Nothing to see here yet");
    }

}
