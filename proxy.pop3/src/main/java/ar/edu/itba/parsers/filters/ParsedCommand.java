package ar.edu.itba.parsers.filters;

import ar.edu.itba.parsers.Pop3Command;

public class ParsedCommand {
	
	public Pop3Command command;
	public String params;
	public boolean status;
	
	ParsedCommand(Pop3Command command, String params, boolean status){
		this.command = command;
		this.params = params;
		this.status = status;
	}
	
	ParsedCommand(){
		command = Pop3Command.ERR;
		params = null;
		status = false;
	}

}
