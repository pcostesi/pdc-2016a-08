package ar.edu.itba.parsers.filters;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class Pop3FilterManager {

	private List<Pop3CommandFilter> filterChain = new ArrayList<Pop3CommandFilter>();

	Pop3FilterManager() {

	}

	public ParsedCommand filter(ByteBuffer buff) {

		ParsedCommand result = new ParsedCommand();

		CharBuffer CBCommand = buff.asCharBuffer();
		String SCommand = CBCommand.toString();

		if (SCommand.length() < 4) {
			return result;
		}
		String aux = SCommand.substring(0, 4).toLowerCase();

		String input = aux + SCommand.substring(5, SCommand.length());

		for (Pop3CommandFilter fil : filterChain) {
			if (fil.filter(input, result)) {
				break;
			}
		}
		return result;
	}

}
