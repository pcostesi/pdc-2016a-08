package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.Upstream;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;

@Singleton
public class GetUserMappingCommand implements Command {

    private final ConfigurationLoader configurator;

    @Inject
    public GetUserMappingCommand(final ConfigurationLoader configurator) {
        this.configurator = configurator;
    }

    @Override
    public String execute(final String... params) throws CommandException {
        final UserMapping mapping = configurator.getUserMapping();

        final Upstream u = mapping.getMappingForUsername(params[0]);
        if (u == null) {
            throw new CommandException("No default user mapping set.");
        }
        return String.format("%s -> %s:%s", params[0], u.getHost(), u.getPort());
    }

}
