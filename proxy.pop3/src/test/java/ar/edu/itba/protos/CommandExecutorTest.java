package ar.edu.itba.protos;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.Upstream;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.config.UserUpstreamPair;
import ar.edu.itba.protos.protocol.admin.CommandException;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;
import ar.edu.itba.protos.protocol.admin.command.Command;
import ar.edu.itba.protos.protocol.admin.command.GetAllMappingsCommand;
import ar.edu.itba.protos.protocol.admin.command.MapUserCommand;
import ar.edu.itba.protos.protocol.admin.command.UnMapUserCommand;

public class CommandExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testTheExecutorDoesNotExecuteEmptyCommands() {
        final CommandExecutor executor = new CommandExecutor();

        thrown.expect(CommandException.class);
        thrown.expectMessage(CommandExecutor.EMPTY_CMD);
        executor.execute(new String[] {});
    }

    @Test
    public void testTheExecutorDoesNotFindACommand() {
        final CommandExecutor executor = new CommandExecutor();

        thrown.expect(CommandException.class);
        thrown.expectMessage(CommandExecutor.NO_SUCH_CMD);
        executor.execute("");
    }

    @Test
    public void testUserCanBeMapped() {
        final UserMapping mapping = Mockito.mock(UserMapping.class);
        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);

        final Command command = new MapUserCommand(mockedConfigurator);
        final String result = command.execute("root@localhost", "example.com", "110");

        assertEquals("root@localhost -> example.com:110", result);
        Mockito.verify(mapping, Mockito.description("Invalid parameters set"))
        .mapUserToUpstream("root@localhost", "example.com", 110);
    }

    @Test
    public void testUserCanBeUnmapped() {
        final UserMapping mapping = Mockito.mock(UserMapping.class);
        mapping.defaultUpstream = new Upstream("example.com", 110);
        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);

        final Command command = new UnMapUserCommand(mockedConfigurator);
        final String result = command.execute("root@localhost");

        assertEquals("root@localhost -> example.com:110", result);
        Mockito.verify(mapping, Mockito.description("Invalid parameters set"))
        .unmapUser("root@localhost");
    }

    @Test
    public void testWeCanGetMappings() {
        final UserMapping mapping = Mockito.mock(UserMapping.class);
        mapping.defaultUpstream = new Upstream("example.com", 110);
        Mockito.when(mapping.getAllMappings()).then((i) -> new UserUpstreamPair[] {
                new UserUpstreamPair("a@localhost", new Upstream("example.com", 111)),
                new UserUpstreamPair("b@localhost", new Upstream("example.com", 112)),
                new UserUpstreamPair("c@localhost", new Upstream("example.com", 113)),
                new UserUpstreamPair("d@localhost", new Upstream("example.com", 114)),
        });
        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);

        final Command command = new GetAllMappingsCommand(mockedConfigurator);
        final String result = command.execute("root@localhost");

        assertEquals("- a@localhost -> example.com:111\r\n" +
                "- b@localhost -> example.com:112\r\n" +
                "- c@localhost -> example.com:113\r\n" +
                "- d@localhost -> example.com:114", result);
        Mockito.verify(mapping, Mockito.description("Invalid parameters set"))
                .getAllMappings();
    }
}
