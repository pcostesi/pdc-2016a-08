package ar.edu.itba.protos;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import ar.edu.itba.protos.protocol.admin.parser.AdminProtocolParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AdminProtocolParserTest extends TestCase {

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( AdminProtocolParserTest.class );
    }

    public void testTheTokenizerCorrectlyParsesOneCommand() {
        final ByteBuffer commandBuffer = ByteBuffer.wrap("map root@localhost localhost 110\r\n"
                .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();
        final List<String> parsedTokens = parser.tokenize(commandBuffer);
        assertTrue(parsedTokens.size() == 5);
        assertEquals(parsedTokens.get(0), "map");
        assertEquals(parsedTokens.get(1), "root@localhost");
        assertEquals(parsedTokens.get(2), "localhost");
        assertEquals(parsedTokens.get(3), "110");
        assertEquals(parsedTokens.get(4), null);
    }

    public void testTheTokenizerCorrectlyParsesTwoCommands() {
        final ByteBuffer commandBuffer = ByteBuffer
                .wrap("map root@localhost localhost 110\r\nmap root@localhost localhost 110\r\n"
                        .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();
        final List<String> parsedTokens = parser.tokenize(commandBuffer);
        assertTrue(parsedTokens.size() == 10);

        assertEquals(parsedTokens.get(0), "map");
        assertEquals(parsedTokens.get(1), "root@localhost");
        assertEquals(parsedTokens.get(2), "localhost");
        assertEquals(parsedTokens.get(3), "110");
        assertEquals(parsedTokens.get(4), null);

        assertEquals(parsedTokens.get(5), "map");
        assertEquals(parsedTokens.get(6), "root@localhost");
        assertEquals(parsedTokens.get(7), "localhost");
        assertEquals(parsedTokens.get(8), "110");
        assertEquals(parsedTokens.get(9), null);
    }

    public void testTheTokenizerCorrectlyParsesWhitespace() {
        final ByteBuffer commandBuffer = ByteBuffer
                .wrap("map   root@localhost\tlocalhost 110\r\n\r\nmap root@localhost localhost 110\r\n       "
                        .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();
        final List<String> parsedTokens = parser.tokenize(commandBuffer);
        assertTrue(parsedTokens.size() == 11);

        assertEquals(parsedTokens.get(0), "map");
        assertEquals(parsedTokens.get(1), "root@localhost");
        assertEquals(parsedTokens.get(2), "localhost");
        assertEquals(parsedTokens.get(3), "110");
        assertEquals(parsedTokens.get(4), null);
        assertEquals(parsedTokens.get(5), null);

        assertEquals(parsedTokens.get(6), "map");
        assertEquals(parsedTokens.get(7), "root@localhost");
        assertEquals(parsedTokens.get(8), "localhost");
        assertEquals(parsedTokens.get(9), "110");
        assertEquals(parsedTokens.get(10), null);
    }

    public void testTheParserCorrectlyParsesOneLine() {
        final ByteBuffer commandBuffer = ByteBuffer.wrap("map root@localhost localhost 110\r\n"
                .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();
        final List<String[]> parsedTokens = parser.parse(commandBuffer);
        assertTrue(parsedTokens.size() == 1);
        assertTrue(parsedTokens.get(0) != null);
        assertTrue(parsedTokens.get(0).length == 4);
    }

    public void testTheParserCorrectlyParsesOneLineAndAHalf() {
        final ByteBuffer commandBuffer = ByteBuffer.wrap("map root@localhost localhost 110\r\nmap?"
                .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();
        final List<String[]> parsedLines = parser.parse(commandBuffer);
        assertTrue(parsedLines.size() == 1);
        assertTrue(parsedLines.get(0) != null);
        assertTrue(parsedLines.get(0).length == 4);
    }

    public void testTheParserCorrectlyParsesManyLines() {
        final ByteBuffer commandBuffer = ByteBuffer
                .wrap("map   root@localhost\tlocalhost 110\r\n\r\nmap root@localhost localhost 110\r\n   \r\n    "
                        .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();
        final List<String[]> parsedTokens = parser.parse(commandBuffer);

        assertTrue(parsedTokens.size() == 2);
        assertTrue(parsedTokens.get(0) != null);
        assertTrue(parsedTokens.get(0).length == 4);
    }

    public void testTheParserCorrectlyParsesIncompleteBuffers() {
        final ByteBuffer commandBuffer = ByteBuffer.wrap("map root@localhost localhost 110\r\nunm"
                .getBytes(StandardCharsets.US_ASCII));
        final AdminProtocolParser parser = new AdminProtocolParser();

        final List<String[]> parsedLines = parser.parse(commandBuffer);
        assertTrue(parsedLines.size() == 1);
        assertTrue(parsedLines.get(0) != null);
        assertTrue(parsedLines.get(0).length == 4);

        commandBuffer.put("ap user\r\n".getBytes());
        commandBuffer.flip();

        final List<String[]> parsedLinesAgain = parser.parse(commandBuffer);
        assertTrue(parsedLinesAgain.size() == 1);
        assertTrue(parsedLinesAgain.get(0) != null);
        assertTrue(parsedLinesAgain.get(0).length == 2);

        commandBuffer.put("map?\r\n".getBytes());
        commandBuffer.flip();

        final List<String[]> parsedLinesYetAgain = parser.parse(commandBuffer);
        assertTrue(parsedLinesYetAgain.size() == 1);
        assertTrue(parsedLinesYetAgain.get(0) != null);
        assertTrue(parsedLinesYetAgain.get(0).length == 1);
    }
}
