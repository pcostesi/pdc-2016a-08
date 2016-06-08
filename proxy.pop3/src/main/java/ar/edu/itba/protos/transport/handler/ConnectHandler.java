
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.channels.SelectionKey;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Synchronizer;

		/**
		* <p>Se encarga de establecer una conexión con el 'origin-server',
		* por la cual el circuito cliente-servidor quedará establecido.
		* Para determinar el 'origin-server' es necesario pre-procesar una
		* cantidad mínima del flujo de bytes desde el cliente, para poder
		* determinar hacia donde demultiplexar la conexión.</p>
		*
		* <p>Esta clase es <b>thread-safe</b>.</p>
		*/

	@Singleton
	public final class ConnectHandler implements Handler {

		// Logger:
		private final static Logger logger
			= LoggerFactory.getLogger(ConnectHandler.class);

		// Repositorio global de claves:
		private final Synchronizer sync;

		@Inject
		private ConnectHandler(final Synchronizer sync) {

			this.sync = sync;
		}

		public void onSubmit(SelectionKey key) {

			Attachment attachment = (Attachment) key.attachment();
			SelectionKey upstream = attachment.getUpstream();

			if (upstream != null) {

				sync.save(upstream);
				sync.suspend(upstream);
			}
		}

		public void onResume(SelectionKey key) {

			Attachment attachment = (Attachment) key.attachment();
			SelectionKey upstream = attachment.getUpstream();

			if (upstream != null)
				sync.restore(upstream);
		}

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de conexión saliente establecida.
		*/

		public void handle(SelectionKey key) {

			logger.debug("Connect ({})", key);

			Attachment attachment = (Attachment) key.attachment();

			try {

				// Finalizo la conexión remota:
				if (attachment.getSocket().finishConnect()) {

					logger.debug("Connection established on {}", this);

					// Prepara los eventos del canal:
					sync.disable(key, Event.CONNECT);
					sync.enable(key, Event.READ);
					attachment.getOutboundBuffer().flip();

					if (attachment.hasOutboundData())
						sync.enable(key, Event.WRITE);

					attachment.getOutboundBuffer().compact();
				}
				else logger.debug("Pending connection on {}", this);
			}
			catch (IOException exception) {

				logger.error(
					"Could not handle the connection: {}",
					Message.CLOSED_PORT,
					exception);
			}
		}
	}
