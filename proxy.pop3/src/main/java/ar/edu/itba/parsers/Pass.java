package ar.edu.itba.parsers;

public class Pass implements Pop3CommandFilter {

	public boolean filter(String input, ParsedCommand result) {
		
		boolean match = false;

		if (input.length() <= 5) {
		} else if (input.startsWith("pass ")) {
			result.command = Pop3Command.PASS;
			match = true;
			if (input.length() > 5) {
				result.params = input.substring(6);
				int i;
				if (result.params.length() <= maxArgumentSize) {
					result.status = true;
					for (i = 0; i < result.params.length(); i++) {
						if (Character.isWhitespace(result.params.charAt(i))) {
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
