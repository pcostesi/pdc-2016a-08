package ar.edu.itba.protos.parsers.filters;

import java.nio.ByteBuffer;

import ar.edu.itba.protos.parsers.Pop3Command;

public class Quit implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'Q', 'U', 'I', 'T' };
		byte min[] = { 'q', 'u', 'i', 't' };
		boolean match = false;
		int initial = buff.position();
		int index = 0;
		byte b;

		if (buff.remaining() >= 6) {
			match = true;
			while (index < 4) {
				b = buff.get();
				if (!(b == caps[index] || b == min[index])) {
					match = false;
				}
				index++;
			}
			if (index == 4 && match) {
				b = buff.get();
				if (b == '\r' && buff.get() == '\n') {
					result.setCommand(Pop3Command.QUIT);
					result.setStatus(CommandStatus.COMPLETE);
				}

			}
		}
		if (result.getCommand() != Pop3Command.QUIT) {
			buff.position(initial);
			match = false;
		}
		return match;
	}
}
