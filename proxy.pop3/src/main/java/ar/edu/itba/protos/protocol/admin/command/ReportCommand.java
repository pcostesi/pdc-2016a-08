package ar.edu.itba.protos.protocol.admin.command;

import javax.inject.Inject;

import ar.edu.itba.protos.protocol.admin.CommandException;
import ar.edu.itba.protos.transport.metrics.Metrics;

public class ReportCommand implements Command {

    Metrics metrics;

    @Inject
    public ReportCommand(final Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public String execute(final String... params) throws CommandException {
        return metrics.summarize();
    }

}
