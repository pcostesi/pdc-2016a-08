package ar.edu.itba.protos.protocol.admin.command;

import ar.edu.itba.protos.protocol.admin.CommandException;

@FunctionalInterface
public interface Command {
    public String execute(String... params) throws CommandException;
}
