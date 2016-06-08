
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
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

			try {

				// Establecemos la nueva conexión entrante:
				SocketChannel socket = getServer(key).accept();

				if (socket != null) {

					// Fabrico un nuevo 'attachment':
					Attachment attachment = getFactory(key).create();

					// Registro el nuevo cliente y sus datos:
					SelectionKey downstream = socket
						.configureBlocking(false)
						.register(key.selector(), 0, attachment);

					// Especifico el flujo que identifica este canal:
					attachment.setDownstream(downstream);

					// El 'attachment' sabe donde se almacenan sus claves:
					attachment.setSynchronizer(sync);

					// Configura los eventos para este canal:
					setOptions(attachment, downstream);
				}
				else throw new IOException();
			}
			catch (IOException exception) {

				logger.error(Message.UNKNOWN.getMessage(), exception);
			}
		}

		/*
		** Permite obtener la fábrica almacenada en el canal
		** asociado al ServerSocket que aceptó la conexión.
		*/

		private AttachmentFactory getFactory(SelectionKey key) {

			AttachmentFactory factory = (AttachmentFactory) key.attachment();
			if (factory != null) return factory;
			else return AttachmentFactory.DEFAULT;
		}

		/*
		** Determina los eventos iniciales a los que este canal
		** debe responder en función del estado por defecto del
		** 'attachment', el cual es obtenido inmediatamente luego de
		** crear el mismo a través de una fábrica (AttachmentFactory).
		*/

		private void setOptions(Attachment attachment, SelectionKey key) {

			/*
			** En este caso utilizamos los métodos provistos por 'Event'
			** en lugar de utilziar el 'Synchronizer', debido a que la
			** clave es nueva, todavía no se encuentra en el repositorio,
			** y además, el canal creado no tiene relación alguna con el
			** canal del servidor (ServerSocketChannel).
			*/

			int options = Event.READ.getOptions();
			if (attachment != null) {

				attachment.getOutboundBuffer().flip();

				if (attachment.hasOutboundData())
					options |= Event.WRITE.getOptions();

				attachment.getOutboundBuffer().compact();
			}
			Event.enable(key, options);
		}

		/*
		** Para la clave del evento recibido, se extrae el
		** ServerSocketChannel correspondiente.
		*/

		private ServerSocketChannel getServer(SelectionKey key) {

			return (ServerSocketChannel) key.channel();
		}
	}
