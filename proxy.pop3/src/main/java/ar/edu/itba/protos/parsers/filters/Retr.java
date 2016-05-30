package ar.edu.itba.protos.parsers.filters;

import ar.edu.itba.protos.parsers.Pop3Command;

public class Retr implements Pop3CommandFilter {
	
	@Override
	public boolean filter(String input, ParsedCommand result) {

		boolean match = false;

		if (input.length() < 5) {
		} else if (input.startsWith("retr ")) {
			result.command = Pop3Command.RETR;
			match = true;
			if (input.length() > 5) {
				result.params = input.substring(5);
				int i;
				if (result.params.length() <= maxArgumentSize) {
					result.status = true;
					char c;
					for (i = 0; i < result.params.length(); i++) {
						c = result.params.charAt(i);
						if (!Character.isDigit(c)) {
							result.status = false;
							break;
						}

					}
				}
			}
		}
		return match;
	}
}
