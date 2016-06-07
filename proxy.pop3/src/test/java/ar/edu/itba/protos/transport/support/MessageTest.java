
	package ar.edu.itba.protos.transport.support;

	import static org.junit.Assert.*;

	import org.junit.Test;

	public class MessageTest {

		@Test
		public void allMessagesAreNotEmpty() {

			for (Message message : Message.values())
				assertTrue(0 < message.getMessage().length());
		}

		@Test
		public void allMessagesAreShort() {

			for (Message message : Message.values())
				assertTrue(message.getMessage().length() < 80);
		}
	}
