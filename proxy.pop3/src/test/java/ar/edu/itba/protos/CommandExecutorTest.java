package ar.edu.itba.protos;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ar.edu.itba.protos.protocol.admin.CommandException;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;

public class CommandExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testTheExecutorExecutesACommand() {
        thrown.expect(CommandException.class);
        thrown.expectMessage(CommandExecutor.EMPTY_CMD);
        CommandExecutor.excute(new String[] {});
    }
}
