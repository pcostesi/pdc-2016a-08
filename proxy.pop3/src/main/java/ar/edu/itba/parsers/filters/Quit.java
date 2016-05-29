package ar.edu.itba.parsers.filters;

import ar.edu.itba.parsers.Pop3Command;

public class Quit implements Pop3CommandFilter {

	@Override
	public boolean filter(String input, ParsedCommand result) {

		boolean match = false;

		if (input.length() < 4) {
		} else if (input.startsWith("quit")) {
			result.command = Pop3Command.QUIT;
			match = true;
			result.status = true;
			if (input.length() > 4) {
				if (!Character.isWhitespace(input.charAt(5))) {
					result.command = Pop3Command.ERR;
				}
				result.status = false;
			}
		}
		return match;
	}
}
