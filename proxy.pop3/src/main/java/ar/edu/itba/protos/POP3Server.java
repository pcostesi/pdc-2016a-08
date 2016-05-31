
	package ar.edu.itba.protos;

	import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.transport.concrete.AdminAttachmentFactory;
	import ar.edu.itba.protos.transport.concrete.ForwardAttachmentFactory;
	import ar.edu.itba.protos.transport.handler.AcceptHandler;
	import ar.edu.itba.protos.transport.handler.ConnectHandler;
	import ar.edu.itba.protos.transport.handler.ReadHandler;
	import ar.edu.itba.protos.transport.handler.WriteHandler;
	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Reactor;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;

		/**
		* Ciclo principal de ejecución (master thread). Su función es
		* levantar el servidor en las direcciones y puertos especificados
		* y comenzar a recibir conexiones entrantes, las cuales serán
		* despachadas entre los 'workers' disponibles.
		*/

	public final class POP3Server {
		private static final Logger logger = LoggerFactory.getLogger(POP3Server.class);
		private static final String LISTEN_ADDR = "0.0.0.0";
		private static int POP3_PORT = 110;
		private static int ADMIN_PORT = 666;

		/*
		** Punto de entrada principal del servidor proxy POP-3.
		*/

		public static void main(String [] arguments) {

			/*
			** Fábricas de 'attachments'. Cada servidor puede
			** tener una fábrica distinta en cada puerto de
			** escucha (es decir, en cada 'listener'):
			*/
			final AttachmentFactory forwardFactory
				= new ForwardAttachmentFactory();

			final AttachmentFactory adminFactory
				= new AdminAttachmentFactory();

			/*
			** Se instalan los manejadores (Handlers) en el
			** demultiplexador de eventos global:
			*/
			final Reactor demultiplexor = Reactor.getInstance()
				.add(new AcceptHandler(), Event.ACCEPT)
				.add(new ReadHandler(), Event.READ)
				.add(new WriteHandler(), Event.WRITE)
				.add(new ConnectHandler(), Event.CONNECT);

			/*
			** Se instancia un nuevo servidor y se aplica
			** un 'binding' en cada dirección especificada:
			*/
			final Server pop3 = new Server()
				.addListener(LISTEN_ADDR, POP3_PORT, forwardFactory)
				.addListener(LISTEN_ADDR, ADMIN_PORT, adminFactory);

			try {

				// Alguien, al menos, debe estar escuchando:
				if (0 < pop3.getListeners()) {
					pop3.dispatch();
					pop3.shutdown();
				}
			} catch (IOException exception) {
				logger.error(Message.CANNOT_RAISE);
			}

			// Quitar todos los manejadores del demultiplexor global:
			demultiplexor.unplug();
		}
	}
