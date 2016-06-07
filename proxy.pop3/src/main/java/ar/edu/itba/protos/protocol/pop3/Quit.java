package ar.edu.itba.protos.protocol.pop3;

import java.nio.ByteBuffer;

public class Quit implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'Q', 'U', 'I', 'T' };
		byte min[] = { 'q', 'u', 'i', 't' };
		boolean match = false;
		int initial = buff.position();
		int index = 0, minSize = caps.length + 1;
		byte b;

		if (buff.remaining() >= minSize) {
			match = true;
			while (index < caps.length) {
				b = buff.get();
				if (!(b == caps[index] || b == min[index])) {
					match = false;
				}
				index++;
			}
			if (index == caps.length && match) {
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
