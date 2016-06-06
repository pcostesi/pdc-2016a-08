package ar.edu.itba.protos.protocol.pop3;

import java.nio.ByteBuffer;

public class Top implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'T', 'O', 'P' };
		byte min[] = { 't', 'o', 'p' };
		boolean match = false;
		int index = 0, initial = buff.position();
		byte b;

		if (buff.remaining() >= 5) {
			match = true;
			while (buff.hasRemaining() && index < 3) {
				b = buff.get();
				if (!(b == caps[index] || b == min[index])) {
					match = false;
				}
				index++;
			}
			if (index == 3 && match) {
				b = buff.get();
				if (b == ' ') {
					result.skipParams(buff); // al proxy no le interesan
					result.setCommand(Pop3Command.PASS);
				}
			}
		}
		if (result.getCommand() != Pop3Command.TOP) {
			buff.position(initial);
			match = false;
		}
		return match;
	}
}
