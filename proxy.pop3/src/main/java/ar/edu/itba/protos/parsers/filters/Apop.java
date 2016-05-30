package ar.edu.itba.protos.parsers.filters;

import ar.edu.itba.protos.parsers.Pop3Command;

public class Apop implements Pop3CommandFilter {

	public boolean filter(String input, ParsedCommand result) {

		boolean match = false;

		if (input.length() <= 5) {
		} else if (input.startsWith("apop ")) {
			result.command = Pop3Command.APOP;
			match = true;
			if (input.length() > 5) {
				result.params = input.substring(5);
				result.status = true;
			}
		}
		return match;
	}

}
