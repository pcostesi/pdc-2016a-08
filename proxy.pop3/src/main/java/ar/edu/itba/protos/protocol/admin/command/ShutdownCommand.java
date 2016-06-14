package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;

import ar.edu.itba.protos.protocol.admin.CommandException;
import ar.edu.itba.protos.transport.reactor.Reactor;

public class ShutdownCommand implements Command {

    private final Reactor reactor;

    @Inject
    public ShutdownCommand(final Reactor reactor) {
        this.reactor = reactor;
    }

    @Override
    public String execute(final String... params) throws CommandException {
        reactor.unplug();
        System.exit(0);
        return "Shutting down";
    }

}
