package ar.edu.itba.protos.parsers.filters;

import java.nio.ByteBuffer;

import ar.edu.itba.protos.parsers.Pop3Command;

public class Apop implements Pop3CommandFilter {

	@Override
	public boolean filter(ByteBuffer buff, ParsedCommand result) {

		byte caps[] = { 'A', 'P', 'O', 'P' };
		byte min[] = { 'a', 'p', 'o', 'p' };
		boolean match = false;
		int index = 0;
		int initial = buff.position();
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
