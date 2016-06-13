package ar.edu.itba.protos.protocol.admin.command;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;

public class GetAllMappingsCommand implements Command {

    private final ConfigurationLoader configurator;

    @Inject
    public GetAllMappingsCommand(final ConfigurationLoader configurator) {
        this.configurator = configurator;
    }
    @Override
    public String execute(final String... params) throws CommandException {
        final UserMapping mapping = configurator.getUserMapping();
        return Arrays.stream(mapping.getAllMappings())
                .map(m -> String.format("- %s -> %s:%d", m.user, m.upstream.getHost(), m.upstream.getPort()))
                .collect(Collectors.joining("\r\n"));
    }

}
