
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.net.ConnectException;
	import java.nio.channels.ClosedChannelException;
	import java.nio.channels.SelectionKey;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;
	import ar.edu.itba.protos.transport.support.Synchronizer;

		/**
		* <p>Se encarga de establecer una conexión con el
		* <b>origin-server</b>, por la cual el circuito
		* cliente-servidor quedará establecido. Para determinar
		* el <i>origin-server</i>, es necesario pre-procesar una
		* cantidad mínima del flujo de bytes desde el cliente,
		* para poder determinar hacia donde demultiplexar la
		* conexión.</p>
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

		/**
		* <p>En caso de que la clave posea un forwarder,
		* almacena el estado del mismo en el repositorio de
		* claves, siempre y cuando el downstream y el upstream
		* no representen el mismo canal.</p>
		*
		* @param key
		*	La clave a procesar.
		*/

		public void onSubmit(SelectionKey key) {

			SelectionKey upstream
				= ((Attachment) key.attachment()).getUpstream();

			if (key != upstream && upstream != null)
				sync.save(upstream);
		}

		/**
		* <p>Recupera el estado del canal de forwarding, en caso
		* de que exista uno. El canal downstream es recuperado
		* automáticamente por el núcleo de procesamiento.</p>
		*
		* @param key
		*	La clave a procesar.
		*/

		public void onResume(SelectionKey key) {

			SelectionKey upstream
				= ((Attachment) key.attachment()).getUpstream();

			if (key != upstream && upstream != null)
				sync.restore(upstream);
		}

		/**
		* <p>Establece por completo la conexión remota con el
		* servidor origen, y prepara el canal para los eventos
		* iniciales, lo que la deja preparada para operar.</p>
		*
		* @param key
		*	La clave a procesar, en la cual se activó el
		*	evento <b>CONNECT</b>.
		*/

		public void handle(SelectionKey key) {

			logger.debug("Connect ({})", key);

			Attachment attachment = (Attachment) key.attachment();

			try {

				// Finalizo la conexión remota:
				if (attachment.getSocket().finishConnect()) {

					logger.info(
						Message.CONNECTION_SUCCEED.getMessage(),
						Server.tryToResolveAddress(key));

					// Prepara los eventos del canal:
					sync.disable(key, Event.CONNECT);

					// Obtengo las opciones iniciales:
					sync.enable(key, attachment.getInitialOptions());
				}
				else logger.debug(
					Message.PENDING_CONNECTION.getMessage(),
					Server.tryToResolveAddress(key));
			}
			catch (ClosedChannelException exception) {

				logger.error(
					Message.UNEXPECTED_UNPLUG.getMessage(),
					Server.tryToResolveAddress(key));
			}
			catch (ConnectException exception) {

				logger.error(
					Message.CONNECTION_TIMEOUT.getMessage(),
					Server.tryToResolveAddress(key));
			}
			catch (IOException exception) {

				exception.printStackTrace();
				logger.error(
					Message.CANNOT_FORWARD.getMessage(),
					this.getClass().getSimpleName());
			}
		}
	}
