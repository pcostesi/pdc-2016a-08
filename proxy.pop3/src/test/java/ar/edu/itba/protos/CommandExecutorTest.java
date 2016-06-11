package ar.edu.itba.protos;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.protocol.admin.CommandException;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;

public class CommandExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testTheExecutorExecutesACommand() {
        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        final CommandExecutor executor = new CommandExecutor(mockedConfigurator);

        thrown.expect(CommandException.class);
        thrown.expectMessage(CommandExecutor.EMPTY_CMD);
        executor.execute(new String[] {});
    }

    @Test
    public void testUserCanBeMapped() {
        final UserMapping mapping = Mockito.mock(UserMapping.class);
        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);
        final CommandExecutor executor = new CommandExecutor(mockedConfigurator);

        final String result = executor.execute(new String[] { "map", "root@localhost", "example.com", "110" });

        assertEquals("root@localhost -> example.com:110", result);
        Mockito.verify(mapping, Mockito.description("Invalid parameters set"))
        .mapUserToUpstream("root@localhost", "example.com", 110);
    }
}
