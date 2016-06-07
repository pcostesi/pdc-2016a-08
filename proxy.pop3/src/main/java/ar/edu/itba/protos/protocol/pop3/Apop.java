package ar.edu.itba.protos.protocol.pop3;

import java.nio.ByteBuffer;

public class Apop implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'A', 'P', 'O', 'P' };
		byte min[] = { 'a', 'p', 'o', 'p' };
		boolean match = false;
		int index = 0, minSize = caps.length +1;
		int initial = buff.position();
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
				if (b == ' ') {
					result.extractParams(buff);
					result.setCommand(Pop3Command.APOP);
				}
			}
		}
		if (result.getCommand() != Pop3Command.APOP) {
			buff.position(initial);
			match = false;
		}
		return match;
	}
}
