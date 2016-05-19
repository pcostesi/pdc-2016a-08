
	package ar.edu.itba.protos.transport.handler;

	import java.nio.channels.SelectionKey;

	import ar.edu.itba.protos.transport.reactor.Handler;

		/**
		* Se encarga de procesar los eventos de lectura.
		* Estos suceden cuando, una vez establecido un
		* circuito de conexión, uno de los extremos de dicho
		* circuito comienza a emitir un flujo de bytes.
		*/

	public final class ReadHandler implements Handler {

		public ReadHandler() {

			return;
		}

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de lectura del flujo de bytes.
		*/

		public void handle(SelectionKey key) {

			/**/System.out.println("> Read (" + key + ")");
		}
	}
