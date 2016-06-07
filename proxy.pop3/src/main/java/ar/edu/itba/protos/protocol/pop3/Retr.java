package ar.edu.itba.protos.protocol.pop3;

import java.nio.ByteBuffer;

public class Retr implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'R', 'E', 'T', 'R' };
		byte min[] = { 'r', 'e', 't', 'r' };
		boolean match = false;
		int index = 0, initial = buff.position(), minSize = caps.length + 1;
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
					result.skipParams(buff); // al proxy no le interesan
					result.setCommand(Pop3Command.RETR);
				}
			}

		}
		if (result.getCommand() != Pop3Command.RETR) {
			buff.position(initial);
			match = false;
		}
		return match;
	}
}
