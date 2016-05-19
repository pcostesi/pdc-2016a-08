
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.SelectionKey;

		/**
		* Representa un manejador de eventos. Para cada
		* evento manejable, el 'Handler' debería subscribirse
		* en el reactor de interés.
		*/

	public interface Handler {

		public void handle(SelectionKey key);
	}
