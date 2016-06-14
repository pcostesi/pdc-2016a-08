package ar.edu.itba.protos.protocol.admin.command;

import java.io.IOException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.protocol.admin.CommandException;

public class SaveMappingsCommand implements Command {

    private final ConfigurationLoader config;

    @Inject
    public SaveMappingsCommand(final ConfigurationLoader config) {
        this.config = config;
    }

    @Override
    public String execute(final String... params) throws CommandException {
        try {
            config.setUserMapping(params[0], config.getUserMapping());
        } catch (IOException | JAXBException e) {
            throw new CommandException(e.getMessage());
        }
        return "Proxy mapping set to " + params[0];
    }

}
