
	package ar.edu.itba.protos.transport.handler;

	import java.nio.channels.SelectionKey;

	import ar.edu.itba.protos.transport.reactor.Handler;

		/**
		** Se encarga de procesar los eventos de aceptación.
		** Estos suceden cuando un cliente establece conexión
		** en alguna de las direcciones y puertos de escucha de
		** alguno de los servidores que están utilizando el mismo
		** reactor al cual este 'handler' está subscripto.
		*/

	public final class AcceptHandler implements Handler {

		public AcceptHandler() {

			return;
		}

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de aceptación.
		*/

		public void handle(SelectionKey key) {

			return;
		}
	}
