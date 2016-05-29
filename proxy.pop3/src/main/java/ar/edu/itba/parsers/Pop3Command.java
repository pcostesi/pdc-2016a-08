package ar.edu.itba.parsers;

public enum Pop3Command {
	
	//
	APOP,
	//
	USER, 
	//
	PASS, 
	//
	TOP,
	STAT,
	LIST,
	DELE,
	NOOP,
	RSET,
	//
	QUIT,
	UIDL,
	// 1 numero de arg, si mando caca tiene status false
	RETR,
	ERR;
    
    public final String toString() {
    	return this.name();
    }

}
