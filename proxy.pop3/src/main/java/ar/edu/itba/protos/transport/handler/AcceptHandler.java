
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

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;
	import ar.edu.itba.protos.transport.support.Synchronizer;

		/**
		* <p>Se encarga de procesar los eventos de aceptación.
		* Estos suceden cuando un cliente establece conexión
		* en alguna de las direcciones y puertos de escucha de
		* alguno de los servidores que están utilizando el mismo
		* reactor al cual este 'handler' está subscripto.</p>
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

		@Inject
		private AcceptHandler(final Synchronizer sync) {

			this.sync = sync;
		}

		/*
		** No es necesario implementar estas funcionalidades
		** debido a que 'ThreadingCore' ya se encarga lo
		** suficiente.
		*/

		public void onSubmit(SelectionKey key) {}
		public void onResume(SelectionKey key) {}

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de aceptación.
		*/

		public void handle(SelectionKey key) {

			logger.debug("Accept ({})", key);

			// La clave del nuevo cliente:
			SelectionKey downstream = null;

			try {

				// Establecemos la nueva conexión entrante:
				SocketChannel socket
					= ((ServerSocketChannel) key.channel()).accept();

				if (socket != null) {

					// Obtengo la fábrica para este servicio:
					AttachmentFactory factory
						= (AttachmentFactory) key.attachment();

					// Fabrico un nuevo 'attachment':
					Attachment attachment = factory.create();

					// Registro el nuevo cliente y sus datos:
					downstream = socket
						.configureBlocking(false)
						.register(key.selector(), 0, attachment);

					if (attachment != null) {

						// Especifico el flujo que identifica este canal:
						attachment.setDownstream(downstream);

						// Setea el repositorio de claves:
						attachment.setSynchronizer(sync);

						// Configura los eventos iniciales para este canal:
						Event.enable(
							downstream,
							attachment.getInitialOptions());
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
		}
	}
