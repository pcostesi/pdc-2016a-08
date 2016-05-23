
	package ar.edu.itba.protos.transport.handler;

	import java.nio.channels.SelectionKey;

	import ar.edu.itba.protos.transport.reactor.Handler;

		/**
		* Se encarga de establecer una conexión con el 'origin-server',
		* por la cual el circuito cliente-servidor quedará establecido.
		* Para determinar el 'origin-server' es necesario pre-procesar una
		* cantidad mínima del flujo de bytes desde el cliente, para poder
		* determinar hacia donde demultiplexar la conexión.
		*/

	public final class ConnectHandler implements Handler {

		public ConnectHandler() {

			return;
		}

		public void handle(SelectionKey key) {

			/**/System.out.println("> Write (" + key + ")");
		}
	}
