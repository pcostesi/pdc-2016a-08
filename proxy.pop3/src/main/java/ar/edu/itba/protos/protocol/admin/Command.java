package ar.edu.itba.protos.protocol.admin;

@FunctionalInterface
public interface Command {
    public String execute(String... params) throws CommandException;
}
