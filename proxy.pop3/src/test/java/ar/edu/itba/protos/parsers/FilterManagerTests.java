package ar.edu.itba.protos.parsers;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

import ar.edu.itba.protos.parsers.filters.ParsedCommand;
import ar.edu.itba.protos.parsers.filters.Pop3FilterManager;

public class FilterManagerTests {

	private final Pop3FilterManager parser = new Pop3FilterManager();
	private final String superLongString = "THISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJ";
	
	@Test
	public void sendShortCommandTest() {
		String commandToTest = "";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false);
	}
	
	@Test
	public void parseCaseSensitivness() {
		String commandToTest = "user Juansito";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == true && result.command == Pop3Command.USER && result.params.equals("Juansito"));
	}
	
	@Test
	public void parseUSERCommandTest() {
		String commandToTest = "USER Juansito";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == true && result.command == Pop3Command.USER && result.params.equals("Juansito"));
	}
	
	@Test
	public void parseWrongRetrCommandTest() {
		String commandToTest = "RETR HOLA";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false && result.command == Pop3Command.RETR);
	}
	
	@Test
	public void parseRetrCommandTest() {
		String commandToTest = "RETR 1";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == true && result.command == Pop3Command.RETR && result.params.equals("1"));
	}
	
	@Test
	public void parseIncompleteCommandTest() {
		String commandToTest = "USER";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false && result.command == Pop3Command.USER);
	}
	
	@Test
	public void parseLongUserCommandTest() {
		String commandToTest = "USER "+ superLongString;
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false && result.command == Pop3Command.USER && result.params.equals(superLongString));
	}
	
	@Test
	public void parseLongPassCommandTest() {
		String commandToTest = "PASS "+ superLongString;
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false && result.command == Pop3Command.PASS && result.params.equals(superLongString));
	}
	
	@Test
	public void parsePASSCommandTest() {
		String commandToTest = "PASS thisIsAPassword";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == true && result.command == Pop3Command.PASS && result.params.equals("thisIsAPassword"));
	}
	
	@Test
	public void parseAPOPCommandTest() {
		String commandToTest = "APOP mrose c4c9334bac560ecc979e58001b3e22fb";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == true && result.command == Pop3Command.APOP && result.params.equals("mrose c4c9334bac560ecc979e58001b3e22fb"));
	}
	
	@Test
	public void parseWrongSpaceTest() {
		String commandToTest = "USERJuansito";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false);
	}
	
	@Test
	public void parseUnkownCommandTest() {
		String commandToTest = "CULO";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.status == false);
	}
	
}