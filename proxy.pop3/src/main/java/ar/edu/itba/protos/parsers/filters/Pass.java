package ar.edu.itba.protos.parsers.filters;

import java.nio.ByteBuffer;

import ar.edu.itba.protos.parsers.Pop3Command;

public class Pass implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'P', 'A', 'S', 'S' };
		byte min[] = { 'p', 'a', 's', 's' };
		boolean match = false;
		int index = 0, initial = buff.position();
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
				if (b == ' ') {
					result.skipParams(buff); // al proxy no le interesan
					result.setCommand(Pop3Command.PASS);
				}
			}
		}
		if (result.getCommand() != Pop3Command.PASS) {
			buff.position(initial);
			match = false;
		}
		return match;
	}
}
