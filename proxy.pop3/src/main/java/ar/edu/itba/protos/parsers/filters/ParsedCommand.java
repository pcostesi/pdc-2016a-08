package ar.edu.itba.protos.parsers.filters;

import java.nio.ByteBuffer;
import ar.edu.itba.protos.parsers.Pop3Command;

public class ParsedCommand {

	private Pop3Command command;
	private String params;
	private CommandStatus status;
	private static final int maxCommandSize = 255;

	ParsedCommand(Pop3Command command, String params, CommandStatus status) {
		this.command = command;
		this.params = params;
		this.status = status;
	}

	ParsedCommand() {
		command = Pop3Command.ERR;
		params = null;
		status = CommandStatus.UNCHECKED;
	}

	public Pop3Command getCommand() {
		return command;
	}

	public void setCommand(Pop3Command command) {
		this.command = command;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public CommandStatus getStatus() {
		return status;
	}

	public void setStatus(CommandStatus status) {
		this.status = status;
	}

	/*
	 * Consume el buffer de entrada hasta encontrar CRLF, terminar el buffer o
	 * recorrer maxCommandSize. Si encuentra CRLF, guarda los caracteres
	 * entremedio en params (String).
	 */
	public void extractParams(ByteBuffer buff) {

		byte parameters[];
		int count = 0;

		count = skipParams(buff);
		if (status == CommandStatus.COMPLETE) {
			if (count != 0) {
				parameters = new byte[count];
				int i = 0;
				int position = buff.position() - count - 2; // -2 for \r\n, ya
															// que position esta
															// delante de ellos
				while (i < count) {
					parameters[i++] = buff.get(position++);
				}
				params = new String(parameters);
			}
		}
	}

	/*
	 * Busca CRLF, consumiendo el buffer y devolviendo cuantos caracteres salteo
	 * (NO cuenta el CRLF)
	 */
	public int skipParams(ByteBuffer buff) {
		byte c;
		boolean endFound = false;
		int count = 0;

		while (buff.hasRemaining() && !endFound && count < maxCommandSize) {
			c = buff.get();
			if (c == '\r' && buff.hasRemaining() && buff.get() == '\n') {
				endFound = true;
			}
			count++;
		}
		if (endFound) {
			count--; // conte el \r, asi que lo descuento
			status = CommandStatus.COMPLETE;
		} else if (count == maxCommandSize) {
			status = CommandStatus.OVERSIZED;
		} else if (!buff.hasRemaining()) {
			status = CommandStatus.INCOMPLETE;
		}
		return count;
	}

}
