package ar.edu.itba.protos.parsers.filters;

public interface Pop3CommandFilter {
	
	public static final int maxArgumentSize = 40;
	
	public boolean filter(String input, ParsedCommand result);
	
}
