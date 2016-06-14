package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;

@Singleton
public class MapUserCommand implements Command {

    private final ConfigurationLoader configurator;

    @Inject
    public MapUserCommand(final ConfigurationLoader configurator) {
        this.configurator = configurator;
    }

    @Override
    public String execute(final String... params) throws CommandException {
        if (params.length != 3) {
            throw new CommandException("Invalid number of parameters: " + params.length);
        }

        final UserMapping mapping = configurator.getUserMapping();

        mapping.mapUserToUpstream(params[0], params[1], Integer.parseInt(params[2]));
        return String.format("%s -> %s:%s", params[0], params[1], params[2]);
    }

}
