package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;

@Singleton
public class MapDefaultCommand implements Command {

    private final ConfigurationLoader configurator;

    @Inject
    public MapDefaultCommand(final ConfigurationLoader configurator) {
        this.configurator = configurator;
    }
    @Override
    public String execute(final String... params) throws CommandException {
        final UserMapping mapping = configurator.getUserMapping();
        if (params.length != 2) {
            throw new CommandException("Invalid number of arguments: " + params.length);
        }
        mapping.setDefaultUpstream(params[0], Integer.parseInt(params[1]));
        return String.format("%s:%s", params[0], params[1]);
    }

}
