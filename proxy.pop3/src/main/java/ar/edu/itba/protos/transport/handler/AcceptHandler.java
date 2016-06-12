
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.ClosedChannelException;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.ServerSocketChannel;
	import java.nio.channels.SocketChannel;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;
	import ar.edu.itba.protos.transport.support.Synchronizer;
	import ar.edu.itba.protos.transport.support.WatchdogTimer;

		/**
		* <p>Se encarga de procesar los eventos de aceptación.
		* Estos suceden cuando un cliente establece conexión
		* en alguna de las direcciones y puertos de escucha de
		* alguno de los servidores que están utilizando el mismo
		* reactor al cual este <i>handler</i> está subscripto.</p>
		*
		* <p>Esta clase es <b>thread-safe</b>.</p>
		*/

	@Singleton
	public final class AcceptHandler implements Handler {

		// Logger:
		private final static Logger logger
			= LoggerFactory.getLogger(AcceptHandler.class);

		// Repositorio global de claves:
		private final Synchronizer sync;

		// Monitor de inactividad:
		private final WatchdogTimer watchdog;

		@Inject
		private AcceptHandler(
					final Synchronizer sync,
					final WatchdogTimer watchdog) {

			this.sync = sync;
			this.watchdog = watchdog;
		}

		/*
		** No es necesario implementar estas funcionalidades
		** debido a que 'ThreadingCore' ya se encarga lo
		** suficiente. Además, la nueva conexión entrante se
		** sincroniza internamente, debido a que este handler
		** es el único que posee referencia hacia ella.
		*/

		public void onSubmit(SelectionKey key) {}
		public void onResume(SelectionKey key) {}

		/**
		* <p>Se encarga de aceptar una nueva conexión entrante,
		* de registrar el nuevo canal y de instalar su <i>attachment</i>
		* correspondiente, obtenido gracias a la fábrica asociada a la
		* clave manipulada. Además, configura el estado inicial del canal
		* y subscribe la misma al monitor de inactividad.</p>
		*
		* <p>El repositorio de claves no se utiliza para modificar el
		* estado de la clave generada debido a que no es posible que otro
		* handler acceda a la misma, debido a que esta se crea por primera
		* vez en este lugar, y nadie más la puede referenciar.</p>
		*
		* @param key
		*	La clave a procesar, en la cual se activó el
		*	evento <b>ACCEPT</b>.
		*/

		public void handle(SelectionKey key) {

			// La clave del nuevo cliente:
			SelectionKey downstream = null;

			try {

				// La interfaz activada en el servidor:
				ServerSocketChannel server =
					(ServerSocketChannel) key.channel();

				// Establecemos la nueva conexión entrante:
				SocketChannel socket = server.accept();

				if (socket != null) {

					logger.info(
						Message.INCOMING_CONNECTION.getMessage(),
						socket.getRemoteAddress(),
						server.getLocalAddress());

					// Obtengo la fábrica para este servicio:
					AttachmentFactory factory
						= (AttachmentFactory) key.attachment();

					// Fabrico un nuevo 'attachment':
					Attachment attachment = factory.create();

					// Registro el nuevo cliente y sus datos:
					downstream = socket
						.configureBlocking(false)
						.register(key.selector(), 0, attachment);

					// Almacenar el estado de la nueva clave:
					sync.save(downstream);

					if (attachment != null) {

						// Especifico el flujo que identifica este canal:
						attachment.setDownstream(downstream);

						// Setea el repositorio de claves:
						attachment.setSynchronizer(sync);

						// Configura los eventos iniciales para este canal:
						sync.enable(
							downstream,
							attachment.getInitialOptions());

						// Monitoreo el nuevo canal:
						watchdog.update(downstream);
					}
				}
				else throw new IOException();
			}
			catch (ClosedChannelException exception) {

				logger.error(
					Message.INTERFACE_DOWN.getMessage(),
					Server.tryToResolveAddress(key));
			}
			catch (CancelledKeyException exception) {

				logger.error(
					Message.CLIENT_UNPLUGGED.getMessage(),
					Server.tryToResolveAddress(downstream));
			}
			catch (IOException exception) {

				logger.error(
					Message.UNKNOWN.getMessage(),
					this.getClass().getSimpleName());
			}

			// Repone el estado de las claves:
			if (downstream != null) sync.restore(key, downstream);
			else sync.restore(key);
		}
	}
