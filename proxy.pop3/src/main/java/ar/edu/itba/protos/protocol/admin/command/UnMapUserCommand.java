package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.Upstream;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;

@Singleton
public class UnMapUserCommand implements Command {

    private final ConfigurationLoader configurator;

    @Inject
    public UnMapUserCommand(final ConfigurationLoader configurator) {
        this.configurator = configurator;
    }

    @Override
    public String execute(final String... params) throws CommandException {
        if (params.length != 1) {
            throw new CommandException("Invalid number of parameters: " + params.length);
        }

        final UserMapping mapping = configurator.getUserMapping();

        mapping.unmapUser(params[0]);
        final Upstream u = mapping.getDefaultUpstream();
        return String.format("%s -> %s:%s", params[0], u.getHost(), u.getPort());
    }

}
