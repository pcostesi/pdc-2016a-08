
	package ar.edu.itba.protos;

	import java.io.IOException;

	import ar.edu.itba.protos.transport.handler.AcceptHandler;
	import ar.edu.itba.protos.transport.handler.ReadHandler;
	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Reactor;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;

		/**
		* Ciclo principal de ejecución (master thread). Su función es
		* levantar el servidor en las direcciones y puertos especificados
		* y comenzar a recibir conexiones entrantes, las cuales serán
		* despachadas entre los 'workers' disponibles.
		*/

	public final class POP3Server {

		/*
		** Punto de entrada principal del servidor proxy POP-3.
		*/

		public static void main(String [] arguments) {

			/**/System.out.println("POP-3 Proxy Server");

			/*
			** Se instalan los manejadores (Handlers) en el
			** demultiplexador de eventos global:
			*/
			final Reactor demultiplexor = Reactor.getInstance()
				.add(new AcceptHandler(), Event.ACCEPT)
				.add(new ReadHandler(), Event.READ);

			/*
			** Se instancia un nuevo servidor y se aplica
			** un 'binding' en cada dirección especificada:
			*/
			Server pop3 = new Server()
				.addListener("0.0.0.0", 110, null)
				.addListener("0.0.0.0", 666, null);

			try {

				// Alguien, al menos, debe estar escuchando:
				if (0 < pop3.getListeners()) {

					pop3.dispatch();
					pop3.shutdown();
				}
			}
			catch (IOException exception) {

				/**/exception.printStackTrace();
				System.out.println(Message.CANNOT_RAISE);
			}

			// Quitar todos los manejadores del demultiplexor global:
			demultiplexor.unplug();

			/**/System.out.println("End.");
		}
	}
