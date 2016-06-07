package ar.edu.itba.protos.protocol.pop3;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Pop3FilterManager {

	private static final int smallestCommand = 5; // Would be TOP
	private List<Pop3CommandFilter> filterChain = new ArrayList<Pop3CommandFilter>();

	public Pop3FilterManager() {
		filterChain.add(new Apop());
		filterChain.add(new Top());
		filterChain.add(new Quit());
		filterChain.add(new Retr());
		filterChain.add(new User());
		filterChain.add(new Pass());
		filterChain.add(new Capa());
	}

	public ParsedCommand filter(ByteBuffer buff) {

		ParsedCommand result = new ParsedCommand();
		if (buff.remaining() < smallestCommand) {
			return result;
		}

		for (Pop3CommandFilter fil : filterChain) {
			if (fil.filter(buff, result)) {
				break;
			}
		}
		if (result.getCommand() == Pop3Command.ERR) {
			result.skipParams(buff);
		}
		return result;
	}

}
