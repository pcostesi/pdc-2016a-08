package ar.edu.itba.protos;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.Upstream;
import ar.edu.itba.protos.config.UserMapping;
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

    @Test
    public void testUserCanBeMapped() {
        final UserMapping mapping = ConfigurationLoader.getUserMapping();
        final String result = CommandExecutor.excute(new String[] { "map", "root@localhost", "example.com", "110" });

        assertEquals("root@localhost -> example.com:110", result);
        final Upstream mapped = mapping.getMappingForUsername("root@localhost");
        assertEquals(mapped.getHost(), "example.com");
        assertEquals(mapped.getPort(), 110);
    }
}
