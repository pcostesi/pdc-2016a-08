package ar.edu.itba.protos.protocol.pop3;

import java.nio.ByteBuffer;

public class User implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'U', 'S', 'E', 'R' };
		byte min[] = { 'u', 's', 'e', 'r' };
		boolean match = false;
		int index = 0, initial = buff.position();
		byte b;

		if (buff.remaining() >= 5) {
			match = true;
			while (buff.hasRemaining() && index < 4) {
				b = buff.get();
				if (!(b == caps[index] || b == min[index])) {
					match = false;
				}
				index++;
			}
			if (index == 4 && match) {
				b = buff.get();
				if (b == ' ') {
					result.extractParams(buff);
					result.setCommand(Pop3Command.USER);
				}
			}

		}
		if (result.getCommand() != Pop3Command.USER) {
			buff.position(initial);
			match = false;
		}
		return match;
	}

}
