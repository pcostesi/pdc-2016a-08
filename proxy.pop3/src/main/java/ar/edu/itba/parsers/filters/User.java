package ar.edu.itba.parsers.filters;

import ar.edu.itba.parsers.Pop3Command;

public class User implements Pop3CommandFilter {

	public boolean filter(String input, ParsedCommand result) {
		
		boolean match = false;
		
		if (input.length() <= 5) {
		} else if (input.startsWith("user ")){
			result.command = Pop3Command.USER;
			match = true;

			if (input.length() > 5) {
				result.params = input.substring(5);
				int i;
				if (result.params.length() <= maxArgumentSize) {
					result.status = true;
					// char c;
					for (i = 0; i < result.params.length(); i++) {
						if (Character.isWhitespace(result.params.charAt(i))) {
							result.status = false;
							break;
						}
						/*
						 * c = params.charAt(i);
						 * if(!Character.isLetterOrDigit(c)){ status = false;
						 * break; }
						 */
					}
				}
			}
		}
		return match;
	}

}
