package ar.edu.itba.protos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ar.edu.itba.protos.protocol.admin.command.CommandResult;


public class CommandResultTest {

    @Test
    public void testResultOkFormatsNicely() {
        final CommandResult resultOk = CommandResult.ok("this is a test string");
        assertEquals(resultOk.getMessage(), "OK 21\r\nthis is a test string\r\n\r\n");
    }

    @Test
    public void testResultErrFormatsNicely() {
        final CommandResult resultErr = CommandResult.err("this is a test string");
        assertEquals(resultErr.getMessage(), "ERR 21\r\nthis is a test string\r\n\r\n");
    }


}
