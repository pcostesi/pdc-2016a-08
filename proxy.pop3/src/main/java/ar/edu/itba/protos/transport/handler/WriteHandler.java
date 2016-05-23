
	package ar.edu.itba.protos.transport.handler;

	import java.nio.channels.SelectionKey;

	import ar.edu.itba.protos.transport.reactor.Handler;

		/**
		* Este 'handler' es el encargado de forwardear la información
		* a través de los circuitos establecidos. En primer lugar,
		* recibe la información del cliente, y la envía hacia el
		* servidor origen. En segundo lugar, envía las respuestas de
		* este último devuelta al cliente. Además, se encarga de
		* gestionar las situaciones de error.
		*/

	public final class WriteHandler implements Handler {

		public WriteHandler() {

			return;
		}

		public void handle(SelectionKey key) {

			/**/System.out.println("> Write (" + key + ")");
		}
	}
