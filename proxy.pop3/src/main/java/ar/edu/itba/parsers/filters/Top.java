package ar.edu.itba.parsers.filters;

import ar.edu.itba.parsers.Pop3Command;

public class Top implements Pop3CommandFilter {

	public boolean filter(String input, ParsedCommand result) {
		
		boolean match = false;

		if (input.length() <= 4) {
		} else if (input.startsWith("top ")) {
			result.command = Pop3Command.TOP;
			match = true;
			if (input.length() > 4) {
				result.params = input.substring(5);
				result.status = true;
			}
		}
		return match;
	}
}
