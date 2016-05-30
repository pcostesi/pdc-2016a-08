package ar.edu.itba.protos.parsers;

import java.nio.CharBuffer;

import ar.edu.itba.protos.transport.support.Attachment;

public class Pop3ClientParser implements Pop3Parser {

	private Attachment att;
	private CharBuffer buff;
	

	public void pareseMessage() {
		buff = att.getInboundBuffer().asCharBuffer();
		while (buff.remaining() != 0) {
			
		}
	}
}
