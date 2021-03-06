package ar.edu.itba.protos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.Upstream;
import ar.edu.itba.protos.config.UserMapping;
import ar.edu.itba.protos.config.UserUpstreamPair;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;
import ar.edu.itba.protos.protocol.admin.command.Command;
import ar.edu.itba.protos.protocol.admin.command.CommandResult;
import ar.edu.itba.protos.protocol.admin.command.GetAllMappingsCommand;
import ar.edu.itba.protos.protocol.admin.command.GetDefaultMappingCommand;
import ar.edu.itba.protos.protocol.admin.command.GetUserMappingCommand;
import ar.edu.itba.protos.protocol.admin.command.MapDefaultCommand;
import ar.edu.itba.protos.protocol.admin.command.MapUserCommand;
import ar.edu.itba.protos.protocol.admin.command.UnMapUserCommand;

public class CommandExecutorTest {

    @Test
    public void testTheExecutorDoesNotExecuteEmptyCommands() {
        final CommandExecutor executor = new CommandExecutor();

        final CommandResult result = executor.execute(new String[] {});
        assertEquals(result.isOk(), false);
        assertEquals(result.getOriginalMessage(), CommandExecutor.EMPTY_CMD);
    }

    @Test
    public void testTheExecutorDoesNotFindACommand() {
        final CommandExecutor executor = new CommandExecutor();

        final CommandResult result = executor.execute("fake-command");
        assertEquals(result.isOk(), false);
        assertEquals(result.getOriginalMessage(), CommandExecutor.NO_SUCH_CMD);
    }

    @Test
    public void testInvalidNumberOfParameters() {
        final CommandExecutor executor = new CommandExecutor();

        final CommandResult result = executor.execute("map", "root@localhost");
        assertEquals(result.isOk(), false);
        assertEquals(result.getOriginalMessage(), "Invalid number of parameters:\n" +
                "Expected: user, host, port\n" +
                "Got:      root@localhost");
    }

    @Test
    public void testUserCanBeMapped() {
        final UserMapping mapping = Mockito.spy(UserMapping.class);
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
        final UserMapping mapping = Mockito.spy(UserMapping.class);
        mapping.setDefaultUpstream("example.com", 110);
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
        final UserMapping mapping = Mockito.spy(UserMapping.class);
        mapping.setDefaultUpstream("example.com", 110);
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

    @Test
    public void testWecCanSetADefaultMap() {
        final UserMapping mapping = Mockito.spy(UserMapping.class);
        mapping.setDefaultUpstream("example.com", 110);

        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);

        final Command command = new MapDefaultCommand(mockedConfigurator);
        final String result = command.execute("example.co.uk", "110");

        assertEquals("example.co.uk:110", result);
        Mockito.verify(mapping).setDefaultUpstream("example.co.uk", 110);
    }

    @Test
    public void testWecCanGetTheDefaultMapping() {
        final UserMapping mapping = Mockito.spy(UserMapping.class);
        mapping.setDefaultUpstream("example.com", 110);

        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);

        final Command command = new GetDefaultMappingCommand(mockedConfigurator);
        final String result = command.execute();

        assertEquals("example.com:110", result);
        Mockito.verify(mapping).getDefaultUpstream();
    }

    @Test
    public void testWecCanGetTheUserMapping() {
        final UserMapping mapping = Mockito.spy(UserMapping.class);
        mapping.setDefaultUpstream("example.co.uk", 110);
        mapping.mapUserToUpstream("root@localhost", "example.com", 110);
        final ConfigurationLoader mockedConfigurator = Mockito.mock(ConfigurationLoader.class);
        Mockito.when(mockedConfigurator.getUserMapping()).then((i) -> mapping);

        final Command command = new GetUserMappingCommand(mockedConfigurator);
        final String result = command.execute("root@localhost");

        assertEquals("root@localhost -> example.com:110", result);
        Mockito.verify(mapping).getMappingForUsername("root@localhost");
    }

}
