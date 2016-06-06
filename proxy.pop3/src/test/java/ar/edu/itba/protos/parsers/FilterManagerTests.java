package ar.edu.itba.protos.parsers;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

import ar.edu.itba.protos.parsers.filters.CommandStatus;
import ar.edu.itba.protos.parsers.filters.ParsedCommand;
import ar.edu.itba.protos.parsers.filters.Pop3FilterManager;

public class FilterManagerTests {

	private final Pop3FilterManager parser = new Pop3FilterManager();


	@Test
	public void sendShortCommandTest() {
		String commandToTest = "";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getCommand() == Pop3Command.ERR);
	}

	@Test
	public void parseCaseSensitivness() {
		String commandToTest = "user Juansito\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getCommand() == Pop3Command.USER && result.getStatus() == CommandStatus.COMPLETE
				&& result.getParams().equals("Juansito"));
	}

	@Test
	public void parseUSERCommandTest() {
		String commandToTest = "USER Juansito\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.COMPLETE && result.getCommand() == Pop3Command.USER
				&& result.getParams().equals("Juansito"));
	}

	@Test
	public void parseWrongRetrCommandTest() {
		String commandToTest = "RETR HOLA";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getCommand() == Pop3Command.RETR && result.getStatus() == CommandStatus.INCOMPLETE);
	}

	@Test
	public void parseRetrCommandTest() {
		String commandToTest = "RETR 1\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.COMPLETE && result.getCommand() == Pop3Command.RETR);
	}

	@Test
	public void parseIncompleteCommandTest() {
		String commandToTest = "USER ";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.INCOMPLETE && result.getCommand() == Pop3Command.USER);
	}

	@Test
	public void parseLongUserCommandTest() {
		String commandToTest = "USER " + superLongString + "\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.OVERSIZED && result.getCommand() == Pop3Command.USER);
	}

	@Test
	public void parseLongPassCommandTest() {
		String commandToTest = "PASS " + superLongString;
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.OVERSIZED && result.getCommand() == Pop3Command.PASS
				&& result.getParams() == null);
	}

	@Test
	public void parsePASSCommandTest() {
		String commandToTest = "PASS thisIsAPassword\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.COMPLETE && result.getCommand() == Pop3Command.PASS
				&& result.getParams() == null);
	}

	@Test
	public void parseAPOPCommandTest() {
		String commandToTest = "APOP mrose c4c9334bac560ecc979e58001b3e22fb\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.COMPLETE && result.getCommand() == Pop3Command.APOP
				&& result.getParams().equals("mrose c4c9334bac560ecc979e58001b3e22fb"));
	}

	@Test
	public void parseWrongSpaceTest() {
		String commandToTest = "USERJuansito\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		int initial = buffer.position();
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.UNCHECKED && result.getCommand() == Pop3Command.ERR && initial == buffer.position());
	}

	@Test
	public void parseUnkownCommandTest() {
		String commandToTest = "CMDD FakeCommandou";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.UNCHECKED && result.getCommand() == Pop3Command.ERR);
	}
	
	@Test
	public void CapaTest() {
		String commandToTest = "CAPA\r\n";
		ByteBuffer buffer = ByteBuffer.wrap(commandToTest.getBytes());
		ParsedCommand result = parser.filter(buffer);
		assertTrue(result.getStatus() == CommandStatus.COMPLETE && result.getCommand() == Pop3Command.CAPA);
	}
	
	
	/*Placed here to avoid visual spamm*/
	private final String superLongString = "THISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJ"
			+ "DAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHI"
			+ "SISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSA"
			+ "KJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJA"
			+ "SHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJ"
			+ "SKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJ"
			+ "KBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJD"
			+ "AKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHIS"
			+ "ISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAK"
			+ "JBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJAS"
			+ "HJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJS"
			+ "KJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJK"
			+ "BSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDA"
			+ "KJSDSADSAKJTHISISAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISI"
			+ "SAVERASJDBJBSAKJBSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJTHISISAVERASJDBJBSAKJ"
			+ "BSADKJSDKSAKJASHJKDBASDYSBDAKJSKJSABJKBDJKASBJKBSDKJBDKDKJSKJDAKJSDSADSAKJ";

}