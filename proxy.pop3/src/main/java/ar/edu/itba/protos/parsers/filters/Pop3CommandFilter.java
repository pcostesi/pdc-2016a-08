package ar.edu.itba.protos.parsers.filters;

import java.nio.ByteBuffer;

public interface Pop3CommandFilter {
	
	public static final int maxArgumentSize = 40;
	
	public boolean filter(ByteBuffer input, ParsedCommand result);
	
}
