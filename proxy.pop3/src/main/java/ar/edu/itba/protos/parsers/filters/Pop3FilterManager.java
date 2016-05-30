package ar.edu.itba.protos.parsers.filters;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class Pop3FilterManager {

	private List<Pop3CommandFilter> filterChain = new ArrayList<Pop3CommandFilter>();

	public Pop3FilterManager() {
		filterChain.add(new Apop());
		filterChain.add(new Top());
		filterChain.add(new Quit());
		filterChain.add(new Retr());
		filterChain.add(new User());
		filterChain.add(new Pass());
	}

	public ParsedCommand filter(ByteBuffer buff) {

		ParsedCommand result = new ParsedCommand();

		String SCommand = new String(buff.array());

		if (SCommand.length() < 4) {
			return result;
		}
		String aux = SCommand.substring(0, 4).toLowerCase();

		String input = aux + SCommand.substring(4, SCommand.length());

		for (Pop3CommandFilter fil : filterChain) {
			if (fil.filter(input, result)) {
				break;
			}
		}
		return result;
	}

}
