package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.Upstream;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;

@Singleton
public class GetDefaultMappingCommand implements Command {

    private final ConfigurationLoader configurator;

    @Inject
    public GetDefaultMappingCommand(final ConfigurationLoader configurator) {
        this.configurator = configurator;
    }
    @Override
    public String execute(final String... params) throws CommandException {
        final UserMapping mapping = configurator.getUserMapping();
        final Upstream u = mapping.getDefaultUpstream();
        if (u == null) {
            throw new CommandException("Default mapping not set.");
        }
        return String.format("%s:%s", u.getHost(), u.getPort());
    }

}
